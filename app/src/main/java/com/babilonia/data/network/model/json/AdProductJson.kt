package com.babilonia.data.network.model.json

import com.google.gson.annotations.SerializedName

open class AdProductJson(
    @SerializedName("key")
    val key: String? = null,

    @SerializedName("duration")
    val duration: Int? = null,

    @SerializedName("price")
    val price: Float? = null,

    @SerializedName("comment")
    val comment: String? = null
)