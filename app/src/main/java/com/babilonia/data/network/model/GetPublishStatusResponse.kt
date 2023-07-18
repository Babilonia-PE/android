package com.babilonia.data.network.model

import com.google.gson.annotations.SerializedName

class GetPublishStatusResponse(
    @SerializedName("status")
    val status: String? = null
)