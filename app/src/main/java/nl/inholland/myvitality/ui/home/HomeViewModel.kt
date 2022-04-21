package nl.inholland.myvitality.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.data.entities.ActivityCategory
import nl.inholland.myvitality.data.entities.ApiResponse
import nl.inholland.myvitality.data.entities.ResponseStatus
import nl.inholland.myvitality.data.entities.User
import nl.inholland.myvitality.util.SharedPreferenceHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeViewModel constructor(private val apiClient: ApiClient, private val sharedPrefs: SharedPreferenceHelper) : ViewModel() {

    val currentUser: MutableLiveData<User> by lazy {
        MutableLiveData<User>()
    }

    val categories: MutableLiveData<List<ActivityCategory>> by lazy {
        MutableLiveData<List<ActivityCategory>>()
    }

    val apiResponse: MutableLiveData<ApiResponse> by lazy {
        MutableLiveData<ApiResponse>()
    }

    fun getLoggedInUser(){
        sharedPrefs.accessToken?.let{
            apiClient.getUser("Bearer $it").enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if(response.isSuccessful && response.body() != null){
                        response.body()?.let { user ->
                            currentUser.value = user
                            sharedPrefs.currentUserId = user.userId
                        }
                    } else if(response.code() == 401){
                        apiResponse.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    apiResponse.value = ApiResponse(ResponseStatus.API_ERROR)
                    Log.e("HomeFragment", "onFailure: ", t)
                }
            })
        }
    }

    fun getActivityCategories(){
        sharedPrefs.accessToken?.let {
            apiClient.getActivityCategories("Bearer $it").enqueue(object : Callback<List<ActivityCategory>> {
                override fun onResponse(call: Call<List<ActivityCategory>>, response: Response<List<ActivityCategory>>) {
                    if(response.isSuccessful && response.body() != null){
                        response.body()?.let { foundCategories ->
                            categories.value = foundCategories
                        }
                    } else if(response.code() == 401){
                        apiResponse.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                    }
                }

                override fun onFailure(call: Call<List<ActivityCategory>>, t: Throwable) {
                    apiResponse.value = ApiResponse(ResponseStatus.API_ERROR)
                    Log.e("HomeFragment", "onFailure: ", t)
                }
            })
        }
    }
}