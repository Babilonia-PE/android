package com.babilonia.domain.model.enums

import android.content.res.Resources
import com.babilonia.R
import java.util.*

enum class PropertyType {
    APARTMENT,
    HOUSE,
    COMMERCIAL,
    OFFICE,
    LAND,
    ROOM,
    LOCAL_INDUSTRIAL,
    LAND_AGRICULTURAL,
    LAND_INDUSTRIAL,
    LAND_COMMERCIAL,
    COTTAGE,
    BEACH_HOUSE,
    BUILDING,
    HOTEL,
    DEPOSIT,
    PARKING,
    AIRS;

    companion object {
        fun getPropertyName(propertyIndex: Int): String {
            return values()[propertyIndex]
                .name
                .toLowerCase(Locale.ENGLISH)
        }

        fun getLocalizedPropertyName(resources: Resources, unlocalizedName: String?): String {
            val propertyTypes = resources.getStringArray(R.array.property_types)
            if (unlocalizedName.isNullOrEmpty()) {
                return propertyTypes.firstOrNull() ?: ""
            }
            val index = try {
                valueOf(unlocalizedName.toUpperCase(Locale.ENGLISH)).ordinal
            } catch (e: IllegalArgumentException) {
                return ""
            }
            return propertyTypes.getOrNull(index) ?: ""
        }
    }
}