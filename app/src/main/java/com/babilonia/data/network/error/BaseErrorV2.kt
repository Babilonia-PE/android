package com.babilonia.data.network.error

import com.google.gson.annotations.SerializedName

data class BaseErrorV2(
    @SerializedName("state") val state: String?,
    @SerializedName("desc") val desc: String?)