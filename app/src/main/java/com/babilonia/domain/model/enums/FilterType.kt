package com.babilonia.domain.model.enums

// Created by Anton Yatsenko on 24.07.2019.
enum class FilterType(var type: String) {
    LISTING("listing_type"),
    PROPERTY("property_type"),
    PRICE_START("price_start"),
    PRICE_END("price_end"),
    PRICE(""),
    AREA_TOTAL_START("total_area[from]"),
    AREA_TOTAL_END("total_area[to]"),
    AREA_BUILT_START("built_area[from]"),
    AREA_BUILT_END("built_area[to]"),
    YEAR_OF_CONSTRUCTION_START("year_of_construction[from]"),
    YEAR_OF_CONSTRUCTION_END("year_of_construction[to]"),
    BEDROOMS("bedrooms_count"),
    BATHROOMS("bathrooms_count"),
    TOTAL_FLOORS("total_floors_count"),
    FLOOR_NUMBER("floor_number"),
    PARKING("parking_slots_count"),
    PARKING_FOR_VISITORS("parking_for_visits"),
    WAREHOUSE("warehouse"),
    FACILITY("facility_ids[]")
}