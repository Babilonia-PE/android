package com.babilonia.domain.repository

import com.babilonia.domain.model.AppConfig
import com.babilonia.domain.model.User
import com.babilonia.domain.model.enums.LoginStatus
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

// Created by Anton Yatsenko on 27.05.2019.
interface AuthRepository {
    fun authenticate(code: String): Single<User>
    fun getUser(): Flowable<User>
    fun isLoggedIn(): Single<LoginStatus>
    fun uploadUserAvatar(avatar: String, firstName: String, lastName: String, email: String): Single<User>
    fun updateUser(user: User): Single<User>
    fun signOut(): Completable
    fun getAppConfig(): Single<AppConfig>
    fun initAppConfig(): Completable
    fun getUserId(): Single<Long>
}