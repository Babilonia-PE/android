package com.babilonia.data.datasource

import com.babilonia.data.db.model.AppConfigDto
import com.babilonia.data.db.model.TokensDto
import com.babilonia.data.db.model.UserDto
import com.babilonia.domain.model.enums.LoginStatus
import io.reactivex.Completable
import io.reactivex.Single

// Created by Anton Yatsenko on 27.05.2019.
interface AuthDataSourceLocal {
    fun isLoggedIn(): Single<LoginStatus>
    fun saveUser(user: UserDto)
    fun getUser(): Single<UserDto>
    fun saveTokens(tokens: TokensDto)
    fun getToken(): String?
    fun getRefresh(): String?
    fun signOut(): Completable
    fun getAppConfig(): Single<AppConfigDto>
    fun saveConfig(configDto: AppConfigDto): Completable
    fun isValidateDefaultLocation(): Boolean
    fun setValidateDefaultLocation(status: Boolean)
}