package nl.inholland.myvitality.ui.profile.overview

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

class ProfileViewModel constructor(private val apiClient: ApiClient, private val sharedPrefs: SharedPreferenceHelper) : ViewModel() {

    val currentUser: MutableLiveData<User> by lazy {
        MutableLiveData<User>()
    }

    val isFollowing: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    val currentActivities: MutableLiveData<List<Activity>> by lazy {
        MutableLiveData<List<Activity>>()
    }

    val personalScoreboard: MutableLiveData<List<PersonalScoreboardResult>> by lazy {
        MutableLiveData<List<PersonalScoreboardResult>>()
    }

    val apiResponse: MutableLiveData<ApiResponse> by lazy {
        MutableLiveData<ApiResponse>()
    }

    fun getUser(userId: String?){
        sharedPrefs.accessToken?.let{
            apiClient.getUser("Bearer $it", userId = userId).enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if(response.isSuccessful && response.body() != null){
                        response.body()?.let { user ->
                            currentUser.value = user

                            if(userId != null){
                                isFollowing.value = user.isFollowing
                            }
                        }
                    } else if(response.code() == 401){
                        apiResponse.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    apiResponse.value = ApiResponse(ResponseStatus.API_ERROR)
                    Log.e("ProfileActivity", "onFailure: ", t)
                }
            })
        }
    }

    fun getActivities(userId: String?){
        sharedPrefs.accessToken?.let {
            apiClient.getActivities("Bearer $it", userId = userId, progress = ActivityProgress.IN_PROGRESS.id).enqueue(object : Callback<List<Activity>> {
                override fun onResponse(call: Call<List<Activity>>, response: Response<List<Activity>>) {
                    if(response.isSuccessful && response.body() != null){
                        response.body()?.let { activities ->
                            currentActivities.value = activities
                        }
                    } else if(response.code() == 401){
                        apiResponse.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                    }
                }

                override fun onFailure(call: Call<List<Activity>>, t: Throwable) {
                    apiResponse.value = ApiResponse(ResponseStatus.API_ERROR)
                    Log.e("ProfileActivity", "onFailure: ", t)
                }
            })
        }
    }

    fun getUserScoreboard(userId: String?){
        sharedPrefs.accessToken?.let {
            apiClient.getPersonalScoreboard("Bearer $it", userId = userId).enqueue(object : Callback<List<PersonalScoreboardResult>> {
                override fun onResponse(call: Call<List<PersonalScoreboardResult>>, response: Response<List<PersonalScoreboardResult>>) {
                    if(response.isSuccessful && response.body() != null){
                        response.body()?.let { result ->
                            personalScoreboard.value = result
                        }
                    } else if(response.code() == 401){
                        apiResponse.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                    }
                }

                override fun onFailure(call: Call<List<PersonalScoreboardResult>>, t: Throwable) {
                    apiResponse.value = ApiResponse(ResponseStatus.API_ERROR)
                    Log.e("ProfileActivity", "onFailure: ", t)
                }
            })
        }
    }


    fun followUser(userId: String){
        if(isFollowing.value == null) apiResponse.value = ApiResponse(ResponseStatus.API_ERROR)

        sharedPrefs.accessToken?.let {
            apiClient.followUser("Bearer $it", userId).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if(response.isSuccessful){
                        isFollowing.value = true
                    } else if(response.code() == 401){
                        apiResponse.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    apiResponse.value = ApiResponse(ResponseStatus.API_ERROR)
                    Log.e("ProfileActivity", "onFailure: ", t)
                }
            })
        }
    }

    fun unfollowUser(userId: String){
        if(isFollowing.value == null) apiResponse.value = ApiResponse(ResponseStatus.API_ERROR)

        sharedPrefs.accessToken?.let {
            apiClient.unfollowUser("Bearer $it", userId).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if(response.isSuccessful){
                        isFollowing.value = false
                    } else if(response.code() == 401){
                        apiResponse.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    apiResponse.value = ApiResponse(ResponseStatus.API_ERROR)
                    Log.e("ProfileActivity", "onFailure: ", t)
                }
            })
        }
    }
}