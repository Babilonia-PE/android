package com.babilonia.data.network

import com.babilonia.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class TokenInterceptorV2 @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder = request.newBuilder()
        val token   = BuildConfig.PAYMENT_TOKEN
        builder.header("Authorization", "$token")
        return chain.proceed(builder.build())
    }
}