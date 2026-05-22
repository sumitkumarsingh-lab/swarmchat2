package com.example.data.api

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Global dynamic network configuration for the Telco Swarm frontend.
 * Provides on-the-fly Base URL swaps so developers can test against different HTTP servers.
 */
object ApiConfig {
    // Linked directly to our permanent cloud server gateway
    var baseUrl: String = "https://telco-swarm-backend.onrender.com/"
        set(value) {
            val trimmed = value.trim()
            val formatted = if (trimmed.endsWith("/")) trimmed else "$trimmed/"
            field = formatted
        }
}

/**
 * An OkHttp Interceptor that redirects ongoing requests to the current address
 * stored in [ApiConfig.baseUrl] at runtime.
 */
class DynamicBaseUrlInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val currentBaseUrl = ApiConfig.baseUrl.toHttpUrlOrNull()
        if (currentBaseUrl != null) {
            val newUrl = request.url.newBuilder()
                .scheme(currentBaseUrl.scheme)
                .host(currentBaseUrl.host)
                .port(currentBaseUrl.port)
                .build()
            request = request.newBuilder()
                .url(newUrl)
                .build()
        }
        return chain.proceed(request)
    }
}
