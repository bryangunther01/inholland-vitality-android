package nl.inholland.myvitality.ui.authentication.register.additionaldetails

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.data.entities.ApiResponse
import nl.inholland.myvitality.data.entities.ResponseStatus
import nl.inholland.myvitality.util.RequestUtils
import nl.inholland.myvitality.util.SharedPreferenceHelper
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterAdditionalDetailsViewModel constructor(private val apiClient: ApiClient, private val sharedPrefs: SharedPreferenceHelper) : ViewModel() {

    val apiResponse: MutableLiveData<ApiResponse> by lazy {
        MutableLiveData<ApiResponse>()
    }

    fun updateUserProfile(
        description: String,
        filePart: MultipartBody.Part? = null
    ) {
        sharedPrefs.accessToken?.let {
            apiClient.updateUserProfile(
                "Bearer $it",
                description = RequestUtils.createPartFromOptionalString(description),
                file = filePart
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