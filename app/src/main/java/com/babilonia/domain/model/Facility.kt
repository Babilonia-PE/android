package com.babilonia.domain.model

// Created by Anton Yatsenko on 07.06.2019.
class Facility(
    var id: Int = 0,
    var key: String?,
    var title: String?,
    var icon: String?,
    var isChecked: Boolean = false

) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Facility

        if (title != other.title) return false

        return true
    }

    override fun hashCode(): Int {
        return title?.hashCode() ?: 0
    }
}