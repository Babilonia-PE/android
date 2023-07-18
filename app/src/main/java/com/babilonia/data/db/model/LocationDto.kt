package com.babilonia.data.db.model

import io.realm.RealmObject

// Created by Anton Yatsenko on 11.06.2019.
open class LocationDto : RealmObject() {
    var latitude: Float? = null
    var longitude: Float? = null
    var address: String? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LocationDto

        if (latitude != other.latitude) return false
        if (longitude != other.longitude) return false
        if (address != other.address) return false

        return true
    }

    override fun hashCode(): Int {
        var result = latitude?.hashCode() ?: 0
        result = 31 * result + (longitude?.hashCode() ?: 0)
        result = 31 * result + (address?.hashCode() ?: 0)
        return result
    }

}