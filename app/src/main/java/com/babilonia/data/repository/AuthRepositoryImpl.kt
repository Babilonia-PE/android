package com.babilonia.data.repository

import com.babilonia.data.datasource.AuthDataSourceLocal
import com.babilonia.data.datasource.AuthDataSourceRemote
import com.babilonia.data.db.model.TokensDto
import com.babilonia.data.mapper.ConfigMapper
import com.babilonia.data.mapper.UserMapper
import com.babilonia.data.network.error.mapErrors
import com.babilonia.data.network.error.mapNetworkErrors
import com.babilonia.data.network.model.AuthRequest
import com.babilonia.data.network.model.json.UpdateUserJson
import com.babilonia.domain.model.AppConfig
import com.babilonia.domain.model.User
import com.babilonia.domain.model.enums.LoginStatus
import com.babilonia.domain.repository.AuthRepository
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import javax.inject.Inject

// Created by Anton Yatsenko on 27.05.2019.
class AuthRepositoryImpl @Inject constructor(
    private val authDataSourceRemote: AuthDataSourceRemote,
    private val authDataSourceLocal: AuthDataSourceLocal,
    private val userMapper: UserMapper,
    private val configMapper: ConfigMapper
) :
    AuthRepository {
    override fun getAppConfig(): Single<AppConfig> {
        return authDataSourceLocal.getAppConfig()
            .map { configMapper.mapLocalToDomain(it) }
    }

    override fun initAppConfig(): Completable {
        return authDataSourceRemote.getAppConfig()
            .flatMapCompletable { authDataSourceLocal.saveConfig(configMapper.mapRemoteToLocal(it)) }
    }

    override fun signOut(): Completable {
        return authDataSourceLocal.signOut()
    }

    override fun updateUser(user: User): Single<User> {
        val userJson = UpdateUserJson(user.phoneNumber, user.firstName, user.lastName, user.email)
        return authDataSourceRemote.updateUser(userJson)
            .mapNetworkErrors()
            .mapErrors()
            .doOnSuccess { authDataSourceLocal.saveUser(userMapper.mapRemoteToLocal(it)) }
            .map { userMapper.mapRemoteToDomain(it) }
    }

    override fun uploadUserAvatar(
        avatar: String, firstName: String, lastName: String, email: String
    ): Single<User> {
        return authDataSourceRemote.uploadUserAvatar(avatar, firstName, lastName, email)
            .mapNetworkErrors()
            .mapErrors()
            .doOnSuccess { authDataSourceLocal.saveUser(userMapper.mapRemoteToLocal(it)) }
            .map { userMapper.mapRemoteToDomain(it) }
    }

    override fun getUser(): Flowable<User> {
        val remote = authDataSourceRemote.getUser()
            .doOnSuccess { authDataSourceLocal.saveUser(userMapper.mapRemoteToLocal(it)) }
            .map { userMapper.mapRemoteToDomain(it) }
        val local =
            authDataSourceLocal.getUser().map { userMapper.mapLocalToDomain(it) }
        return Single.mergeDelayError(local, remote)
            .mapNetworkErrors()
            .mapErrors()
    }

    override fun authenticate(code: String): Single<User> {
        return authDataSourceRemote.authenticate(AuthRequest(code))
            .doOnSuccess {
                val tokens = it.tokens
                val tokenDto = TokensDto(tokens.authentication, tokens.exchange)
                authDataSourceLocal.saveTokens(tokenDto)
                authDataSourceLocal.saveUser(userMapper.mapRemoteToLocal(it.user))
            }
            .mapNetworkErrors()
            .mapErrors()
            .map { userMapper.mapRemoteToDomain(it.user) }

    }

    override fun isLoggedIn(): Single<LoginStatus> {
        return authDataSourceLocal.isLoggedIn()
    }

    override fun getUserId(): Single<Long> {
        return authDataSourceLocal.getUser().map { it.id }
    }
}