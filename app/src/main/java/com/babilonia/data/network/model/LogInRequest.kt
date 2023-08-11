package com.babilonia.data.network.model

import com.google.gson.annotations.SerializedName

class LogInRequest(
    @SerializedName("email") var email: String,
    @SerializedName("password") var password: String,
    @SerializedName("ipa") var ipAddress: String,
    @SerializedName("ua") var userAgent: String,
    @SerializedName("sip") var signProvider: String
)