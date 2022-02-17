package nl.inholland.myvitality.ui.challenge

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

class ChallengeViewModel constructor(
    private val apiClient: ApiClient,
    private val sharedPrefs: SharedPreferenceHelper
) : ViewModel() {

    private val _currentChallenge = MutableLiveData<Challenge>()
    private val _explorableChallenges = MutableLiveData<List<Challenge>>()
    private val _response = MutableLiveData<ApiResponse>()

    val currentChallenge: LiveData<Challenge>
        get() = _currentChallenge

    val explorableChallenges: LiveData<List<Challenge>>
        get() = _explorableChallenges

    val apiResponse: LiveData<ApiResponse>
        get() = _response

    fun getChallenge(challengeId: String) {
        sharedPrefs.accessToken?.let {
            apiClient.getChallenge("Bearer $it", challengeId).enqueue(object : Callback<Challenge> {
                override fun onResponse(call: Call<Challenge>, response: Response<Challenge>) {
                    if(response.body() == null) _response.value = ApiResponse(ResponseStatus.NOT_FOUND)

                    if (response.isSuccessful && response.body() != null) {
                        response.body()?.let { challenge ->
                            _currentChallenge.value = challenge
                        }
                    } else if (response.code() == 401) {
                        _response.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                    }
                }

                override fun onFailure(call: Call<Challenge>, t: Throwable) {
                    _response.value = ApiResponse(ResponseStatus.API_ERROR)
                    Log.e("HomeFragment", "onFailure: ", t)
                }
            })
        }
    }

    fun getChallenges(challengeType: ChallengeType, currentChallengeId: String) {
        sharedPrefs.accessToken?.let {
            apiClient.getChallenges(
                "Bearer $it",
                challengeType = challengeType.id,
            ).enqueue(object : Callback<List<Challenge>> {
                override fun onResponse(
                    call: Call<List<Challenge>>,
                    response: Response<List<Challenge>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        response.body()?.let { challenges ->
                            val filtered = challenges.stream()
                                .filter { o -> o.challengeProgress == ChallengeProgress.NOT_SUBSCRIBED || o.challengeProgress == ChallengeProgress.CANCELLED }
                                .filter { o -> o.challengeId != currentChallengeId }.collect(
                                Collectors.toList()
                            )

                            _explorableChallenges.value = filtered
                        }
                    } else if (response.code() == 401) {
                        _response.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                    }
                }

                override fun onFailure(call: Call<List<Challenge>>, t: Throwable) {
                    _response.value = ApiResponse(ResponseStatus.API_ERROR)
                    Log.e("HomeFragment", "onFailure: ", t)
                }
            })
        }
    }

    fun updateChallengeProgress(challengeProgress: ChallengeProgress, currentChallengeId: String) {
        sharedPrefs.accessToken?.let {
            apiClient.updateChallengeProgress("Bearer $it", currentChallengeId, challengeProgress.id)
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            _response.value = ApiResponse(ResponseStatus.UPDATED_VALUE)
                        } else if (response.code() == 401) {
                            _response.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        _response.value = ApiResponse(ResponseStatus.API_ERROR)
                        Log.e("HomeFragment", "onFailure: ", t)
                    }
                })
        }
    }
}