package com.babilonia.data.network.model

import com.google.gson.annotations.SerializedName

class SignUpRequest(
    @SerializedName("full_name") var fullName: String,
    @SerializedName("email") var email: String,
    @SerializedName("password") var password: String,
    @SerializedName("prefix") var prefix: String,
    @SerializedName("phone_number") var phoneNumber: String,
    @SerializedName("ipa") var ipAddress: String,
    @SerializedName("ua") var userAgent: String,
    @SerializedName("sip") var signProvider: String
)