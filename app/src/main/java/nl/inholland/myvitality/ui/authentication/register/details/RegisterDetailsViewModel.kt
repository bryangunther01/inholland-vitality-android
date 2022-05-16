package nl.inholland.myvitality.ui.authentication.register.details

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.data.entities.ApiResponse
import nl.inholland.myvitality.data.entities.ResponseStatus
import nl.inholland.myvitality.util.RequestUtils
import nl.inholland.myvitality.util.SharedPreferenceHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterDetailsViewModel constructor(private val apiClient: ApiClient, private val sharedPrefs: SharedPreferenceHelper) : ViewModel() {

    val apiResponse: MutableLiveData<ApiResponse> by lazy {
        MutableLiveData<ApiResponse>()
    }

    fun updateUserProfile(
        firstName: String,
        lastName: String,
        jobTitle: String,
        location: String,
    ) {
        sharedPrefs.accessToken?.let {
            apiClient.updateUserProfile(
                "Bearer $it",
                firstName = RequestUtils.createPartFromOptionalString(firstName),
                lastName = RequestUtils.createPartFromOptionalString(lastName),
                jobTitle = RequestUtils.createPartFromOptionalString(jobTitle),
                location = RequestUtils.createPartFromOptionalString(location),
            ).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        apiResponse.value = ApiResponse(ResponseStatus.UPDATED_VALUE)
                    } else if (response.code() == 401) {
                        apiResponse.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    apiResponse.value = ApiResponse(ResponseStatus.API_ERROR)
                    Log.e("RegisterDetailsActivity", "onFailure: ", t)
                }
            })
        }
    }
}