package com.babilonia.data.network.model

import com.google.gson.annotations.SerializedName

class LogInTokensResponse(
    var type: String,
    var authentication: String,
    @SerializedName("user_id")
    var userId: Long
)