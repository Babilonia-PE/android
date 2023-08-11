package com.babilonia.data.repository

import android.content.SharedPreferences
import android.hardware.SensorManager
import android.opengl.Matrix
import com.babilonia.Constants
import com.babilonia.data.datasource.RealEstateDataSource
import com.babilonia.data.datasource.system.LocationProvider
import com.babilonia.data.datasource.system.RotationProvider
import com.babilonia.data.mapper.FiltersMapper
import com.babilonia.data.model.DataResult
import com.babilonia.data.model.ar.ArState
import com.babilonia.data.model.ar.tag.MovableArObject
import com.babilonia.data.model.geo.LocationRequest
import com.babilonia.data.model.geo.RealEstateGeoData
import com.babilonia.domain.model.Filter
import com.babilonia.domain.model.geo.ILocation
import com.babilonia.domain.repository.ListingRepository
import com.babilonia.domain.utils.ArTagScreenPositionProvider
import com.babilonia.domain.utils.ArTagType
import com.babilonia.domain.utils.ArTagTypeProvider
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class ArRepositoryImpl @Inject constructor(
    private val arRemoteDataSource: RealEstateDataSource,
    private val listingRepository: ListingRepository,
    private val locationProvider: LocationProvider,
    private val rotationProvider: RotationProvider,
    private val arTagTypeProvider: ArTagTypeProvider,
    private val screenPositionProvider: ArTagScreenPositionProvider,
    private val prefs: SharedPreferences,
    private val filtersMapper: FiltersMapper
) : ArRepository {

    override fun needToShowOnboarding(): Single<Boolean> {
        return Single.create<Boolean> {  emitter ->
            val needToShow = prefs.getBoolean(NEED_TO_SHOW_ONBOARDING, true)
            if (needToShow) {
                prefs.edit().putBoolean(NEED_TO_SHOW_ONBOARDING, false).apply()
            }
            emitter.onSuccess(needToShow)
        }
    }

    override fun getArObjects(
        cameraProjectionMatrix: FloatArray,
        locationRequest: LocationRequest,
        sceneWidth: Int,
        sceneHeight: Int,
        filters: List<Filter>
    ): Observable<DataResult<ArState>> {

        val locationObservable = getLocationObservable(locationRequest)
            .switchMap { locationResult ->
                when (locationResult) {
                    is DataResult.Success -> arRemoteDataSource.getRealEstateList(locationResult.data, filtersMapper.mapToQuery(filters),)
                        .map { realEstateList ->
                            realEstateList
                                .map {
                                    val geoData = RealEstateGeoData(locationResult.data, it.locationAttributes)
                                    MovableArObject(it, geoData)
                                }
                                .filter { it.distance < Constants.AR_VISIBILITY_DISTANCE }
                        }

                    is DataResult.Error -> Observable.error(locationResult.throwable)
                }
            }

        return Observable
            .combineLatest(
                locationObservable,
                getRotationObservable(cameraProjectionMatrix),
                BiFunction<List<MovableArObject>, Pair<FloatArray, FloatArray>, DataResult<ArState>>
                { geoDataList, rotationPair ->

                    val azimuth = normalizeAzimuth(rotationPair.first[0].toDouble())

                    DataResult.Success(
                        ArState(
                            azimuth = azimuth,
                            arObjects = applyScreenPosition(
                                arObjects = geoDataList,
                                rotatedProjectionMatrix = rotationPair.second,
                                sceneWidth = sceneWidth,
                                sceneHeight = sceneHeight
                            )
                        )
                    )
                }
            )
            .subscribeOn(Schedulers.io())
    }

    override fun getNavigationTarget(
        cameraProjectionMatrix: FloatArray,
        locationRequest: LocationRequest,
        sceneWidth: Int,
        sceneHeight: Int,
        selectedId: Long
    ): Observable<DataResult<ArState>> {

        val locationObservable = getLocationObservable(locationRequest)
            .switchMap { locationResult ->
                when (locationResult) {
                    is DataResult.Success -> listingRepository.getListingById(selectedId).toObservable()
                        .map {
                            MovableArObject(it, RealEstateGeoData(locationResult.data, it.locationAttributes))
                        }

                    is DataResult.Error -> Observable.error(locationResult.throwable)
                }
            }

        return Observable
            .combineLatest(
                locationObservable,
                getRotationObservable(cameraProjectionMatrix, true),
                BiFunction<MovableArObject, Pair<FloatArray, FloatArray>, DataResult<ArState>>
                { arObject, rotationPair ->

                    val azimuth = normalizeAzimuth(rotationPair.first[0].toDouble())

                    DataResult.Success(
                        ArState(
                            azimuth = azimuth,
                            arObjects = listOf(
                                applyScreenPosition(
                                    arObject = arObject,
                                    rotatedProjectionMatrix = rotationPair.second,
                                    sceneWidth = sceneWidth,
                                    sceneHeight = sceneHeight
                                )
                            )
                        )
                    )
                }
            )
            .subscribeOn(Schedulers.io())
    }

    private fun applyScreenPosition(
        arObjects: List<MovableArObject>,
        rotatedProjectionMatrix: FloatArray,
        sceneWidth: Int,
        sceneHeight: Int
    ): List<MovableArObject> {

        val coordinateVector = FloatArray(4)
        return arObjects.filter { arObject ->

            Matrix.multiplyMV(
                coordinateVector,
                0,
                rotatedProjectionMatrix,
                0,
                arObject.geoData.coordinatesENU,
                0
            )

            arObject.arTagSizeType = arTagTypeProvider.getArTagType(arObject)
            arObject.tagRect = screenPositionProvider.getPosition(
                arObject.arTagSizeType,
                coordinateVector,
                sceneWidth,
                sceneHeight
            )
            coordinateVector[2] < 0
        }

    }

    private fun applyScreenPosition(
        arObject: MovableArObject,
        rotatedProjectionMatrix: FloatArray,
        sceneWidth: Int,
        sceneHeight: Int
    ): MovableArObject {

        val coordinateVector = FloatArray(4)

        return arObject.apply {
            Matrix.multiplyMV(
                coordinateVector,
                0,
                rotatedProjectionMatrix,
                0,
                arObject.geoData.coordinatesENU,
                0
            )
            arTagSizeType = ArTagType.PIN
            tagRect = screenPositionProvider.getPosition(
                arObject.arTagSizeType,
                coordinateVector,
                sceneWidth,
                sceneHeight
            )
        }
    }

    private fun getLocationObservable(locationRequest: LocationRequest): Observable<DataResult<ILocation>> {
        return locationProvider.getUpdatedLocation(locationRequest)
            .observeOn(Schedulers.io())
    }

    private fun getRotationObservable(
        cameraProjectionMatrix: FloatArray,
        needMapRotationVector: Boolean = false
    ): Observable<Pair<FloatArray, FloatArray>> {
        return rotationProvider.getWindowRotation()
            .observeOn(Schedulers.io())
            .map { rotationAndMapMatrices ->

                val rotationMatrix = rotationAndMapMatrices.first
                val mapRotationMatrix = rotationAndMapMatrices.second

                val rotatedProjectionMatrix = FloatArray(16)
                Matrix.multiplyMM(
                    rotatedProjectionMatrix,
                    0,
                    cameraProjectionMatrix,
                    0,
                    rotationMatrix,
                    0
                )

                val orientation = FloatArray(3)
                SensorManager.getOrientation(
                    if (needMapRotationVector) mapRotationMatrix else rotationMatrix,
                    orientation
                )
                orientation to rotatedProjectionMatrix
            }
    }

    private fun normalizeAzimuth(azimuth: Double): Float = ((Math.toDegrees(azimuth) + 360) % 360).toFloat()

    companion object {
        private const val NEED_TO_SHOW_ONBOARDING = "ONBOARDING"
    }
}