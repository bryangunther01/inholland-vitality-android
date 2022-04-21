package nl.inholland.myvitality.ui.profile.edit

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.data.TokenApiClient
import nl.inholland.myvitality.data.entities.ApiResponse
import nl.inholland.myvitality.data.entities.Interest
import nl.inholland.myvitality.data.entities.ResponseStatus
import nl.inholland.myvitality.data.entities.User
import nl.inholland.myvitality.util.RequestUtils
import nl.inholland.myvitality.util.SharedPreferenceHelper
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileEditViewModel constructor(
    private val apiClient: ApiClient,
    private val tokenApiClient: TokenApiClient,
    private val sharedPrefs: SharedPreferenceHelper
) : ViewModel() {

    val currentUser: MutableLiveData<User> by lazy {
        MutableLiveData<User>()
    }

    val interests: MutableLiveData<List<Interest>> by lazy {
        MutableLiveData<List<Interest>>()
    }

    val selectedInterests: MutableLiveData<List<Interest>> by lazy {
        MutableLiveData<List<Interest>>()
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
                            selectedInterests.value = user.interests
                            currentUser.value = user

                            sharedPrefs.currentUserId = user.userId
                        }
                    } else if (response.code() == 401) {
                        apiResponse.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    apiResponse.value = ApiResponse(ResponseStatus.API_ERROR)
                    Log.e("ProfileEditActivity", "onFailure: ", t)
                }
            })
        }
    }

    fun deleteAccount() {
        sharedPrefs.accessToken?.let {
            sharedPrefs.pushToken?.let {
                tokenApiClient.deletePushToken("Bearer ${sharedPrefs.accessToken}", it).enqueue(object :
                    Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        Log.i("ProfileEditActivity", "Push token deleted")
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Log.e("ProfileEditActivity", "onFailure: ", t)
                    }
                })
            }
            apiClient.deleteUser("Bearer $it").enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        apiResponse.value = ApiResponse(ResponseStatus.DELETED)
                        sharedPrefs.logoutUser()
                    } else if (response.code() == 401) {
                        apiResponse.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    apiResponse.value = ApiResponse(ResponseStatus.API_ERROR)
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
        interests: String? = null,
        filePart: MultipartBody.Part? = null
    ) {
        sharedPrefs.accessToken?.let {
            apiClient.updateUserProfile(
                "Bearer $it",
                RequestUtils.createPartFromOptionalString(firstName),
                RequestUtils.createPartFromOptionalString(lastName),
                RequestUtils.createPartFromOptionalString(jobTitle),
                RequestUtils.createPartFromOptionalString(location),
                RequestUtils.createPartFromOptionalString(description),
                RequestUtils.createPartFromOptionalString(interests),
                filePart
            ).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        apiResponse.value = ApiResponse(ResponseStatus.UPDATED_VALUE)
                    } else if (response.code() == 401) {
                        apiResponse.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    apiResponse.value = ApiResponse(ResponseStatus.API_ERROR)
                    Log.e("ProfileEditActivity", "onFailure: ", t)
                }
            })
        }
    }

    fun getInterests() {
        sharedPrefs.accessToken?.let {
            apiClient.getInterests("Bearer $it").enqueue(object : Callback<List<Interest>> {
                override fun onResponse(call: Call<List<Interest>>, response: Response<List<Interest>>) {
                    if (response.isSuccessful && response.body() != null) {
                        response.body()?.let { it ->
                            interests.value = it
                        }
                    } else if (response.code() == 401) {
                        apiResponse.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                    }
                }

                override fun onFailure(call: Call<List<Interest>>, t: Throwable) {
                    apiResponse.value = ApiResponse(ResponseStatus.API_ERROR)
                    Log.e("ProfileEditActivity", "onFailure: ", t)
                }
            })
        }
    }

    fun setSelectedInterests(interestIds: List<String>){
        val selectedInterestIds = interests.value?.filter { interestIds.contains(it.interestId) }
        selectedInterests.value = selectedInterestIds
    }
}