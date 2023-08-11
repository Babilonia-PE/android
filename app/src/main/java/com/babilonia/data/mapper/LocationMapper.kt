package com.babilonia.data.mapper

import com.babilonia.EmptyConstants
import com.babilonia.data.db.model.LocationDto
import com.babilonia.data.network.model.json.LocationJson
import com.babilonia.domain.model.Location
import com.babilonia.presentation.utils.SvgUtil.convertEmptyToNull
import com.babilonia.presentation.utils.SvgUtil.refactorAddress
import javax.inject.Inject

// Created by Anton Yatsenko on 24.06.2019.
class LocationMapper @Inject constructor() : Mapper<LocationDto, LocationJson, Location> {
    override fun mapRemoteToLocal(from: LocationJson): LocationDto {
        return mapDomainToLocal(mapRemoteToDomain(from))
    }

    override fun mapLocalToRemote(from: LocationDto): LocationJson {
        return mapDomainToRemote(mapLocalToDomain(from))
    }

    override fun mapDomainToLocal(from: Location): LocationDto {
        return LocationDto().apply {
            address = refactorAddress(from.address?.trim())
            latitude = from.latitude.toFloat()
            longitude = from.longitude.toFloat()

            department = from.department?.trim()
            district = from.district?.trim()
            province = from.province?.trim()
            zipCode = from.zipCode?.trim()
            country = from.country?.trim()
        }
    }

    override fun mapDomainToRemote(from: Location): LocationJson {
        return LocationJson().apply {
            address = refactorAddress(from.address?.trim())
            latitude = from.latitude.toFloat()
            longitude = from.longitude.toFloat()

            department = convertEmptyToNull(from.department)
            district   = convertEmptyToNull(from.district)
            province   = convertEmptyToNull(from.province)
            zipCode    = convertEmptyToNull(from.zipCode)
            country    = convertEmptyToNull(from.country)
        }
    }

    override fun mapLocalToDomain(from: LocationDto): Location {
        return Location().apply {
            address = refactorAddress(from.address?.trim())
            latitude = from.latitude?.toDouble() ?: EmptyConstants.ZERO_DOUBLE
            longitude = from.longitude?.toDouble() ?: EmptyConstants.ZERO_DOUBLE

            department = from.department?.trim()
            district = from.district?.trim()
            province = from.province?.trim()
            zipCode = from.zipCode?.trim()
            country = from.country?.trim()
        }
    }

    override fun mapRemoteToDomain(from: LocationJson): Location {
        return Location().apply {
            address = refactorAddress(from.address?.trim())
            latitude = from.latitude?.toDouble() ?: EmptyConstants.ZERO_DOUBLE
            longitude = from.longitude?.toDouble() ?: EmptyConstants.ZERO_DOUBLE

            department = from.department?.trim()
            district = from.district?.trim()
            province = from.province?.trim()
            zipCode = from.zipCode?.trim()
            country = from.country?.trim()
        }
    }
}