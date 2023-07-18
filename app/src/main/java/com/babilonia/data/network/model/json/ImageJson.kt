package com.babilonia.data.network.model.json

import com.babilonia.EmptyConstants
import com.google.gson.annotations.SerializedName

// Created by Anton Yatsenko on 07.06.2019.
open class ImageJson {

    var id: Int = EmptyConstants.EMPTY_INT
    var photo: ThumbsJson? = null
    @SerializedName("created_at")
    var createdAt: String? = null
}