package com.babilonia.domain.usecase.ar

import android.graphics.RectF
import com.babilonia.data.datasource.system.GravityProvider
import com.babilonia.data.model.DataResult
import com.babilonia.data.model.ar.ArState
import com.babilonia.data.model.ar.tag.MovableArObject
import com.babilonia.data.model.geo.LocationRequest
import com.babilonia.data.repository.ArRepository
import com.babilonia.domain.model.Filter
import com.babilonia.domain.usecase.base.SubscriberUseCase
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import javax.inject.Inject

class GetArStateSubscriberUseCase @Inject constructor(
    private val arRepository: ArRepository,
    private val gravityProvider: GravityProvider
) :
    SubscriberUseCase<DataResult<ArState>, GetArStateSubscriberUseCase.Params>() {

    private lateinit var canvasRectF: RectF
    private var horizontalSpace: Float = 0F
    private var verticalSpace: Float = 0F

    override fun buildSubscriptionUseCase(params: Params): Observable<DataResult<ArState>> {

        canvasRectF = RectF(0.0f, 0.0f, params.sceneWidth.toFloat(), params.sceneHeight.toFloat())
        horizontalSpace = params.horizontalSpace
        verticalSpace = params.verticalSpace

        return Observable.combineLatest(
            gravityProvider.getGravity(),
            arRepository.getArObjects(
                params.cameraProjectionMatrix,
                params.locationRequest,
                params.sceneWidth,
                params.sceneHeight,
                params.filters
            ),
            BiFunction<Float, DataResult<ArState>, DataResult<ArState>> { gravityX, state ->
                when (state) {
                    is DataResult.Success -> {
                        val newTags = state.data.arObjects.map {
                            MovableArObject(it).apply { tagRect.offset(0f, gravityX) }
                        }
                        sortViewByDistanceAndAdjustPosition(newTags, canvasRectF)
                        DataResult.Success(ArState(state.data.azimuth, newTags))
                    }
                    is DataResult.Error -> state
                }
            })
    }

    private fun sortViewByDistanceAndAdjustPosition(sourceTags: List<MovableArObject>, canvasRectF: RectF) {
        if (sourceTags.size < 2) return

        val sorted = sourceTags
            .filter { RectF.intersects(canvasRectF, it.tagRect) }
            .sortedBy { it.distance }
            .map { it.tagRect }

        if (sorted.size < 2) return

        val placed = mutableListOf<RectF>()
        val intersectRectF = RectF()

        for (sortedRectF in sorted) {

            val permittedDirections = hashSetOf<Dir>()
            var verticalTotalOffset = 0F
            var horizontalTotalOffset = 0F

            for (placedRectF in placed) {
                if (intersectRectF.setIntersect(sortedRectF, placedRectF)) {
                    val verOffset = intersectRectF.height() + verticalTotalOffset
                    val horOffset = intersectRectF.width() + horizontalTotalOffset

                    if (verOffset < horOffset) {
                        verticalTotalOffset = verOffset
                        when {
                            permittedDirections.contains(Dir.TOP) -> moveRect(placedRectF, sortedRectF, Dir.TOP)
                            permittedDirections.contains(Dir.BOTTOM) -> moveRect(placedRectF, sortedRectF, Dir.BOTTOM)
                            sortedRectF.top <= placedRectF.centerY() -> {
                                moveRect(placedRectF, sortedRectF, Dir.TOP)
                                permittedDirections.add(Dir.TOP)
                            }
                            else -> {
                                moveRect(placedRectF, sortedRectF, Dir.BOTTOM)
                                permittedDirections.add(Dir.BOTTOM)
                            }
                        }
                    } else {
                        horizontalTotalOffset = horOffset
                        when {
                            permittedDirections.contains(Dir.LEFT) -> moveRect(placedRectF, sortedRectF, Dir.LEFT)
                            permittedDirections.contains(Dir.RIGHT) -> moveRect(placedRectF, sortedRectF, Dir.RIGHT)
                            sortedRectF.left <= placedRectF.centerX() -> {
                                moveRect(placedRectF, sortedRectF, Dir.LEFT)
                                permittedDirections.add(Dir.LEFT)
                            }
                            else -> {
                                moveRect(placedRectF, sortedRectF, Dir.RIGHT)
                                permittedDirections.add(Dir.RIGHT)
                            }
                        }

                    }
                }
            }

            placed.add(sortedRectF)
        }
    }

    private fun moveRect(source: RectF, target: RectF, direction: Dir) {
        when (direction) {
            Dir.LEFT -> target.offset(source.left - target.right - horizontalSpace, 0F)

            Dir.RIGHT -> target.offsetTo(source.right + horizontalSpace, target.top)

            Dir.TOP -> target.offset(0F, source.top - target.bottom - verticalSpace)

            Dir.BOTTOM -> target.offsetTo(target.left, source.bottom + verticalSpace)
        }
    }

    class Params(
        val locationRequest: LocationRequest,
        val cameraProjectionMatrix: FloatArray,
        val sceneWidth: Int,
        val sceneHeight: Int,
        val horizontalSpace: Float,
        val verticalSpace: Float,
        val filters: List<Filter>
    )

    enum class Dir {
        LEFT, RIGHT, TOP, BOTTOM
    }
}

