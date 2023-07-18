package com.babilonia.data.network.model.json

import com.google.gson.annotations.SerializedName

// Created by Anton Yatsenko on 27.06.2019.
class UpdateUserJson(
    @SerializedName("phone_number")
    var phoneNumber: String? = null,
    @SerializedName("first_name")
    var firstName: String? = null,
    @SerializedName("last_name")
    var lastName: String? = null,
    var email: String? = null
)