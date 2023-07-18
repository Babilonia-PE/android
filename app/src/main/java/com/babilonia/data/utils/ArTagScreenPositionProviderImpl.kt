package com.babilonia.data.utils

import android.content.Context
import android.graphics.RectF
import com.babilonia.R
import com.babilonia.domain.utils.ArTagScreenPositionProvider
import com.babilonia.domain.utils.ArTagType
import com.babilonia.domain.utils.ArTagType.*
import javax.inject.Inject

class ArTagScreenPositionProviderImpl @Inject constructor(private val context: Context) : ArTagScreenPositionProvider {

    private val sizesMap = hashMapOf<ArTagType, Pair<Float, Float>>()

    override fun getPosition(
        type: ArTagType,
        coordinateVector: FloatArray,
        sceneWidth: Int,
        sceneHeight: Int
    ): RectF {

        val (halfHeight, halfWidth) = getSizeInPx(type)

        // cameraCoordinateVector[2] is z, that always less than 0 to display on right position
        // if z > 0, the point will display on the opposite

        return if (coordinateVector[2] < 0) {
            val x = (0.5f + coordinateVector[0] / coordinateVector[3]) * sceneWidth
            val y = (0.5f - coordinateVector[1] / coordinateVector[3]) * sceneHeight

            RectF(x - halfWidth, y - halfHeight, x + halfWidth, y + halfHeight)
        } else {
            RectF(0F - 4F * halfWidth, 0F - 4F * halfHeight, 0F, 0F)
        }
    }

    override fun getArPinPosition(
        coordinateVector: FloatArray,
        sceneWidth: Int,
        sceneHeight: Int
    ): RectF {

        val (halfHeight, halfWidth) = getSizeInPx(PIN)

        // cameraCoordinateVector[2] is z, that always less than 0 to display on right position
        // if z > 0, the point will display on the opposite

        return if (coordinateVector[2] < 0) {
            val x = (0.5f + coordinateVector[0] / coordinateVector[3]) * sceneWidth
            val y = (0.5f - coordinateVector[1] / coordinateVector[3]) * sceneHeight

            RectF(x - halfWidth, y - halfHeight, x + halfWidth, y + halfHeight)
        } else {
            RectF(0F - 4F * halfWidth, 0F - 4F * halfHeight, 0F, 0F)
        }
    }

    private fun getSizeInPx(sizeType: ArTagType): Pair<Float, Float> = sizesMap[sizeType] ?: with(sizeType) {

        val (heightId, widthId) = when (sizeType) {
            SMALL -> R.dimen.ar_tag_small_height to R.dimen.ar_tag_small_width
            MEDIUM -> R.dimen.ar_tag_medium_height to R.dimen.ar_tag_medium_width
            LARGE -> R.dimen.ar_tag_large_height to R.dimen.ar_tag_large_width
            PIN -> R.dimen.ar_tag_destination_height to R.dimen.ar_tag_destination_width
            INITIAL -> 0 to 0
        }

        val halfHeight = context.resources.getDimension(heightId) / 2
        val halfWidth = context.resources.getDimension(widthId) / 2

        sizesMap[sizeType] = halfHeight to halfWidth

        halfHeight to halfWidth
    }
}