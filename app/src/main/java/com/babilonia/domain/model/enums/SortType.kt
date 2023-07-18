package com.babilonia.domain.model.enums

// Created by Anton Yatsenko on 22.07.2019.
enum class SortType(var value: String, var order: String) {
    MOST_RELEVANT("relevant_position", "desc"),
    NEWEST("created_at", "desc"),
    OLDEST("created_at", "asc"),
    EXPENSIVE("price", "desc"),
    CHEAPEST("price", "asc"),
    EXPENSIVE_M2("m2price", "desc"),
    CHEAPEST_M2("m2price", "asc"),
    LARGEST("area", "desc"),
    SMALLEST("area", "asc"),
    NEAREST("distance", "asc"),
    DISTANT("distance", "desc");

    companion object {
        fun getByPosition(pos: Int): SortType {
            return values()[pos]
        }
    }
}