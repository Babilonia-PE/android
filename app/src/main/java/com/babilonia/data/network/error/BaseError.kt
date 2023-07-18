package com.babilonia.data.network.error

import com.babilonia.EmptyConstants
import com.google.gson.annotations.SerializedName

// Created by Anton Yatsenko on 06.06.2019.
data class BaseError(
    @SerializedName("data")
    val dataError: Data?
) {
    fun getKey(): String = dataError?.errors?.get(0)?.key ?: EmptyConstants.EMPTY_STRING
    fun getMessage(): String = dataError?.errors?.get(0)?.message ?: EmptyConstants.EMPTY_STRING
}

data class Data(
    @SerializedName("errors")
    val errors: List<Error?>?
)

data class Error(
    @SerializedName("key")
    val key: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("payload")
    val payload: Payload?,
    @SerializedName("type")
    val type: String?
)

data class Payload(
    @SerializedName("code")
    val code: String?,
    @SerializedName("exception")
    val exception: String?
)