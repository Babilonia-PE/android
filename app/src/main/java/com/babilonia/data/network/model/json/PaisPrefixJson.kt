package com.babilonia.data.network.model.json

import com.google.gson.annotations.SerializedName

open class PaisPrefixJson {
    @SerializedName("name") var name: String? = null
    @SerializedName("prefix") var prefix: String? = null
    @SerializedName("mask") var mask: String? = null
    @SerializedName("iso_code") var isoCode: String? = null
}