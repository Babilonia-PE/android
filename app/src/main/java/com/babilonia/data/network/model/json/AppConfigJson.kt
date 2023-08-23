package com.babilonia.data.network.model.json

import com.google.gson.annotations.SerializedName

// Created by Anton Yatsenko on 15.07.2019.
class AppConfigJson {
    var urls: UrlsJson? = null

    @SerializedName("default_location")
    var location: LocationJson? = null

    @SerializedName("new_version")
    var newVersion: NewVersionJson? = null
}