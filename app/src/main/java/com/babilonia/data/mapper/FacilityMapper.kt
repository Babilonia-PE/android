package com.babilonia.data.mapper

import com.babilonia.data.db.model.FacilityDto
import com.babilonia.data.db.model.ThumbsDto
import com.babilonia.data.network.model.json.FacilityJson
import com.babilonia.data.network.model.json.ThumbsJson
import com.babilonia.domain.model.Facility
import javax.inject.Inject

// Created by Anton Yatsenko on 20.06.2019.
class FacilityMapper @Inject constructor() : Mapper<FacilityDto, FacilityJson, Facility> {
    override fun mapRemoteToLocal(from: FacilityJson): FacilityDto {
        return mapDomainToLocal(mapRemoteToDomain(from))
    }

    override fun mapLocalToRemote(from: FacilityDto): FacilityJson {
        return mapDomainToRemote(mapLocalToDomain(from))
    }

    override fun mapDomainToLocal(from: Facility): FacilityDto {
        return FacilityDto().apply {
            id = from.id
            key = from.key
            title = from.title
            icon = ThumbsDto().apply {
                url = from.icon
            }
        }
    }

    override fun mapDomainToRemote(from: Facility): FacilityJson {
        return FacilityJson().apply {
            id = from.id
            key = from.key
            title = from.title
            icon = ThumbsJson().apply {
                url = from.icon
            }
        }
    }

    override fun mapLocalToDomain(from: FacilityDto): Facility = Facility(
        from.id,
        from.key,
        from.title,
        from.icon?.url
    )

    override fun mapRemoteToDomain(from: FacilityJson): Facility = Facility(
        from.id,
        from.key,
        from.title,
        from.icon?.url
    )

}