package com.babilonia.data.utils

import com.babilonia.data.model.ar.navigator.Navigatable
import com.babilonia.domain.utils.ArTagType
import com.babilonia.domain.utils.ArTagTypeProvider
import javax.inject.Inject

class DistanceBasedArTagTypeProvider @Inject constructor() : ArTagTypeProvider {
    override fun getArTagType(navigatable: Navigatable): ArTagType {
        val distance = navigatable.distance
        return when {
            distance <= 180 -> ArTagType.LARGE
            distance <= 260 -> ArTagType.MEDIUM
            else -> ArTagType.SMALL
        }
    }
}