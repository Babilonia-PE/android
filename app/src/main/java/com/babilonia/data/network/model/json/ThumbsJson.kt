package com.babilonia.data.network.model.json

import com.google.gson.annotations.SerializedName

// Created by Anton Yatsenko on 18.06.2019.
class ThumbsJson {
    @SerializedName("url")
    var url: String? = null
    @SerializedName("url_min")
    var thumbMin: String? = null
    @SerializedName("url_middle")
    var thumbMiddle: String? = null
    @SerializedName("url_large")
    var thumbLarge: String? = null
}