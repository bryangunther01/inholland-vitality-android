package nl.inholland.myvitality.ui.timeline.overview

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.data.entities.*
import nl.inholland.myvitality.util.SharedPreferenceHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TimelineOverviewViewModel constructor(private val apiClient: ApiClient, private val sharedPrefs: SharedPreferenceHelper) : ViewModel() {

    val currentUser: MutableLiveData<User> by lazy {
        MutableLiveData<User>()
    }

    val posts: MutableLiveData<List<TimelinePost>> by lazy {
        MutableLiveData<List<TimelinePost>>()
    }

    val apiResponse: MutableLiveData<ApiResponse> by lazy {
        MutableLiveData<ApiResponse>()
    }

    fun getLoggedInUser() {
        sharedPrefs.accessToken?.let {
            apiClient.getUser("Bearer $it").enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful && response.body() != null) {
                        response.body()?.let { user ->
                            currentUser.value = user
                        }
                    } else if (response.code() == 401) {
                        apiResponse.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    apiResponse.value = ApiResponse(ResponseStatus.API_ERROR)
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
                        response.body()?.let { foundPosts ->
                            posts.value = foundPosts
                        }
                    } else if (response.code() == 401) {
                        apiResponse.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                    }
                }

                override fun onFailure(call: Call<List<TimelinePost>>, t: Throwable) {
                    apiResponse.value = ApiResponse(ResponseStatus.API_ERROR)
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
                            apiResponse.value = ApiResponse(ResponseStatus.UPDATED_VALUE)
                        } else if (response.code() == 401) {
                            apiResponse.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        apiResponse.value = ApiResponse(ResponseStatus.API_ERROR)
                        Log.e("TimelineOverviewFragment", "onFailure: ", t)
                    }
                })
            } else {
                apiClient.unlikePost("Bearer $it", timelinePostId).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            apiResponse.value = ApiResponse(ResponseStatus.UPDATED_VALUE)
                        } else if (response.code() == 401) {
                            apiResponse.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        apiResponse.value = ApiResponse(ResponseStatus.API_ERROR)
                        Log.e("TimelineOverviewFragment", "onFailure: ", t)
                    }
                })
            }
        }
    }
}