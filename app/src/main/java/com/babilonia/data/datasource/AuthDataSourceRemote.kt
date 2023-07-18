package com.babilonia.data.datasource

import com.babilonia.data.network.model.AuthRequest
import com.babilonia.data.network.model.AuthResponse
import com.babilonia.data.network.model.BaseResponse
import com.babilonia.data.network.model.json.AppConfigJson
import com.babilonia.data.network.model.json.UpdateUserJson
import com.babilonia.data.network.model.json.UserJson
import io.reactivex.Single
import retrofit2.Call

// Created by Anton Yatsenko on 27.05.2019.
interface AuthDataSourceRemote {
    fun authenticate(authRequest: AuthRequest): Single<AuthResponse>
    fun refreshToken(token: String): Call<BaseResponse<AuthResponse>>
    fun uploadUserAvatar(avatar: String, firstName: String, lastName: String, email: String): Single<UserJson>
    fun updateUser(updateUserJson: UpdateUserJson): Single<UserJson>
    fun getUser(): Single<UserJson>
    fun getAppConfig(): Single<AppConfigJson>
}