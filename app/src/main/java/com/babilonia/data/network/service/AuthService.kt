package com.babilonia.data.network.service

import com.babilonia.data.network.model.AuthRequest
import com.babilonia.data.network.model.AuthResponse
import com.babilonia.data.network.model.BaseResponse
import com.babilonia.data.network.model.RefreshTokenRequest
import com.babilonia.data.network.model.json.AppConfigJson
import com.babilonia.data.network.model.json.UpdateUserJson
import com.babilonia.data.network.model.json.UserJson
import io.reactivex.Single
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

// Created by Anton Yatsenko on 04.06.2019.
interface AuthService {
    @POST("api/firebase/authentication")
    fun authenticate(@Body body: AuthRequest): Single<BaseResponse<AuthResponse>>

    @PUT("api/users/session/restore")
    fun refreshToken(@Body body: RefreshTokenRequest): Call<BaseResponse<AuthResponse>>

    @Multipart
    @PUT("api/users/me/profile")
    fun uploadUserAvatar(
        @Part image: MultipartBody.Part,
        @Part firstName: MultipartBody.Part,
        @Part lastName: MultipartBody.Part,
        @Part email: MultipartBody.Part
    ): Single<BaseResponse<UserJson>>

    @PUT("api/users/me/profile")
    fun updateUser(
        @Body user: BaseResponse<UpdateUserJson>
    ): Single<BaseResponse<UserJson>>

    @GET("api/users/me/profile")
    fun getUser(): Single<BaseResponse<UserJson>>

    @GET("api/app_config")
    fun getAppConfig(): Single<BaseResponse<AppConfigJson>>

}