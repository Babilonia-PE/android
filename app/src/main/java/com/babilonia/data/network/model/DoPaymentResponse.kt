package com.babilonia.data.network.model

import com.google.gson.annotations.SerializedName

class DoPaymentResponse(
    @SerializedName("status") val state: String? = null,
    @SerializedName("desc") val description: String? = null
)