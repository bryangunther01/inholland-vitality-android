package nl.inholland.myvitality.ui.home

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

class HomeViewModel constructor(private val apiClient: ApiClient, private val sharedPrefs: SharedPreferenceHelper) : ViewModel() {

    private val _currentUser = MutableLiveData<User>()
    private val _currentChallenges = MutableLiveData<List<Challenge>>()
    private val _explorableChallenges = MutableLiveData<List<Challenge>>()
    private val _responseError = MutableLiveData<ApiResponse>()

    val currentUser: LiveData<User>
        get() = _currentUser

    val currentChallenges: LiveData<List<Challenge>>
        get() = _currentChallenges

    val explorableChallenges: LiveData<List<Challenge>>
        get() = _explorableChallenges

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

    fun getChallenges(){
        sharedPrefs.accessToken?.let {
            apiClient.getChallenges("Bearer $it").enqueue(object : Callback<List<Challenge>> {
                override fun onResponse(call: Call<List<Challenge>>, response: Response<List<Challenge>>) {
                    if(response.isSuccessful && response.body() != null){
                        response.body()?.let { challenges ->
                            val currentChallenges = challenges.stream()
                                .filter { o -> o.challengeProgress == ChallengeProgress.IN_PROGRESS }
                                .collect(Collectors.toList())
                            val exploreChallenges = challenges.stream()
                                .filter { o -> o.challengeProgress == ChallengeProgress.NOT_SUBSCRIBED || o.challengeProgress == ChallengeProgress.CANCELLED }
                                .collect(Collectors.toList())

                            _currentChallenges.value = currentChallenges
                            _explorableChallenges.value = exploreChallenges
                        }
                    } else if(response.code() == 401){
                        _responseError.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                    }
                }

                override fun onFailure(call: Call<List<Challenge>>, t: Throwable) {
                    _responseError.value = ApiResponse(ResponseStatus.API_ERROR)
                    Log.e("HomeFragment", "onFailure: ", t)
                }
            })
        }
    }
}