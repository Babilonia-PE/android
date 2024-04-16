package com.babilonia.data.datasource

import com.babilonia.data.network.model.*
import com.babilonia.data.network.model.json.AppConfigJson
import com.babilonia.data.network.model.json.ImageJson
import com.babilonia.data.network.model.json.PaisPrefixJson
import com.babilonia.data.network.model.json.UserJson
import com.babilonia.domain.model.PaisPrefix
import io.reactivex.Single
import retrofit2.Call

// Created by Anton Yatsenko on 27.05.2019.
interface AuthDataSourceRemote {
    fun signUp(
        fullName: String,
        email: String,
        password: String,
        prefix: String,
        phoneNumber: String,
        ipAddress: String,
        userAgent: String,
        signProvider: String
    ): Single<SignUpResponse>

    fun getListPaisPrefix(): Single<List<PaisPrefixJson>>

    fun logIn(
        email: String,
        password: String,
        ipAddress: String,
        userAgent: String,
        signProvider: String
    ): Single<LogInResponse>

    fun authenticate(authRequest: AuthRequest): Single<AuthResponse>
    fun refreshToken(token: String): Call<BaseResponse<AuthResponse>>
    fun uploadImages(
        image: String,
        type: String
    ): Single<List<ImageJson>>

    fun updateUser(
        fullName: String,
        email: String,
        phoneNumber: String,
        prefix: String,
        password: String?,
        photoId: Int?
    ): Single<UserJson>
    fun getUser(): Single<UserJson>
    fun getAppConfig(version: Int): Single<AppConfigJson>
    fun deleteAccount(): Single<BaseResponse<Any>>
}