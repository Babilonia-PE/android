package com.babilonia.data.mapper

import com.babilonia.data.db.model.NewVersionDto
import com.babilonia.data.network.model.json.NewVersionJson
import com.babilonia.domain.model.NewVersion

fun NewVersion.mapDomainToLocal(): NewVersionDto {
    val update = update
    return NewVersionDto().apply {
        this.update = update
    }
}

fun NewVersion.mapDomainToRemote() =
    NewVersionJson(
        update
    )

fun NewVersionDto.mapLocalToDomain() =
    NewVersion(
        update ?: false
    )

fun NewVersionJson.mapRemoteToDomain() =
    NewVersion(
        update ?: false
    )