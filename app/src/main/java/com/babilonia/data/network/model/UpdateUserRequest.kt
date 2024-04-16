package com.babilonia.data.network.model

import com.google.gson.annotations.SerializedName

class UpdateUserRequest(
    @SerializedName("full_name") var fullName: String,
    @SerializedName("email") var email: String,
    @SerializedName("phone_number") var phoneNumber: String,
    @SerializedName("prefix") var prefix: String,
    @SerializedName("change_password") var changePassword: Boolean = false,
    @SerializedName("password") var password: String? = null,
    @SerializedName("photo") var photoId: List<Int>? = null
)