package com.babilonia.data.network.model.json

import com.google.gson.annotations.SerializedName

// Created by Anton Yatsenko on 06.06.2019.
open class UserJson {
    var id: Long? = null
    @SerializedName("phone_number")
    var phoneNumber: String? = null
    @SerializedName("first_name")
    var firstName: String? = null
    @SerializedName("last_name")
    var lastName: String? = null
    var avatar: ThumbsJson? = null
    var email: String? = null
}