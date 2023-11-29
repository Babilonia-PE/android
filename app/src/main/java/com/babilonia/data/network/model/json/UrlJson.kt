package com.babilonia.data.network.model.json

import com.google.gson.annotations.SerializedName

class UrlJson {
    @SerializedName("main")
    var main: String? = null
    @SerializedName("share")
    var share: String? = null
}