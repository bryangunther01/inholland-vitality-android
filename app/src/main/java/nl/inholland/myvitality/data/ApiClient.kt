package nl.inholland.myvitality.data

import nl.inholland.myvitality.data.entities.AuthSettings
import nl.inholland.myvitality.data.entities.requestbody.LoginRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiClient {

    /** Authentication calls **/
    @POST("Login")
    fun login(
        @Body body: LoginRequest
    ): Call<AuthSettings>

}