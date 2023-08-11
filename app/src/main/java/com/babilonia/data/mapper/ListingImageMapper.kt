package com.babilonia.data.mapper

import com.babilonia.data.db.model.ImageDto
import com.babilonia.data.db.model.ThumbsDto
import com.babilonia.data.network.model.json.ImageJson
import com.babilonia.data.network.model.json.ThumbsJson
import com.babilonia.domain.model.ListingImage
import javax.inject.Inject

// Created by Anton Yatsenko on 20.06.2019.
class ListingImageMapper @Inject constructor() : Mapper<ImageDto, ImageJson, ListingImage> {
    override fun mapRemoteToLocal(from: ImageJson): ImageDto {
        return mapDomainToLocal(mapRemoteToDomain(from))
    }

    override fun mapLocalToRemote(from: ImageDto): ImageJson {
        return mapDomainToRemote(mapLocalToDomain(from))
    }

    override fun mapDomainToLocal(from: ListingImage): ImageDto = ImageDto().apply {
        id = from.id
        photo = ThumbsDto().apply {
            thumbMiddle = from.url
        }
    }

    override fun mapDomainToRemote(from: ListingImage): ImageJson = ImageJson().apply {
        id = from.id
        photo = ThumbsJson().apply {
            thumbMiddle = from.url
        }
    }

    override fun mapLocalToDomain(from: ImageDto) = ListingImage(from.photo?.thumbMiddle, false, from.id)

    override fun mapRemoteToDomain(from: ImageJson): ListingImage =
        ListingImage(from.photo?.url, false, from.id)

}