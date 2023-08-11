package com.babilonia.data.network

import com.babilonia.Constants
import com.babilonia.EmptyConstants
import com.babilonia.data.datasource.AuthDataSourceLocal
import com.babilonia.data.db.model.TokensDto
import com.babilonia.data.network.error.RefreshTokenException
import com.babilonia.data.network.model.RefreshTokenRequest
import com.google.firebase.crashlytics.FirebaseCrashlytics
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

// Created by Anton Yatsenko on 07.06.2019.
class TokenAuthenticator @Inject constructor(
    private val authStorageLocal: AuthDataSourceLocal,
    private val tokenServiceHolder: TokenServiceHolder
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        if (Constants.RETRY_COUNT > 3) {
            Constants.RETRY_COUNT = 0
            FirebaseCrashlytics.getInstance().log("RefreshTokenException()")
            throw RefreshTokenException()
        }
        Constants.RETRY_COUNT += 1
        val refresh = authStorageLocal.getRefresh() ?: EmptyConstants.EMPTY_STRING
        val execute =
            tokenServiceHolder.authService.refreshToken(RefreshTokenRequest(refresh)).execute()
        val data = execute.body()?.data
        data?.tokens?.let {
            val tokenDto = TokensDto(it.authentication, it.exchange)
            authStorageLocal.saveTokens(tokenDto)
        }

        return response.request().newBuilder()
            .header("Authorization", "Token token=" + data?.tokens?.authentication).build()
    }

}