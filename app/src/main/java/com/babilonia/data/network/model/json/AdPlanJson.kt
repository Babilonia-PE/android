package com.babilonia.data.network.model.json

import com.google.gson.annotations.SerializedName

open class AdPlanJson(
    @SerializedName("plan_key")
    val key: String? = null,

    @SerializedName("title")
    val title: String? = null,

    @SerializedName("descriptions")
    val descriptions: List<String>? = null,

    @SerializedName("products")
    val products: List<AdProductJson>? = null
)