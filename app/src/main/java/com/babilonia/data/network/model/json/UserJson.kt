package com.babilonia.data.network.model.json

import com.google.gson.annotations.SerializedName

// Created by Anton Yatsenko on 06.06.2019.
open class UserJson {
    var id: Long? = null
    @SerializedName("phone_number")
    var phoneNumber: String? = null
    @SerializedName("full_name")
    var fullName: String? = null
    var avatar: ThumbsJson? = null
    var email: String? = null
}