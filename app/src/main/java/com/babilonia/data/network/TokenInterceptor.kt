package com.babilonia.data.network

import com.babilonia.data.datasource.AuthDataSourceLocal
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

// Created by Anton Yatsenko on 06.06.2019.
class TokenInterceptor @Inject constructor(private val authStorageLocal: AuthDataSourceLocal) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder = request.newBuilder()
        val token = authStorageLocal.getToken()
        if (token != null) {
            builder.header("Authorization", "Token token=$token")
        }
        return chain.proceed(builder.build())
    }
}