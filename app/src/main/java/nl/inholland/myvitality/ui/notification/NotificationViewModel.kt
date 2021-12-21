package nl.inholland.myvitality.ui.notification

import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.gunther.bryan.newsreader.utils.SharedPreferenceHelper
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.data.entities.*
import nl.inholland.myvitality.ui.MainActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.stream.Collectors

class NotificationViewModel constructor(
    private val apiClient: ApiClient,
    private val sharedPrefs: SharedPreferenceHelper
) : ViewModel() {

    private val _notifications = MutableLiveData<List<Notification>>()
    private val _response = MutableLiveData<ApiResponse>()

    val notifications: LiveData<List<Notification>>
        get() = _notifications

    val apiResponse: LiveData<ApiResponse>
        get() = _response

    fun getNotifications(limit: Int, offset: Int) {
        sharedPrefs.accessToken?.let {
            apiClient.getNotifications("Bearer $it", limit , offset).enqueue(object : Callback<List<Notification>> {
                override fun onResponse(call: Call<List<Notification>>, response: Response<List<Notification>>) {
                    if (response.isSuccessful && response.body() != null) {
                        response.body()?.let { notifications ->
                            _notifications.value = notifications
                        }
                    } else if (response.code() == 401) {
                        _response.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                    }
                }

                override fun onFailure(call: Call<List<Notification>>, t: Throwable) {
                    _response.value = ApiResponse(ResponseStatus.API_ERROR)
                    Log.e("NotificationActivity", "onFailure: ", t)
                }
            })
        }
    }
}