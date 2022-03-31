package nl.inholland.myvitality.ui.activity.detail

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.ktx.androidParameters
import com.google.firebase.dynamiclinks.ktx.dynamicLink
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.dynamiclinks.ktx.shortLinkAsync
import com.google.firebase.ktx.Firebase
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
    private val _activityProgress = MutableLiveData<ActivityProgress>()
    private val _recommendedActivities = MutableLiveData<List<Activity>>()
    private val _shareableLink = MutableLiveData<String>()
    private val _response = MutableLiveData<ApiResponse>()

    val currentActivity: LiveData<Activity>
        get() = _currentActivity

    val activityProgress: LiveData<ActivityProgress>
        get() = _activityProgress

    val recommendedActivities: LiveData<List<Activity>>
        get() = _recommendedActivities

    val shareableLink: LiveData<String>
        get() = _shareableLink

    val apiResponse: LiveData<ApiResponse>
        get() = _response


    fun getActivity(activityId: String) {
        sharedPrefs.accessToken?.let {
            apiClient.getActivity("Bearer $it", activityId).enqueue(object : Callback<Activity> {
                override fun onResponse(call: Call<Activity>, response: Response<Activity>) {
                    if (response.body() == null) _response.value =
                        ApiResponse(ResponseStatus.NOT_FOUND)

                    if (response.isSuccessful && response.body() != null) {
                        response.body()?.let { activity ->
                            _currentActivity.value = activity
                            _activityProgress.value = activity.activityProgress
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
        if (currentActivity.value == null) _recommendedActivities.value = emptyList()

        sharedPrefs.accessToken?.let {
            apiClient.getActivities(
                "Bearer $it",
                currentActivity.value!!.category.categoryId,
                state = ActivityState.RECOMMENDED.id
            ).enqueue(object : Callback<List<Activity>> {
                override fun onResponse(
                    call: Call<List<Activity>>,
                    response: Response<List<Activity>>
                ) {
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
                            _activityProgress.value = activityProgress
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

    fun createShareLink(activityId: String) {
        val invitationLink =
            "https://example.com/activity?activityId=${activityId}"

        Firebase.dynamicLinks.createDynamicLink()
            .setLink(Uri.parse(invitationLink))
            .setDomainUriPrefix("https://myvitality.page.link")
            .setAndroidParameters(
                DynamicLink.AndroidParameters.Builder("nl.inholland.myvitality")
                    .setMinimumVersion(1)
                    .build()
            ).buildShortDynamicLink().addOnSuccessListener { result ->
                _shareableLink.value = result.shortLink.toString()
            }.addOnFailureListener {
                Log.d("HomeFragment", "==> ${it.localizedMessage}", it)
            }
    }
}