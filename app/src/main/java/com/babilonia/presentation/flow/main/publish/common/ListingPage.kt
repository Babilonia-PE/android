package com.babilonia.presentation.flow.main.publish.common

// Created by Anton Yatsenko on 06.06.2019.
enum class ListingPage(val page: Int) {
    COMMON(0),
    DETAILS(1),
    FACILITIES(2),
    ADVANCED(3),
    PHOTOS(4);

    companion object {
        fun byPage(a: Int): ListingPage {
            return values().find { it.page == a } ?: COMMON
        }
    }
}