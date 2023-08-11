package com.babilonia.data.network.model.json

import com.google.gson.annotations.SerializedName

// Created by Anton Yatsenko on 27.06.2019.
class UpdateUserJson(
    @SerializedName("phone_number")
    var phoneNumber: String? = null,
    @SerializedName("full_name")
    var fullName: String? = null,
    var email: String? = null
)