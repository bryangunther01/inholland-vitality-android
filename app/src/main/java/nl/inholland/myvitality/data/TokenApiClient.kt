package nl.inholland.myvitality.data

import nl.inholland.myvitality.data.entities.AuthSettings
import nl.inholland.myvitality.data.entities.requestbody.AuthRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface TokenApiClient {

    @POST("login")
    fun login(
        @Body body: AuthRequest
    ): Call<AuthSettings>

    @POST("login/refresh")
    fun refreshToken(
        @Query("refreshToken") token: String
    ): Call<AuthSettings>
}