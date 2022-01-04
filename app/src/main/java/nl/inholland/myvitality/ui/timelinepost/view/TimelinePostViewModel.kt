package nl.inholland.myvitality.ui.timelinepost.view

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

class TimelinePostViewModel constructor(
    private val apiClient: ApiClient,
    private val sharedPrefs: SharedPreferenceHelper
) : ViewModel() {

    private val _post = MutableLiveData<TimelinePost>()
    private val _isLiked = MutableLiveData<Boolean>()
    private val _likedCount = MutableLiveData<Int>()
    private val _comments = MutableLiveData<List<Comment>>()
    private val _response = MutableLiveData<ApiResponse>()

    val post: LiveData<TimelinePost>
        get() = _post

    val isLiked: LiveData<Boolean>
        get() = _isLiked

    val likedCount: LiveData<Int>
        get() = _likedCount

    val comments: LiveData<List<Comment>>
        get() = _comments

    val apiResponse: LiveData<ApiResponse>
        get() = _response

    fun getPost(timelinePostId: String) {
        sharedPrefs.accessToken?.let {
            apiClient.getTimelinePost("Bearer $it", timelinePostId)
                .enqueue(object : Callback<TimelinePost> {
                    override fun onResponse(
                        call: Call<TimelinePost>,
                        response: Response<TimelinePost>
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            response.body()?.let { post ->
                                _post.value = post
                                _isLiked.value = post.iLikedPost
                                _likedCount.value = post.countOfLikes
                            }
                        } else if (response.code() == 401) {
                            _response.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                        }
                    }

                    override fun onFailure(call: Call<TimelinePost>, t: Throwable) {
                        _response.value = ApiResponse(ResponseStatus.API_ERROR)
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
                            response.body()?.let { comments ->
                                _comments.value = comments
                            }
                        } else if (response.code() == 401) {
                            _response.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                        }
                    }

                    override fun onFailure(call: Call<List<Comment>>, t: Throwable) {
                        _response.value = ApiResponse(ResponseStatus.API_ERROR)
                        Log.e("TimelinePostActivity", "onFailure: ", t)
                    }
                })
        }
    }

    fun updateLike(timelinePostId: String) {
        sharedPrefs.accessToken?.let {
            val liked = _isLiked.value ?: false

            if (!liked) {
                apiClient.likePost("Bearer $it", timelinePostId).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            _isLiked.value = true
                            _likedCount.value?.let { count ->
                                _likedCount.value = count+1
                            }
                        } else if (response.code() == 401) {
                            _response.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        _response.value = ApiResponse(ResponseStatus.API_ERROR)
                        Log.e("TimelinePostActivity", "onFailure: ", t)
                    }
                })
            } else {
                apiClient.unlikePost("Bearer $it", timelinePostId).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            _isLiked.value = false
                            _likedCount.value?.let { count ->
                                _likedCount.value = count-1
                            }
                        } else if (response.code() == 401) {
                            _response.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        _response.value = ApiResponse(ResponseStatus.API_ERROR)
                        Log.e("TimelinePostActivity", "onFailure: ", t)
                    }
                })
            }
        }
    }
}