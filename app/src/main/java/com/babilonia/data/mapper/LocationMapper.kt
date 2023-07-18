package com.babilonia.data.mapper

import com.babilonia.EmptyConstants
import com.babilonia.data.db.model.LocationDto
import com.babilonia.data.network.model.json.LocationJson
import com.babilonia.domain.model.Location
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
            address = from.address
            latitude = from.latitude.toFloat()
            longitude = from.longitude.toFloat()
        }
    }

    override fun mapDomainToRemote(from: Location): LocationJson {
        return LocationJson().apply {
            address = from.address
            latitude = from.latitude.toFloat()
            longitude = from.longitude.toFloat()
        }
    }

    override fun mapLocalToDomain(from: LocationDto): Location {
        return Location().apply {
            address = from.address
            latitude = from.latitude?.toDouble() ?: EmptyConstants.ZERO_DOUBLE
            longitude = from.longitude?.toDouble() ?: EmptyConstants.ZERO_DOUBLE
        }
    }

    override fun mapRemoteToDomain(from: LocationJson): Location {
        return Location().apply {
            address = from.address
            latitude = from.latitude?.toDouble() ?: EmptyConstants.ZERO_DOUBLE
            longitude = from.longitude?.toDouble() ?: EmptyConstants.ZERO_DOUBLE
        }
    }
}