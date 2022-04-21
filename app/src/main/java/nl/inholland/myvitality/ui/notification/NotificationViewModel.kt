package nl.inholland.myvitality.ui.notification

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.data.entities.ApiResponse
import nl.inholland.myvitality.data.entities.Notification
import nl.inholland.myvitality.data.entities.ResponseStatus
import nl.inholland.myvitality.util.SharedPreferenceHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationViewModel constructor(private val apiClient: ApiClient, private val sharedPrefs: SharedPreferenceHelper) : ViewModel() {

    val notifications: MutableLiveData<List<Notification>> by lazy {
        MutableLiveData<List<Notification>>()
    }

    val apiResponse: MutableLiveData<ApiResponse> by lazy {
        MutableLiveData<ApiResponse>()
    }

    fun getNotifications(limit: Int, offset: Int) {
        sharedPrefs.accessToken?.let {
            apiClient.getNotifications("Bearer $it", limit , offset).enqueue(object : Callback<List<Notification>> {
                override fun onResponse(call: Call<List<Notification>>, response: Response<List<Notification>>) {
                    if (response.isSuccessful && response.body() != null) {
                        response.body()?.let { foundNotifications ->
                            notifications.value = foundNotifications
                        }
                    } else if (response.code() == 401) {
                        apiResponse.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                    }
                }

                override fun onFailure(call: Call<List<Notification>>, t: Throwable) {
                    apiResponse.value = ApiResponse(ResponseStatus.API_ERROR)
                    Log.e("NotificationActivity", "onFailure: ", t)
                }
            })
        }
    }
}