package com.babilonia.data.db.model

import com.babilonia.EmptyConstants
import io.realm.RealmObject

// Created by Anton Yatsenko on 11.06.2019.
open class LocationDto : RealmObject() {
    var latitude: Float? = null
    var longitude: Float? = null
    var address: String? = null
    var department: String? = null
    var district: String? = null
    var province: String? = null
    var zipCode: String? = null
    var country: String? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LocationDto

        if (latitude != other.latitude) return false
        if (longitude != other.longitude) return false
        if (address != other.address) return false

        if (department != other.department) return false
        if (district != other.district) return false
        if (province != other.province) return false
        if (zipCode != other.zipCode) return false
        if (country != other.country) return false

        return true
    }

    override fun hashCode(): Int {
        var result = latitude?.hashCode() ?: 0
        result = 31 * result + (longitude?.hashCode() ?: 0)
        result = 31 * result + (address?.hashCode() ?: 0)

        result = 31 * result + (department?.hashCode() ?: 0)
        result = 31 * result + (district?.hashCode() ?: 0)
        result = 31 * result + (province?.hashCode() ?: 0)
        result = 31 * result + (zipCode?.hashCode() ?: 0)
        result = 31 * result + (country?.hashCode() ?: 0)

        return result
    }

}