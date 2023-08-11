package com.babilonia.data.network.model.json

import com.google.gson.annotations.SerializedName

class UserActionRequest(
    @SerializedName("listing_id") val listingId: Long,
    @SerializedName("key") val key: String,
    @SerializedName("ipa") var ipAddress: String,
    @SerializedName("ua") var userAgent: String,
    @SerializedName("sip") var signProvider: String,
    @SerializedName("step") var step: Int = 1
)