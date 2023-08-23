package com.babilonia.data.mapper

import com.babilonia.EmptyConstants
import com.babilonia.data.db.model.AppConfigDto
import com.babilonia.data.network.model.json.AppConfigJson
import com.babilonia.data.network.model.json.UrlsJson
import com.babilonia.domain.model.AppConfig
import javax.inject.Inject

// Created by Anton Yatsenko on 15.07.2019.
class ConfigMapper @Inject constructor(private val locationMapper: LocationMapper) :
    Mapper<AppConfigDto, AppConfigJson, AppConfig> {

    override fun mapDomainToLocal(from: AppConfig): AppConfigDto {
        return AppConfigDto().apply {
            locationDto = from.locationDto?.let { locationMapper.mapDomainToLocal(it) }
            terms = from.terms
            privacyPolicy = from.privacyPolicy
            newVersionDto = from.newVersion?.mapDomainToLocal()
        }
    }

    override fun mapDomainToRemote(from: AppConfig): AppConfigJson {
        return AppConfigJson().apply {
            location = from.locationDto?.let { locationMapper.mapDomainToRemote(it) }
            urls = UrlsJson(from.terms, from.privacyPolicy)
            newVersion = from.newVersion?.mapDomainToRemote()
        }
    }

    override fun mapLocalToDomain(from: AppConfigDto): AppConfig {
        val locationDto = from.locationDto?.let { locationMapper.mapLocalToDomain(it) }
        val newVersionDto = from.newVersionDto?.mapLocalToDomain()
        return AppConfig(locationDto, from.privacyPolicy, from.terms, newVersionDto)
    }

    override fun mapRemoteToDomain(from: AppConfigJson): AppConfig {
        val locationDto = from.location?.let { locationMapper.mapRemoteToDomain(it) }
        val newVersion = from.newVersion?.mapRemoteToDomain()
        return AppConfig(
            locationDto,
            from.urls?.privacyPolicy ?: EmptyConstants.EMPTY_STRING,
            from.urls?.termsOfUse ?: EmptyConstants.EMPTY_STRING,
            newVersion
        )
    }

    override fun mapRemoteToLocal(from: AppConfigJson): AppConfigDto {
        return mapDomainToLocal(mapRemoteToDomain(from))
    }

    override fun mapLocalToRemote(from: AppConfigDto): AppConfigJson {
        return mapDomainToRemote(mapLocalToDomain(from))
    }
}