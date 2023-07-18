package com.babilonia.data.network

import okhttp3.Interceptor
import okhttp3.Response
import java.util.*
import javax.inject.Inject

class LocaleInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder = request.newBuilder()
        var language = Locale.getDefault().language
        if (language != "es") {
            language = "en"
        }
        builder.header("Accept-Language", language)
        return chain.proceed(builder.build())
    }
}