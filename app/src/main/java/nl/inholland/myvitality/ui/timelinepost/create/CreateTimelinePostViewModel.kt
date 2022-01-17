package nl.inholland.myvitality.ui.timelinepost.create

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.gunther.bryan.newsreader.utils.SharedPreferenceHelper
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.data.entities.ApiResponse
import nl.inholland.myvitality.data.entities.ResponseStatus
import nl.inholland.myvitality.data.entities.User
import nl.inholland.myvitality.data.entities.requestbody.TimelinePostRequest
import nl.inholland.myvitality.util.RequestUtils
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateTimelinePostViewModel constructor(private val apiClient: ApiClient, private val sharedPrefs: SharedPreferenceHelper) : ViewModel() {

    private val _currentUser = MutableLiveData<User>()
    private val _response = MutableLiveData<ApiResponse>()

    val currentUser: LiveData<User>
        get() = _currentUser

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
                    Log.e("CreateTimelinePostActivity", "onFailure: ", t)
                }
            })
        }
    }

    fun createTimelinePost(message: String, filePart: MultipartBody.Part? = null){
        sharedPrefs.accessToken?.let {
            val messageBody = RequestUtils.createPartFromString(message)

            apiClient.createPost("Bearer $it", messageBody, filePart).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        _response.value = ApiResponse(ResponseStatus.CREATED)
                    } else if (response.code() == 401) {
                        _response.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                    } else {
                        _response.value = ApiResponse(ResponseStatus.API_ERROR)
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    _response.value = ApiResponse(ResponseStatus.API_ERROR)
                    Log.e("CreateTimelinePostActivity", "onFailure: ", t)
                }
            })
        }
    }

    fun createComment(timelinePostId: String, text: String){
        sharedPrefs.accessToken?.let {
            apiClient.createComment("Bearer $it", timelinePostId,  TimelinePostRequest(text)).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        _response.value = ApiResponse(ResponseStatus.CREATED)
                    } else if (response.code() == 401) {
                        _response.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                    } else {
                        _response.value = ApiResponse(ResponseStatus.API_ERROR)
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    _response.value = ApiResponse(ResponseStatus.API_ERROR)
                    Log.e("CreateTimelinePostActivity", "onFailure: ", t)
                }
            })
        }
    }
}