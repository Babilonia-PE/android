package com.babilonia.data.network.service

import com.babilonia.data.network.model.*
import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.*

// Created by Anton Yatsenko on 04.06.2019.
interface AuthService {
    @POST("api/firebase/authentication")
    fun authenticate(@Body body: AuthRequest): Single<BaseResponse<AuthResponse>>

    @PUT("api/users/session/restore")
    fun refreshToken(@Body body: RefreshTokenRequest): Call<BaseResponse<AuthResponse>>
}