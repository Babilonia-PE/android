package com.babilonia.data.network.model.json

import com.google.gson.annotations.SerializedName

// Created by Anton Yatsenko on 15.07.2019.
class UrlsJson(
    @SerializedName("terms_of_use") var termsOfUse: String,
    @SerializedName("privacy_policy") var privacyPolicy: String
)