package com.example.bunnypost.data.remote

import com.example.bunnypost.data.local.UserPreferences
import com.example.bunnypost.di.SessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val userPreferences: UserPreferences,
    private val sessionManager: SessionManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (response.code == 401) {
            // Use runBlocking as Interceptor functions are not suspendable
            runBlocking {
                userPreferences.clearUserToken()
                sessionManager.triggerLogout()
            }
        }
        return response
    }
}