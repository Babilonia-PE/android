package com.babilonia.presentation.flow.main.search.model

sealed class FiltersVisibility(
    val isTotalAreaVisible: Boolean = true,
    val isBuiltAreaVisible: Boolean = true,
    val isYearOfConstructionVisible: Boolean = true,
    val isBathroomsVisible: Boolean = true,
    val isBedroomsVisible: Boolean = true,
    val isTotalFloorsVisible: Boolean = true,
    val isFloorNumberVisible: Boolean = true,
    val isParkingVisible: Boolean = true,
    val isParkingForVisitorsVisible: Boolean = true,
    val isWarehouseVisible: Boolean = false, // filter is disabled for now
    val isPetFriendlyVisible: Boolean = true // this is available ONLY FOR RENT
) {
    class All : FiltersVisibility()

    class Apartment : FiltersVisibility()

    class House : FiltersVisibility(
        isFloorNumberVisible = false
    )

    class Commercial : FiltersVisibility(
        isBedroomsVisible = false,
        isPetFriendlyVisible = false
    )

    class Office : FiltersVisibility(isBedroomsVisible = false)

    class Land : FiltersVisibility(
        isBuiltAreaVisible = false,
        isYearOfConstructionVisible = false,
        isBathroomsVisible = false,
        isBedroomsVisible = false,
        isTotalFloorsVisible = false,
        isFloorNumberVisible = false,
        isParkingVisible = false,
        isParkingForVisitorsVisible = false,
        isWarehouseVisible = false,
        isPetFriendlyVisible = false
    )

    class Room : FiltersVisibility(isBuiltAreaVisible = false)

    companion object {
        // These names are taken from 'property_types_for_sorting' in strings.xml. Please ensure
        // they are same after making any changes
        private const val PROPERTY_TYPE_APARTMENT = "apartment"
        private const val PROPERTY_TYPE_HOUSE = "house"
        private const val PROPERTY_TYPE_COMMERCIAL = "commercial"
        private const val PROPERTY_TYPE_OFFICE = "office"
        private const val PROPERTY_TYPE_LAND = "land"
        private const val PROPERTY_TYPE_ROOM = "room"

        fun getVisibilityByPropertyName(propertyLowercase: String?): FiltersVisibility =
            when (propertyLowercase) {
                PROPERTY_TYPE_APARTMENT -> Apartment()
                PROPERTY_TYPE_HOUSE -> House()
                PROPERTY_TYPE_COMMERCIAL -> Commercial()
                PROPERTY_TYPE_OFFICE -> Office()
                PROPERTY_TYPE_LAND -> Land()
                PROPERTY_TYPE_ROOM -> Room()
                else -> All()
            }
    }
}