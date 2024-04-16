package com.babilonia.data.repository

import com.babilonia.data.datasource.AuthDataSourceLocal
import com.babilonia.data.datasource.AuthDataSourceRemote
import com.babilonia.data.db.model.TokensDto
import com.babilonia.data.mapper.ConfigMapper
import com.babilonia.data.mapper.ListingImageMapper
import com.babilonia.data.mapper.UserMapper
import com.babilonia.data.network.error.mapErrors
import com.babilonia.data.network.error.mapNetworkErrors
import com.babilonia.data.network.model.AuthRequest
import com.babilonia.data.network.model.BaseResponse
import com.babilonia.data.network.model.json.PaisPrefixJson
import com.babilonia.domain.model.*
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
    private val configMapper: ConfigMapper,
    private val listingImageMapper: ListingImageMapper
) :
    AuthRepository {
    override fun getAppConfig(): Single<AppConfig> {
        return authDataSourceLocal.getAppConfig()
            .map { configMapper.mapLocalToDomain(it) }
    }

    override fun initAppConfig(version: Int): Completable {
        return authDataSourceRemote.getAppConfig(version)
            .flatMapCompletable { authDataSourceLocal.saveConfig(configMapper.mapRemoteToLocal(it)) }
    }

    override fun signOut(): Completable {
        return authDataSourceLocal.signOut()
    }

    override fun deleteAccount(): Single<BaseResponse<Any>> {
        return authDataSourceRemote.deleteAccount()
    }

    override fun updateUser(user: User, password: String?, prefix: String?, photoId: Int?): Single<User> {
        return authDataSourceRemote.updateUser(
            user.fullName ?: "",
            user.email ?: "",
            user.phoneNumber ?: "",
            prefix.toString(),
            password,
            photoId
        )
            .mapNetworkErrors()
            .mapErrors()
            .doOnSuccess { authDataSourceLocal.saveUser(userMapper.mapRemoteToLocal(it)) }
            .map { userMapper.mapRemoteToDomain(it) }
    }

    override fun uploadImages(
        image: String, type: String
    ): Single<List<ListingImage>> {
        return authDataSourceRemote.uploadImages(image, type)
            .mapNetworkErrors()
            .mapErrors()
            //.doOnSuccess { authDataSourceLocal.saveUser(userMapper.mapRemoteToLocal(it)) }
            .map { it.map { listingImageMapper.mapRemoteToDomain(it) } }
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

    override fun getRemoteUser(): Single<User> {
        return authDataSourceRemote.getUser()
            .mapNetworkErrors()
            .mapErrors()
            .doOnSuccess { authDataSourceLocal.saveUser(userMapper.mapRemoteToLocal(it)) }
            .map { userMapper.mapRemoteToDomain(it) }
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

    override fun signUp(signUp: SignUp): Single<User> {
        return authDataSourceRemote.signUp(
            signUp.fullName,
            signUp.email,
            signUp.password,
            signUp.prefix,
            signUp.phoneNumber,
            signUp.ipa,
            signUp.ua,
            signUp.sip
        )
            .doOnSuccess {
                val user = User(
                    it.userId,
                    "",
                    signUp.fullName,
                    null,
                    signUp.email
                )
                val tokenDto = TokensDto(it.authorization, "")
                authDataSourceLocal.saveTokens(tokenDto)
                authDataSourceLocal.saveUser(userMapper.mapDomainToLocal(user))
            }
            .mapNetworkErrors()
            .mapErrors()
            .map {
                User(
                    it.userId,
                    signUp.phoneNumber,
                    signUp.fullName,
                    null,
                    signUp.email
                )
            }
    }

    override fun getListPaisPrefix(): Single<List<PaisPrefixJson>> {
        return authDataSourceRemote.getListPaisPrefix()
            .mapNetworkErrors()
            .mapErrors()
    }

    override fun logIn(logIn: LogIn): Single<User> {
        return authDataSourceRemote.logIn(
            logIn.email,
            logIn.password,
            logIn.ipa,
            logIn.ua,
            logIn.sip
        )
            .doOnSuccess {
                val user = User(
                    it.tokens.userId,
                    null,
                    "",
                    null,
                    logIn.email
                )
                val tokenDto = TokensDto("${it.tokens.type} ${it.tokens.authentication}", "")
                authDataSourceLocal.saveTokens(tokenDto)
                authDataSourceLocal.saveUser(userMapper.mapDomainToLocal(user))
            }
            .mapNetworkErrors()
            .mapErrors()
            .map {
                User(
                    it.tokens.userId,
                    null,
                    "",
                    null,
                    logIn.email
                )
            }
    }

    override fun isLoggedIn(): Single<LoginStatus> {
        return authDataSourceLocal.isLoggedIn()
    }

    override fun getUserId(): Single<Long> {
        return authDataSourceLocal.getUser().map { it.id }
    }
}