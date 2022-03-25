package nl.inholland.myvitality.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.inholland.myvitality.util.SharedPreferenceHelper
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.data.entities.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.stream.Collectors

class HomeViewModel constructor(private val apiClient: ApiClient, private val sharedPrefs: SharedPreferenceHelper) : ViewModel() {

    private val _currentUser = MutableLiveData<User>()
    private val _categories = MutableLiveData<List<ActivityCategory>>()
    private val _responseError = MutableLiveData<ApiResponse>()

    val currentUser: LiveData<User>
        get() = _currentUser

    val categories: LiveData<List<ActivityCategory>>
        get() = _categories

    val apiResponse: LiveData<ApiResponse>
        get() = _responseError

    fun getLoggedInUser(){
        sharedPrefs.accessToken?.let{
            apiClient.getUser("Bearer $it").enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if(response.isSuccessful && response.body() != null){
                        response.body()?.let { user ->
                            _currentUser.value = user
                            sharedPrefs.currentUserId = user.userId
                        }
                    } else if(response.code() == 401){
                        _responseError.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    _responseError.value = ApiResponse(ResponseStatus.API_ERROR)
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
                        response.body()?.let { categories ->
                            _categories.value = categories
                        }
                    } else if(response.code() == 401){
                        _responseError.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                    }
                }

                override fun onFailure(call: Call<List<ActivityCategory>>, t: Throwable) {
                    _responseError.value = ApiResponse(ResponseStatus.API_ERROR)
                    Log.e("HomeFragment", "onFailure: ", t)
                }
            })
        }
    }
}