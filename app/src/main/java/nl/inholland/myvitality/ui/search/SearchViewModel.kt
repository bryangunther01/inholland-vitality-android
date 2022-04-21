package nl.inholland.myvitality.ui.search

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.data.entities.ApiResponse
import nl.inholland.myvitality.data.entities.ResponseStatus
import nl.inholland.myvitality.data.entities.SimpleUser
import nl.inholland.myvitality.util.SharedPreferenceHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchViewModel constructor(
    private val apiClient: ApiClient,
    private val sharedPrefs: SharedPreferenceHelper
) : ViewModel() {

    val results: MutableLiveData<List<SimpleUser>> by lazy {
        MutableLiveData<List<SimpleUser>>()
    }

    val apiResponse: MutableLiveData<ApiResponse> by lazy {
        MutableLiveData<ApiResponse>()
    }

    fun search(value: String, limit: Int, offset: Int) {
        sharedPrefs.accessToken?.let {
            apiClient.searchUser("Bearer $it", value, limit , offset).enqueue(object : Callback<List<SimpleUser>> {
                override fun onResponse(call: Call<List<SimpleUser>>, response: Response<List<SimpleUser>>) {
                    if (response.isSuccessful && response.body() != null) {
                        response.body()?.let { users ->
                            results.value = users
                        }
                    } else if (response.code() == 401) {
                        apiResponse.value = ApiResponse(ResponseStatus.UNAUTHORIZED)
                    }
                }

                override fun onFailure(call: Call<List<SimpleUser>>, t: Throwable) {
                    apiResponse.value = ApiResponse(ResponseStatus.API_ERROR)
                    Log.e("HomeFragment", "onFailure: ", t)
                }
            })
        }
    }
}