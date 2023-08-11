package com.babilonia.data.network.model

import com.google.gson.annotations.SerializedName

class SignUpResponse(
    var authorization: String,
    @SerializedName("user_id")
    var userId: Long
)