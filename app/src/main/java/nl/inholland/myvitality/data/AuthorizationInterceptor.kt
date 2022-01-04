package nl.inholland.myvitality.data

import android.content.Context
import android.content.Intent
import nl.gunther.bryan.newsreader.utils.SharedPreferenceHelper
import nl.inholland.myvitality.ui.authentication.login.LoginActivity
import okhttp3.Interceptor
import okhttp3.Response

class AuthorizationInterceptor(val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val sharedPrefs = SharedPreferenceHelper(context)
        val currentMillis = System.currentTimeMillis()

        if(sharedPrefs.tokenExpireTime < currentMillis){

        }

        val request = chain.request()
        val response = chain.proceed(request)

        if(response.code() == 401){
            //  TODO: Refresh token instead of clearing

            sharedPrefs.accessToken = null
            sharedPrefs.userFullName = null
            sharedPrefs.userProfileImageUrl = null
        }

        return response
    }
}