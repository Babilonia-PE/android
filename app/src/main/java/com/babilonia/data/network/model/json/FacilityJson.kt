package com.babilonia.data.network.model.json

import com.babilonia.EmptyConstants
import com.google.gson.annotations.SerializedName

// Created by Anton Yatsenko on 07.06.2019.
open class FacilityJson {
    @SerializedName("id")
    var id: Int = EmptyConstants.EMPTY_INT
    @SerializedName("key")
    var key: String? = null
    @SerializedName("title")
    var title: String? = null
    @SerializedName("icon_android")
    var icon: ThumbsJson? = null
}