package nl.inholland.myvitality.ui.profile.overview

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

class ProfileViewModel constructor(private val apiClient: ApiClient, private val sharedPrefs: SharedPreferenceHelper) : ViewModel() {

    private val _currentUser = MutableLiveData<User>()
    private val _isFollowing = MutableLiveData<Boolean>()
    private val _currentChallenges = MutableLiveData<List<Activity>>()
    private val _personalScoreboard = MutableLiveData<List<PersonalScoreboardResult>>()
    private val _responseError = MutableLiveData<ApiResponse>()

    val currentUser: LiveData<User>
        get() = _currentUser

    val isFollowing: LiveData<Boolean>
        get() = _isFollowing

    val currentActivities: LiveData<List<Activity>>
        get() = _currentChallenges

    val personalScoreboard: LiveData<List<PersonalScoreboardResult>>
        get() = _personalScoreboard

    val apiResponse: LiveData<ApiResponse>
        get() = _responseError

    fun getUser(userId: String?){
        sharedPrefs.accessToken?.let{
            apiClient.getUser("Bearer $it", userId = userId).enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if(response.isSuccessful && response.body() != null){
                        response.body()?.let { user ->
                            _currentUser.value = user

                            if(userId != null){
                                _isFollowing.value = user.isFollowing
                            }
                        }
                    } else if(response.code() == 401){
                        _responseError.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    _responseError.value = ApiResponse(ResponseStatus.API_ERROR)
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
                            _currentChallenges.value = activities
                        }
                    } else if(response.code() == 401){
                        _responseError.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                    }
                }

                override fun onFailure(call: Call<List<Activity>>, t: Throwable) {
                    _responseError.value = ApiResponse(ResponseStatus.API_ERROR)
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
                            _personalScoreboard.value = result
                        }
                    } else if(response.code() == 401){
                        _responseError.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                    }
                }

                override fun onFailure(call: Call<List<PersonalScoreboardResult>>, t: Throwable) {
                    _responseError.value = ApiResponse(ResponseStatus.API_ERROR)
                    Log.e("ProfileActivity", "onFailure: ", t)
                }
            })
        }
    }


    fun followUser(userId: String){
        if(isFollowing.value == null) _responseError.value = ApiResponse(ResponseStatus.API_ERROR)

        sharedPrefs.accessToken?.let {
            apiClient.followUser("Bearer $it", userId).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if(response.isSuccessful){
                        _isFollowing.value = true
                    } else if(response.code() == 401){
                        _responseError.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    _responseError.value = ApiResponse(ResponseStatus.API_ERROR)
                    Log.e("ProfileActivity", "onFailure: ", t)
                }
            })
        }
    }

    fun unfollowUser(userId: String){
        if(isFollowing.value == null) _responseError.value = ApiResponse(ResponseStatus.API_ERROR)

        sharedPrefs.accessToken?.let {
            apiClient.unfollowUser("Bearer $it", userId).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if(response.isSuccessful){
                        _isFollowing.value = false
                    } else if(response.code() == 401){
                        _responseError.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    _responseError.value = ApiResponse(ResponseStatus.API_ERROR)
                    Log.e("ProfileActivity", "onFailure: ", t)
                }
            })
        }
    }
}