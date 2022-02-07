package nl.inholland.myvitality.ui.authentication.recover

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.gunther.bryan.newsreader.utils.SharedPreferenceHelper
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.data.entities.ApiResponse
import nl.inholland.myvitality.data.entities.ResponseStatus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AccountRecoveryViewModel constructor(
    private val apiClient: ApiClient,
    private val sharedPrefs: SharedPreferenceHelper
) : ViewModel() {

    private val _response = MutableLiveData<ApiResponse>()

    val apiResponse: LiveData<ApiResponse>
        get() = _response

    fun sendRecoveryMail(email: String) {
        apiClient.recoverUser(email).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    //TODO: Do something??...
                } else if (response.code() == 400) {
                    _response.value = ApiResponse(ResponseStatus.API_ERROR)
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                _response.value = ApiResponse(ResponseStatus.API_ERROR)
                Log.e("ProfileEditActivity", "onFailure: ", t)
            }
        })
    }
}