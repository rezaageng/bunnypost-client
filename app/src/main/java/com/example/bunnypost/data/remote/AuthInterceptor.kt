package com.example.bunnypost.data.remote

import com.example.bunnypost.data.local.UserPreferences
import com.example.bunnypost.di.ApplicationScope
import com.example.bunnypost.di.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val userPreferences: UserPreferences,
    private val sessionManager: SessionManager,
    @ApplicationScope private val externalScope: CoroutineScope
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (response.code == 401) {
            externalScope.launch {

                userPreferences.clearUserData()
                sessionManager.triggerLogout()
            }
        }
        return response
    }
}