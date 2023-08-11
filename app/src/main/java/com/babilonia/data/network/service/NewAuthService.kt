package com.babilonia.data.network.service

import com.babilonia.data.network.model.*
import com.babilonia.data.network.model.json.AppConfigJson
import com.babilonia.data.network.model.json.ImageJson
import com.babilonia.data.network.model.json.UserJson
import io.reactivex.Single
import okhttp3.MultipartBody
import retrofit2.http.*

interface NewAuthService {
    @GET("public/app_config")
    fun getAppConfig(): Single<BaseResponse<AppConfigJson>>

    @POST("auth/signup")
    fun signUp(
        @Body body: SignUpRequest
    ): Single<BaseResponse<SignUpResponse>>

    @POST("auth/login")
    fun logIn(@Body body: LogInRequest): Single<BaseResponse<LogInResponse>>

    @DELETE("me/profile")
    fun deleteAccount(): Single<BaseResponse<Any>>

    @Multipart
    @POST("me/images")
    fun uploadImages(
        @Part image: MultipartBody.Part,
        @Part source: MultipartBody.Part,
        @Part type: MultipartBody.Part
    ): Single<BaseResponse<IdsBaseResponse<List<ImageJson>>>>

    //@Multipart
    @PUT("me/profile")
    fun updateUser(
        @Body body: UpdateUserRequest
    ): Single<BaseResponse<UserJson>>

    @GET("me/profile")
    fun getUser(): Single<BaseResponse<UserJson>>
}