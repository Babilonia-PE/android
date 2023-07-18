package com.babilonia.data.mapper

import com.babilonia.data.network.model.json.ListingsMetadataJson
import com.babilonia.presentation.flow.main.search.model.ListingsMetadata
import javax.inject.Inject

class ListingsMetadataMapper @Inject constructor() {

    fun mapRemoteToDomain(metadataJson: ListingsMetadataJson): ListingsMetadata {
        return with (metadataJson) {
            ListingsMetadata(listingsCount, maxBuiltArea, maxTotalArea)
        }
    }
}