package nl.inholland.myvitality.ui.activity.overview

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

class ActivityOverviewViewModel constructor(
    private val apiClient: ApiClient,
    private val sharedPrefs: SharedPreferenceHelper
) : ViewModel() {

    private val _userActivities = MutableLiveData<List<Activity>>()
    private val _availableActivities = MutableLiveData<List<Activity>>()
    private val _upcomingActivities = MutableLiveData<List<Activity>>()
    private val _response = MutableLiveData<ApiResponse>()

    val userActivities: LiveData<List<Activity>>
        get() = _userActivities

    val availableActivities: LiveData<List<Activity>>
        get() = _availableActivities

    val upcomingActivities: LiveData<List<Activity>>
        get() = _upcomingActivities

    val apiResponse: LiveData<ApiResponse>
        get() = _response

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
            apiClient.getActivities("Bearer $it", categoryId, limit, offset, activityState.id).enqueue(object : Callback<List<Activity>> {
                override fun onResponse(call: Call<List<Activity>>, response: Response<List<Activity>>) {
                    if(response.isSuccessful && response.body() != null){
                        response.body()?.let { activities ->
                            when(activityState){
                                ActivityState.USER -> {
                                    _userActivities.value = activities
                                    userActivitiesOffset += activities.size
                                }
                                ActivityState.AVAILABLE -> {
                                    _availableActivities.value = activities
                                    availableActivitiesOffset += activities.size
                                }
                                ActivityState.UPCOMING -> {
                                    _upcomingActivities.value = activities
                                    upcomingActivitiesOffset += activities.size
                                }
                            }
                        }
                    } else if(response.code() == 401){
                        _response.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                    }
                }

                override fun onFailure(call: Call<List<Activity>>, t: Throwable) {
                    _response.value = ApiResponse(ResponseStatus.API_ERROR)
                    Log.e("ActivityOverviewActivity", "onFailure: ", t)
                }
            })
        }
    }
}