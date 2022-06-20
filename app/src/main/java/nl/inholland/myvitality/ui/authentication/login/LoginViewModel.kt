package nl.inholland.myvitality.ui.authentication.login

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.data.TokenApiClient
import nl.inholland.myvitality.data.entities.ApiResponse
import nl.inholland.myvitality.data.entities.AuthSettings
import nl.inholland.myvitality.data.entities.ResponseStatus
import nl.inholland.myvitality.data.entities.requestbody.AuthRequest
import nl.inholland.myvitality.data.entities.requestbody.PushToken
import nl.inholland.myvitality.data.entities.requestbody.RegisterRequest
import nl.inholland.myvitality.util.SharedPreferenceHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel constructor(private val apiClient: ApiClient, private val tokenApiClient: TokenApiClient, private val sharedPrefs: SharedPreferenceHelper) : ViewModel() {

    val apiResponse: MutableLiveData<ApiResponse> by lazy {
        MutableLiveData<ApiResponse>()
    }

    fun login(email: String, azureId: String, accessToken: String, firstName: String, lastName: String) {
        tokenApiClient.login(AuthRequest(email, azureId, accessToken))
            .enqueue(object : Callback<AuthSettings> {
                override fun onResponse(
                    call: Call<AuthSettings>,
                    response: Response<AuthSettings>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        response.body()?.let {
                            sharedPrefs.accessToken = it.accessToken
                            sharedPrefs.refreshToken = it.refreshToken
                            sharedPrefs.tokenExpireTime = it.expiresIn

                            if(it.registered) {
                                sharedPrefs.recentlyRegistered = true
                                sharedPrefs.userFirstname = firstName
                                sharedPrefs.userLastname = lastName
                            }

                            apiResponse.value = ApiResponse(ResponseStatus.SUCCESSFUL)
                        }
                    } else if (response.code() == 401) {
                        apiResponse.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                    }
                }

                override fun onFailure(call: Call<AuthSettings>, t: Throwable) {
                    apiResponse.value = ApiResponse(ResponseStatus.API_ERROR)
                    Log.e("LoginActivity", "onFailure: ", t)
                }
            })
    }

    fun registerPushToken() {
        sharedPrefs.accessToken?.let {
            apiClient.createPushToken("Bearer $it", PushToken(sharedPrefs.pushToken!!))
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            apiResponse.value = ApiResponse(ResponseStatus.SUCCESSFUL)
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Log.e("LoginActivity", "onFailure: ", t)
                    }
                })
        }
    }
}
