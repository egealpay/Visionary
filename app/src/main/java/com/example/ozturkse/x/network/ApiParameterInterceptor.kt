package com.example.ozturkse.x.network

import okhttp3.Interceptor
import okhttp3.Response
import java.util.*

class ApiParameterInterceptor : Interceptor {

    private val api_key = "xxxxx"

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url()
        val url = originalUrl.newBuilder()
                .addQueryParameter("api_key", api_key)
                .build()

        val requestBuilder = originalRequest.newBuilder().url(url)
        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}