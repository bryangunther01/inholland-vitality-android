package nl.inholland.myvitality.ui.activity.detail

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.data.entities.*
import nl.inholland.myvitality.data.entities.responsebody.ActivityProgressResponse
import nl.inholland.myvitality.util.SharedPreferenceHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.stream.Collectors

class ActivityViewModel constructor(private val apiClient: ApiClient, private val sharedPrefs: SharedPreferenceHelper) : ViewModel() {

    val currentActivity: MutableLiveData<Activity> by lazy {
        MutableLiveData<Activity>()
    }

    val activityProgress: MutableLiveData<ActivityProgress> by lazy {
        MutableLiveData<ActivityProgress>()
    }

    val achievements: MutableLiveData<List<Achievement>> by lazy {
        MutableLiveData<List<Achievement>>()
    }

    val recommendedActivities: MutableLiveData<List<Activity>> by lazy {
        MutableLiveData<List<Activity>>()
    }

    val shareableLink: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val apiResponse: MutableLiveData<ApiResponse> by lazy {
        MutableLiveData<ApiResponse>()
    }


    fun getActivity(activityId: String) {
        sharedPrefs.accessToken?.let {
            apiClient.getActivity("Bearer $it", activityId).enqueue(object : Callback<Activity> {
                override fun onResponse(call: Call<Activity>, response: Response<Activity>) {
                    if (response.body() == null) apiResponse.value =
                        ApiResponse(ResponseStatus.NOT_FOUND)

                    if (response.isSuccessful && response.body() != null) {
                        response.body()?.let { activity ->
                            currentActivity.value = activity
                            activityProgress.value = activity.activityProgress
                        }
                    } else if (response.code() == 401) {
                        apiResponse.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                    }
                }

                override fun onFailure(call: Call<Activity>, t: Throwable) {
                    apiResponse.value = ApiResponse(ResponseStatus.API_ERROR)
                    Log.e("HomeFragment", "onFailure: ", t)
                }
            })
        }
    }

    fun getRecommendedActivities() {
        if (currentActivity.value == null) recommendedActivities.value = emptyList()

        sharedPrefs.accessToken?.let {
            apiClient.getActivities(
                "Bearer $it",
                categoryId = currentActivity.value!!.category.categoryId,
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

                            recommendedActivities.value = filtered
                        }
                    } else if (response.code() == 401) {
                        apiResponse.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                    }
                }

                override fun onFailure(call: Call<List<Activity>>, t: Throwable) {
                    apiResponse.value = ApiResponse(ResponseStatus.API_ERROR)
                    Log.e("HomeFragment", "onFailure: ", t)
                }
            })
        }
    }

    fun updateActivityProgress(progress: ActivityProgress, activityId: String) {
        sharedPrefs.accessToken?.let {
            apiClient.updateActivityProgress("Bearer $it", activityId, progress.id)
                .enqueue(object : Callback<ActivityProgressResponse> {
                    override fun onResponse(call: Call<ActivityProgressResponse>, response: Response<ActivityProgressResponse>) {
                        if (response.isSuccessful && response.body() != null) {
                            response.body()?.let { res ->
                                activityProgress.value = res.activityProgress

                                res.achievements?.let { receivedAchievements ->
                                    achievements.value = receivedAchievements
                                }
                            }

                            apiResponse.value = ApiResponse(ResponseStatus.UPDATED_VALUE)
                        } else if (response.code() == 401) {
                            apiResponse.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                        }
                    }

                    override fun onFailure(call: Call<ActivityProgressResponse>, t: Throwable) {
                        apiResponse.value = ApiResponse(ResponseStatus.API_ERROR)
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
                shareableLink.value = result.shortLink.toString()
            }.addOnFailureListener {
                Log.d("HomeFragment", "==> ${it.localizedMessage}", it)
            }
    }
}