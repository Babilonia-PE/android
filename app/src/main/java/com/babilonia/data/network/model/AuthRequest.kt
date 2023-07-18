package com.babilonia.data.network.model

import com.google.gson.annotations.SerializedName

// Created by Anton Yatsenko on 04.06.2019.
class AuthRequest(@SerializedName("token") var token: String)