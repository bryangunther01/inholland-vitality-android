package nl.inholland.myvitality.ui.timelinepost.view

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.data.entities.ApiResponse
import nl.inholland.myvitality.data.entities.Comment
import nl.inholland.myvitality.data.entities.ResponseStatus
import nl.inholland.myvitality.data.entities.TimelinePost
import nl.inholland.myvitality.util.SharedPreferenceHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TimelinePostViewModel constructor(
    private val apiClient: ApiClient,
    private val sharedPrefs: SharedPreferenceHelper
) : ViewModel() {

    val post: MutableLiveData<TimelinePost> by lazy {
        MutableLiveData<TimelinePost>()
    }

    val isLiked: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    val likedCount: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    val comments: MutableLiveData<List<Comment>> by lazy {
        MutableLiveData<List<Comment>>()
    }

    val apiResponse: MutableLiveData<ApiResponse> by lazy {
        MutableLiveData<ApiResponse>()
    }

    fun getPost(timelinePostId: String) {
        sharedPrefs.accessToken?.let {
            apiClient.getTimelinePost("Bearer $it", timelinePostId)
                .enqueue(object : Callback<TimelinePost> {
                    override fun onResponse(
                        call: Call<TimelinePost>,
                        response: Response<TimelinePost>
                    ) {
                        if(response.body() == null) apiResponse.value = ApiResponse(ResponseStatus.NOT_FOUND)

                        if (response.isSuccessful && response.body() != null) {
                            response.body()?.let { foundPost ->
                                post.value = foundPost
                                isLiked.value = foundPost.iLikedPost
                                likedCount.value = foundPost.countOfLikes
                            }
                        } else if (response.code() == 401) {
                            apiResponse.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                        }
                    }

                    override fun onFailure(call: Call<TimelinePost>, t: Throwable) {
                        apiResponse.value = ApiResponse(ResponseStatus.API_ERROR)
                        Log.e("TimelinePostActivity", "onFailure: ", t)
                    }
                })
        }
    }

    fun getComments(timelinePostId: String, limit: Int, offset: Int) {
        sharedPrefs.accessToken?.let {
            apiClient.getTimelinePostComments("Bearer $it", timelinePostId, limit, offset)
                .enqueue(object : Callback<List<Comment>> {
                    override fun onResponse(
                        call: Call<List<Comment>>,
                        response: Response<List<Comment>>
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            response.body()?.let { foundComments ->
                                comments.value = foundComments
                            }
                        } else if (response.code() == 401) {
                            apiResponse.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                        }
                    }

                    override fun onFailure(call: Call<List<Comment>>, t: Throwable) {
                        apiResponse.value = ApiResponse(ResponseStatus.API_ERROR)
                        Log.e("TimelinePostActivity", "onFailure: ", t)
                    }
                })
        }
    }

    fun updateLike(timelinePostId: String) {
        sharedPrefs.accessToken?.let {
            val liked = isLiked.value ?: false

            if (!liked) {
                apiClient.likePost("Bearer $it", timelinePostId).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            isLiked.value = true
                            likedCount.value?.let { count ->
                                likedCount.value = count+1
                            }
                        } else if (response.code() == 401) {
                            apiResponse.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        apiResponse.value = ApiResponse(ResponseStatus.API_ERROR)
                        Log.e("TimelinePostActivity", "onFailure: ", t)
                    }
                })
            } else {
                apiClient.unlikePost("Bearer $it", timelinePostId).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            isLiked.value = false
                            likedCount.value?.let { count ->
                                likedCount.value = count-1
                            }
                        } else if (response.code() == 401) {
                            apiResponse.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        apiResponse.value = ApiResponse(ResponseStatus.API_ERROR)
                        Log.e("TimelinePostActivity", "onFailure: ", t)
                    }
                })
            }
        }
    }

    fun deletePost(timelinePostId: String){
        sharedPrefs.accessToken?.let {
            apiClient.deletePost("Bearer $it", timelinePostId).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        apiResponse.value = ApiResponse(ResponseStatus.DELETED)
                    } else if (response.code() == 401) {
                        apiResponse.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    apiResponse.value = ApiResponse(ResponseStatus.API_ERROR)
                    Log.e("CreateTimelinePostActivity", "onFailure: ", t)
                }
            })
        }
    }
}