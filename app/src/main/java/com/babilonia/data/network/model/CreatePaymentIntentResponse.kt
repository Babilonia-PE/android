package com.babilonia.data.network.model

import com.google.gson.annotations.SerializedName

class CreatePaymentIntentResponse(
    @SerializedName("status") val state: String? = null,
    @SerializedName("order_id") val order: String? = null,
    @SerializedName("deviceSessionId") val deviceSessionId: String? = null,
    @SerializedName("paymentIntentId") val paymentIntentId: String? = null,
)