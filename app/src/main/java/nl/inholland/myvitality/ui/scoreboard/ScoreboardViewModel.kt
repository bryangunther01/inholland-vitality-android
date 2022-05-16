package nl.inholland.myvitality.ui.notification

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.data.entities.ApiResponse
import nl.inholland.myvitality.data.entities.ResponseStatus
import nl.inholland.myvitality.data.entities.ScoreboardUser
import nl.inholland.myvitality.util.SharedPreferenceHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ScoreboardViewModel constructor(
    private val apiClient: ApiClient,
    private val sharedPrefs: SharedPreferenceHelper
) : ViewModel() {

    val results: MutableLiveData<List<ScoreboardUser>> by lazy {
        MutableLiveData<List<ScoreboardUser>>()
    }

    val apiResponse: MutableLiveData<ApiResponse> by lazy {
        MutableLiveData<ApiResponse>()
    }

    fun getScoreboardItems(limit: Int, offset: Int) {
        sharedPrefs.accessToken?.let {
            apiClient.getScoreboard("Bearer $it", limit , offset).enqueue(object : Callback<List<ScoreboardUser>> {
                override fun onResponse(call: Call<List<ScoreboardUser>>, response: Response<List<ScoreboardUser>>) {
                    if (response.isSuccessful && response.body() != null) {
                        response.body()?.let { users ->
                            results.value = users
                        }
                    } else if (response.code() == 401) {
                        apiResponse.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                    }
                }

                override fun onFailure(call: Call<List<ScoreboardUser>>, t: Throwable) {
                    apiResponse.value = ApiResponse(ResponseStatus.API_ERROR)
                    Log.e("ScoreboardActivity", "onFailure: ", t)
                }
            })
        }
    }
}