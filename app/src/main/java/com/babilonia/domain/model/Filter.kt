package com.babilonia.domain.model

// Created by Anton Yatsenko on 23.07.2019.
data class Filter(var displayedName: String, var type: String, var value: String) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Filter

        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        return type.hashCode()
    }
}