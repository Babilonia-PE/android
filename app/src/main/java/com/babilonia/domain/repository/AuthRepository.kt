package com.babilonia.domain.repository

import com.babilonia.data.network.model.BaseResponse
import com.babilonia.data.network.model.json.PaisPrefixJson
import com.babilonia.domain.model.*
import com.babilonia.domain.model.enums.LoginStatus
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

// Created by Anton Yatsenko on 27.05.2019.
interface AuthRepository {
    fun signUp(signUp: SignUp): Single<User>
    fun getListPaisPrefix(): Single<List<PaisPrefixJson>>
    fun logIn(logIn: LogIn): Single<User>
    fun authenticate(code: String): Single<User>
    fun getUser(): Flowable<User>
    fun getRemoteUser(): Single<User>
    fun isLoggedIn(): Single<LoginStatus>
    fun uploadImages(image: String, type: String): Single<List<ListingImage>>
    fun updateUser(user: User, password: String?, prefix: String?, photoId: Int?): Single<User>
    fun signOut(): Completable
    fun getAppConfig(): Single<AppConfig>
    fun initAppConfig(version: Int): Completable
    fun getUserId(): Single<Long>
    fun deleteAccount(): Single<BaseResponse<Any>>
}