package nl.inholland.myvitality.ui.timeline.liked

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.gunther.bryan.newsreader.utils.SharedPreferenceHelper
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.data.entities.ApiResponse
import nl.inholland.myvitality.data.entities.ResponseStatus
import nl.inholland.myvitality.data.entities.SimpleUser
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TimelineLikedViewModel constructor(
    private val apiClient: ApiClient,
    private val sharedPrefs: SharedPreferenceHelper
) : ViewModel() {

    private val _results = MutableLiveData<List<SimpleUser>>()
    private val _response = MutableLiveData<ApiResponse>()

    val results: LiveData<List<SimpleUser>>
        get() = _results

    val apiResponse: LiveData<ApiResponse>
        get() = _response

    fun search(timelinePostId: String, limit: Int, offset: Int) {
        sharedPrefs.accessToken?.let {
            apiClient.getTimelinePostLikes("Bearer $it", timelinePostId, limit , offset).enqueue(object : Callback<List<SimpleUser>> {
                override fun onResponse(call: Call<List<SimpleUser>>, response: Response<List<SimpleUser>>) {
                    if (response.isSuccessful && response.body() != null) {
                        response.body()?.let { users ->
                            _results.value = users
                        }
                    } else if (response.code() == 401) {
                        _response.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                    }
                }

                override fun onFailure(call: Call<List<SimpleUser>>, t: Throwable) {
                    _response.value = ApiResponse(ResponseStatus.API_ERROR)
                    Log.e("HomeFragment", "onFailure: ", t)
                }
            })
        }
    }
}