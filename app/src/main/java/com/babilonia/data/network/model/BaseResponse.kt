package com.babilonia.data.network.model

import com.google.gson.annotations.SerializedName

// Created by Anton Yatsenko on 04.06.2019.
class BaseResponse<T>(var data: T)

class IdsBaseResponse<T>(var ids: T)
class ListingBaseResponse<T>(
    @SerializedName("object") var data: T
)