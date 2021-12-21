package nl.inholland.myvitality.ui.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.gunther.bryan.newsreader.utils.SharedPreferenceHelper
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.data.entities.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.stream.Collectors

class ProfileViewModel constructor(private val apiClient: ApiClient, private val sharedPrefs: SharedPreferenceHelper) : ViewModel() {

    private val _currentUser = MutableLiveData<User>()
    private val _isFollowing = MutableLiveData<Boolean>()
    private val _currentChallenges = MutableLiveData<List<Challenge>>()
    private val _finishedChallenges = MutableLiveData<List<Challenge>>()
    private val _responseError = MutableLiveData<ApiResponse>()

    val currentUser: LiveData<User>
        get() = _currentUser

    val isFollowing: LiveData<Boolean>
        get() = _isFollowing

    val currentChallenges: LiveData<List<Challenge>>
        get() = _currentChallenges

    val finishedChallenges: LiveData<List<Challenge>>
        get() = _finishedChallenges

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

    fun getChallenges(userId: String?){
        sharedPrefs.accessToken?.let {
            apiClient.getChallenges("Bearer $it", userId = userId).enqueue(object : Callback<List<Challenge>> {
                override fun onResponse(call: Call<List<Challenge>>, response: Response<List<Challenge>>) {
                    if(response.isSuccessful && response.body() != null){
                        response.body()?.let { challenges ->
                            val currentChallenges = challenges.stream()
                                .filter { o -> o.challengeProgress == ChallengeProgress.IN_PROGRESS }
                                .collect(Collectors.toList())
                            val finishedChallenges = challenges.stream()
                                .filter { o -> o.challengeProgress == ChallengeProgress.DONE }
                                .collect(Collectors.toList())

                            _currentChallenges.value = currentChallenges
                            _finishedChallenges.value = finishedChallenges
                        }
                    } else if(response.code() == 401){
                        _responseError.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                    }
                }

                override fun onFailure(call: Call<List<Challenge>>, t: Throwable) {
                    _responseError.value = ApiResponse(ResponseStatus.API_ERROR)
                    Log.e("ProfileActivity", "onFailure: ", t)
                }
            })
        }
    }

    fun toggleUserFollow(userId: String, following: Boolean){
        if(isFollowing.value == null) _responseError.value = ApiResponse(ResponseStatus.API_ERROR)

        sharedPrefs.accessToken?.let {
            apiClient.toggleUserFollow("Bearer $it", userId, following).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if(response.isSuccessful){
                        _isFollowing.value = !isFollowing.value!!
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