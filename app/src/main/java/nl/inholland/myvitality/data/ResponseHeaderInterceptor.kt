package nl.inholland.myvitality.data

import android.content.Context
import android.content.Intent
import nl.gunther.bryan.newsreader.utils.SharedPreferenceHelper
import nl.inholland.myvitality.ui.authentication.login.LoginActivity
import okhttp3.Interceptor
import okhttp3.Response

class ResponseHeaderInterceptor(val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if(response.code() == 401){
            //  TODO: Refresh token instead of clearing
            val sharedPrefs = SharedPreferenceHelper(context)
            sharedPrefs.accessToken = null
            sharedPrefs.userFullName = null
            sharedPrefs.userProfileImageUrl = null
        }

        return response
    }
}