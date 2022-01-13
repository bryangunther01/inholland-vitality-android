package nl.inholland.myvitality.ui.profile.edit

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.gunther.bryan.newsreader.utils.SharedPreferenceHelper
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.data.entities.ApiResponse
import nl.inholland.myvitality.data.entities.ResponseStatus
import nl.inholland.myvitality.data.entities.User
import nl.inholland.myvitality.util.RequestUtils
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileEditViewModel constructor(
    private val apiClient: ApiClient,
    private val sharedPrefs: SharedPreferenceHelper
) : ViewModel() {

    private val _currentUser = MutableLiveData<User>()
    private val _responseError = MutableLiveData<ApiResponse>()

    val currentUser: LiveData<User>
        get() = _currentUser

    val apiResponse: LiveData<ApiResponse>
        get() = _responseError

    fun getLoggedInUser() {
        sharedPrefs.accessToken?.let {
            apiClient.getUser("Bearer $it").enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful && response.body() != null) {
                        response.body()?.let { user ->
                            _currentUser.value = user
                            sharedPrefs.currentUserId = user.userId
                        }
                    } else if (response.code() == 401) {
                        _responseError.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    _responseError.value = ApiResponse(ResponseStatus.API_ERROR)
                    Log.e("ProfileEditActivity", "onFailure: ", t)
                }
            })
        }
    }

    fun updateUserProfile(
        firstName: String,
        lastName: String,
        jobTitle: String,
        location: String,
        description: String,
        filePart: MultipartBody.Part? = null
    ) {
        sharedPrefs.accessToken?.let {
            apiClient.updateUserProfile(
                "Bearer $it",
                RequestUtils.createPartFromString(firstName),
                RequestUtils.createPartFromString(lastName),
                RequestUtils.createPartFromString(jobTitle),
                RequestUtils.createPartFromString(location),
                RequestUtils.createPartFromString(description),
                filePart
            ).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        _responseError.value = ApiResponse(ResponseStatus.UPDATED_VALUE)
                    } else if (response.code() == 401) {
                        _responseError.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    _responseError.value = ApiResponse(ResponseStatus.API_ERROR)
                    Log.e("ProfileEditActivity", "onFailure: ", t)
                }
            })
        }
    }
}