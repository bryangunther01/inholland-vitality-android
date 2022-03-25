package nl.inholland.myvitality.ui.activity.detail

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

class ActivityViewModel constructor(
    private val apiClient: ApiClient,
    private val sharedPrefs: SharedPreferenceHelper
) : ViewModel() {

    private val _currentActivity = MutableLiveData<Activity>()
    private val _recommendedActivities = MutableLiveData<List<Activity>>()
    private val _response = MutableLiveData<ApiResponse>()

    val currentActivity: LiveData<Activity>
        get() = _currentActivity

    val recommendedActivities: LiveData<List<Activity>>
        get() = _recommendedActivities

    val apiResponse: LiveData<ApiResponse>
        get() = _response

    fun getActivity(activityId: String) {
        sharedPrefs.accessToken?.let {
            apiClient.getActivity("Bearer $it", activityId).enqueue(object : Callback<Activity> {
                override fun onResponse(call: Call<Activity>, response: Response<Activity>) {
                    if(response.body() == null) _response.value = ApiResponse(ResponseStatus.NOT_FOUND)

                    if (response.isSuccessful && response.body() != null) {
                        response.body()?.let { activity ->
                            _currentActivity.value = activity
                        }
                    } else if (response.code() == 401) {
                        _response.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                    }
                }

                override fun onFailure(call: Call<Activity>, t: Throwable) {
                    _response.value = ApiResponse(ResponseStatus.API_ERROR)
                    Log.e("HomeFragment", "onFailure: ", t)
                }
            })
        }
    }

    fun getRecommendedActivities() {
        if(currentActivity.value == null) _recommendedActivities.value = emptyList()

        sharedPrefs.accessToken?.let {
            apiClient.getActivities("Bearer $it", currentActivity.value!!.category.categoryId, state = ActivityState.RECOMMENDED.id).enqueue(object : Callback<List<Activity>> {
                override fun onResponse(call: Call<List<Activity>>, response: Response<List<Activity>>) {
                    if (response.isSuccessful && response.body() != null) {
                        response.body()?.let { activities ->
                            val filtered = activities.stream()
                                .filter { o -> o.activityId != currentActivity.value?.activityId }
                                .collect(Collectors.toList())

                            _recommendedActivities.value = filtered
                        }
                    } else if (response.code() == 401) {
                        _response.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                    }
                }

                override fun onFailure(call: Call<List<Activity>>, t: Throwable) {
                    _response.value = ApiResponse(ResponseStatus.API_ERROR)
                    Log.e("HomeFragment", "onFailure: ", t)
                }
            })
        }
    }

    fun updateActivityProgress(activityProgress: ActivityProgress, activityId: String) {
        sharedPrefs.accessToken?.let {
            apiClient.updateActivityProgress("Bearer $it", activityId, activityProgress.id)
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            if (activityProgress == ActivityProgress.IN_PROGRESS) {
                                _response.value = ApiResponse((ResponseStatus.SUCCESSFUL))
                            } else {
                                _response.value = ApiResponse(ResponseStatus.UPDATED_VALUE)
                            }
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