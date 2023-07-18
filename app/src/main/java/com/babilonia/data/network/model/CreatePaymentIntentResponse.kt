package com.babilonia.data.network.model

import com.google.gson.annotations.SerializedName

class CreatePaymentIntentResponse(
    @SerializedName("client_secret")
    val clientSecret: String? = null
)