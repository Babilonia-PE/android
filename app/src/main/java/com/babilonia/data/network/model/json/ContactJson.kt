package com.babilonia.data.network.model.json

import com.google.gson.annotations.SerializedName

open class ContactJson {
    @SerializedName("name") var contactName: String? = null
    @SerializedName("email") var contactEmail: String? = null
    @SerializedName("phone") var contactPhone: String? = null
}