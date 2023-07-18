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
    ROOM;

    companion object {
        fun getPropertyName(propertyIndex: Int): String {
            return values()[propertyIndex]
                .name
                .toLowerCase(Locale.ENGLISH)
        }

        fun getLocalizedPropertyName(resources: Resources, unlocalizedName: String?): String {
            val propertyTypes = resources.getStringArray(R.array.property_types)
            if (unlocalizedName == null) {
                return propertyTypes.first()
            }
            val index = valueOf(unlocalizedName.toUpperCase(Locale.ENGLISH)).ordinal
            return propertyTypes[index]
        }
    }
}