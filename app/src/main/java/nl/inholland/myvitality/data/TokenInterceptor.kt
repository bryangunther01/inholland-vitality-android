package nl.inholland.myvitality.data

import android.content.Context
import android.content.Intent
import android.util.Log
import nl.gunther.bryan.newsreader.utils.SharedPreferenceHelper
import nl.inholland.myvitality.data.entities.ApiResponse
import nl.inholland.myvitality.data.entities.AuthSettings
import nl.inholland.myvitality.data.entities.ResponseStatus
import nl.inholland.myvitality.data.entities.User
import nl.inholland.myvitality.ui.authentication.login.LoginActivity
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Call
import retrofit2.Callback
import javax.inject.Inject

class TokenInterceptor constructor(val context: Context, private val tokenApiClient: TokenApiClient) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val sharedPrefs = SharedPreferenceHelper(context)
        val currentMillis = System.currentTimeMillis()
        var request = chain.request()

        Log.d("TokenInterceptor", "CurrMillis $currentMillis")
        Log.d("TokenInterceptor", "TokenMillis ${sharedPrefs.tokenExpireTime}")

        if(sharedPrefs.tokenExpireTime < currentMillis){
            sharedPrefs.refreshToken?.let {
                tokenApiClient.refreshToken(it).enqueue(object : Callback<AuthSettings> {
                    override fun onResponse(call: Call<AuthSettings>, response: retrofit2.Response<AuthSettings>) {
                        if(response.isSuccessful && response.body() != null){
                            response.body()?.let { authSettings ->
                                Log.i("TokenInterceptor", "onResponse: new tokens received")
                                sharedPrefs.accessToken = authSettings.accessToken
                                sharedPrefs.refreshToken = authSettings.refreshToken
                                sharedPrefs.tokenExpireTime = authSettings.expiresIn

                                request = request.newBuilder()
                                    .addHeader("Authorization", "Bearer ${authSettings.accessToken}")
                                    .build()
                            }
                        } else if(response.code() == 401){
                            Log.e("TokenInterceptor", "onResponse: 401 unauthorized")
                        }
                    }

                    override fun onFailure(call: Call<AuthSettings>, t: Throwable) {
                        Log.e("TokenInterceptor", "onFailure: ", t)
                    }
                })
            }
        }



        return chain.proceed(request)
    }
}