package nl.inholland.myvitality.ui.activity.overview

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.data.entities.Activity
import nl.inholland.myvitality.data.entities.ActivityState
import nl.inholland.myvitality.data.entities.ApiResponse
import nl.inholland.myvitality.data.entities.ResponseStatus
import nl.inholland.myvitality.util.SharedPreferenceHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ActivityOverviewViewModel constructor(private val apiClient: ApiClient, private val sharedPrefs: SharedPreferenceHelper) : ViewModel() {
    val userActivities: MutableLiveData<List<Activity>> by lazy {
        MutableLiveData<List<Activity>>()
    }

    val availableActivities: MutableLiveData<List<Activity>> by lazy {
        MutableLiveData<List<Activity>>()
    }

    val upcomingActivities: MutableLiveData<List<Activity>> by lazy {
        MutableLiveData<List<Activity>>()
    }

    val apiResponse: MutableLiveData<ApiResponse> by lazy {
        MutableLiveData<ApiResponse>()
    }

    private val limit = 10
    private var userActivitiesOffset = 0
    private var availableActivitiesOffset = 0
    private var upcomingActivitiesOffset = 0

    fun getActivities(categoryId: String, activityState: ActivityState){
        val offset = when(activityState){
            ActivityState.USER -> userActivitiesOffset
            ActivityState.AVAILABLE -> availableActivitiesOffset
            ActivityState.UPCOMING -> upcomingActivitiesOffset
            else -> { 0 }
        }

        sharedPrefs.accessToken?.let {
            apiClient.getActivities("Bearer $it", limit, offset, categoryId = categoryId, state = activityState.id).enqueue(object : Callback<List<Activity>> {
                override fun onResponse(call: Call<List<Activity>>, response: Response<List<Activity>>) {
                    if(response.isSuccessful && response.body() != null){
                        response.body()?.let { activities ->
                            when(activityState){
                                ActivityState.USER -> {
                                    userActivities.value = activities
                                    userActivitiesOffset += activities.size
                                }
                                ActivityState.AVAILABLE -> {
                                    availableActivities.value = activities
                                    availableActivitiesOffset += activities.size
                                }
                                ActivityState.UPCOMING -> {
                                    upcomingActivities.value = activities
                                    upcomingActivitiesOffset += activities.size
                                }
                            }
                        }
                    } else if(response.code() == 401){
                        apiResponse.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                    }
                }

                override fun onFailure(call: Call<List<Activity>>, t: Throwable) {
                    apiResponse.value = ApiResponse(ResponseStatus.API_ERROR)
                    Log.e("ActivityOverviewActivity", "onFailure: ", t)
                }
            })
        }
    }
}