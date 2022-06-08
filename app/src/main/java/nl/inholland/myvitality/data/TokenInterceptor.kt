package nl.inholland.myvitality.data

import android.content.Context
import android.content.Intent
import android.util.Log
import nl.inholland.myvitality.data.entities.AuthSettings
import nl.inholland.myvitality.ui.authentication.login.LoginActivity
import nl.inholland.myvitality.util.SharedPreferenceHelper
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Call
import retrofit2.Callback

class TokenInterceptor constructor(val context: Context, private val tokenApiClient: TokenApiClient) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val sharedPrefs = SharedPreferenceHelper(context)
        val currentMillis = System.currentTimeMillis()
        var request = chain.request()

        if(sharedPrefs.tokenExpireTime < currentMillis){
            sharedPrefs.refreshToken?.let {
                tokenApiClient.refreshToken(it).enqueue(object : Callback<AuthSettings> {
                    override fun onResponse(call: Call<AuthSettings>, response: retrofit2.Response<AuthSettings>) {
                        if(response.isSuccessful && response.body() != null){
                            response.body()?.let { authSettings ->

                                sharedPrefs.accessToken = authSettings.accessToken
                                sharedPrefs.refreshToken = authSettings.refreshToken
                                sharedPrefs.tokenExpireTime = authSettings.expiresIn

                                request = request.newBuilder()
                                    .addHeader("Authorization", "Bearer ${authSettings.accessToken}")
                                    .build()
                            }
                        } else if(response.code() == 401){
                            context.startActivity(Intent(context, LoginActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        }
                    }

                    override fun onFailure(call: Call<AuthSettings>, t: Throwable) {
                        Log.e("TokenInterceptor", "onFailure: ", t)
                    }
                })
            }
        } else {
            val response = chain.proceed(request)

            if(response.code() == 403) {
                context.startActivity(Intent(context, LoginActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }

            return response
        }



        return chain.proceed(request)
    }
}