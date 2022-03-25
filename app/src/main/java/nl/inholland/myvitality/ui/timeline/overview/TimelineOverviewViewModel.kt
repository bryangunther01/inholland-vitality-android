package nl.inholland.myvitality.ui.timeline.overview

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.inholland.myvitality.util.SharedPreferenceHelper
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.data.entities.ApiResponse
import nl.inholland.myvitality.data.entities.ResponseStatus
import nl.inholland.myvitality.data.entities.TimelinePost
import nl.inholland.myvitality.data.entities.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TimelineOverviewViewModel constructor(private val apiClient: ApiClient, private val sharedPrefs: SharedPreferenceHelper) : ViewModel() {

    private val _currentUser = MutableLiveData<User>()
    private val _posts = MutableLiveData<List<TimelinePost>>()
    private val _response = MutableLiveData<ApiResponse>()

    val currentUser: LiveData<User>
        get() = _currentUser

    val posts: LiveData<List<TimelinePost>>
        get() = _posts

    val apiResponse: LiveData<ApiResponse>
        get() = _response

    fun getLoggedInUser() {
        sharedPrefs.accessToken?.let {
            apiClient.getUser("Bearer $it").enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful && response.body() != null) {
                        response.body()?.let { user ->
                            _currentUser.value = user
                        }
                    } else if (response.code() == 401) {
                        _response.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    _response.value = ApiResponse(ResponseStatus.API_ERROR)
                    Log.e("TimelineOverviewFragment", "onFailure: ", t)
                }
            })
        }
    }

    fun getTimelinePosts(limit: Int, offset: Int) {
        sharedPrefs.accessToken?.let {
            apiClient.getTimelinePosts("Bearer $it", limit , offset).enqueue(object : Callback<List<TimelinePost>> {
                override fun onResponse(call: Call<List<TimelinePost>>, response: Response<List<TimelinePost>>) {
                    if (response.isSuccessful && response.body() != null) {
                        response.body()?.let { posts ->
                            _posts.value = posts
                        }
                    } else if (response.code() == 401) {
                        _response.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                    }
                }

                override fun onFailure(call: Call<List<TimelinePost>>, t: Throwable) {
                    _response.value = ApiResponse(ResponseStatus.API_ERROR)
                    Log.e("TimelineOverviewFragment", "onFailure: ", t)
                }
            })
        }
    }

    fun updateLike(timelinePostId: String, liked: Boolean) {
        sharedPrefs.accessToken?.let {

            if (liked) {
                apiClient.likePost("Bearer $it", timelinePostId).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            _response.value = ApiResponse(ResponseStatus.UPDATED_VALUE)
                        } else if (response.code() == 401) {
                            _response.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        _response.value = ApiResponse(ResponseStatus.API_ERROR)
                        Log.e("TimelineOverviewFragment", "onFailure: ", t)
                    }
                })
            } else {
                apiClient.unlikePost("Bearer $it", timelinePostId).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            _response.value = ApiResponse(ResponseStatus.UPDATED_VALUE)
                        } else if (response.code() == 401) {
                            _response.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        _response.value = ApiResponse(ResponseStatus.API_ERROR)
                        Log.e("TimelineOverviewFragment", "onFailure: ", t)
                    }
                })
            }
        }
    }
}