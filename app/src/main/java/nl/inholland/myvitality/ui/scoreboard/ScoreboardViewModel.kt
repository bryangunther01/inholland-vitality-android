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

class ScoreboardViewModel constructor(
    private val apiClient: ApiClient,
    private val sharedPrefs: SharedPreferenceHelper
) : ViewModel() {

    private val _results = MutableLiveData<List<ScoreboardUser>>()
    private val _response = MutableLiveData<ApiResponse>()

    val results: LiveData<List<ScoreboardUser>>
        get() = _results

    val apiResponse: LiveData<ApiResponse>
        get() = _response

    fun getScoreboardItems(limit: Int, offset: Int) {
        sharedPrefs.accessToken?.let {
            apiClient.getScoreboard("Bearer $it", limit , offset).enqueue(object : Callback<List<ScoreboardUser>> {
                override fun onResponse(call: Call<List<ScoreboardUser>>, response: Response<List<ScoreboardUser>>) {
                    if (response.isSuccessful && response.body() != null) {
                        response.body()?.let { users ->
                            _results.value = users
                        }
                    } else if (response.code() == 401) {
                        _response.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                    }
                }

                override fun onFailure(call: Call<List<ScoreboardUser>>, t: Throwable) {
                    _response.value = ApiResponse(ResponseStatus.API_ERROR)
                    Log.e("ScoreboardActivity", "onFailure: ", t)
                }
            })
        }
    }
}