package nl.inholland.myvitality.data

import android.content.Context
import android.content.Intent
import android.util.Log
import nl.gunther.bryan.newsreader.utils.SharedPreferenceHelper
import nl.inholland.myvitality.ui.authentication.login.LoginActivity
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject

// JEROEN: Hoe moet ik hier mijn ApiClient injecten?
class AuthorizationInterceptor (val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val sharedPrefs = SharedPreferenceHelper(context)
        val currentMillis = System.currentTimeMillis()

        if(sharedPrefs.tokenExpireTime < currentMillis){
            Log.d("TEST123", "EXPIREDDD")
        } else {
            Log.d("TEST123", "NOT EXPIRED")
        }

        val request = chain.request()
        val response = chain.proceed(request)

        if(response.code() == 401){
            context.startActivity(Intent(context, LoginActivity::class.java))
            //  TODO: Refresh token instead of clearing
        }

        return response
    }
}