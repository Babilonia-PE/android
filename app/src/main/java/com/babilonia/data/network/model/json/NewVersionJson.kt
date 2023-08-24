package com.babilonia.data.network.model.json

import com.google.gson.annotations.SerializedName

data class NewVersionJson(
    @SerializedName("android")
    var update: Boolean? = null
)