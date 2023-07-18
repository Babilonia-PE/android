package com.babilonia.data.network.model.json

import com.google.gson.annotations.SerializedName

// Created by Anton Yatsenko on 25.07.2019.
data class PriceRangeItemJson(
    @SerializedName("listings_count")
    var listingsCount: Int,
    @SerializedName("number")
    var number: Int,
    @SerializedName("start_price")
    var startPrice: Int
)