package com.babilonia.data.network.model.json

import com.google.gson.annotations.SerializedName

// Created by Anton Yatsenko on 24.07.2019.
class ListingsMetadataJson(
    @SerializedName("listings_count")
    val listingsCount: Int = 0,

    @SerializedName("max_built_area")
    val maxBuiltArea: Int = 0,

    @SerializedName("max_total_area")
    val maxTotalArea: Int = 0
)