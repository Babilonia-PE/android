package com.babilonia.data.mapper

import com.babilonia.data.db.model.NewVersionDto
import com.babilonia.data.network.model.json.NewVersionJson
import com.babilonia.domain.model.NewVersion

fun NewVersion.mapDomainToLocal(): NewVersionDto {
    val android = android
    return NewVersionDto().apply {
        this.android = android
    }
}

fun NewVersion.mapDomainToRemote() =
    NewVersionJson(
        android
    )

fun NewVersionDto.mapLocalToDomain() =
    NewVersion(
        android ?: false
    )

fun NewVersionJson.mapRemoteToDomain() =
    NewVersion(
        android ?: false
    )