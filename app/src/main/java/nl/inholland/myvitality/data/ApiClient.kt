package nl.inholland.myvitality.data

import nl.inholland.myvitality.data.entities.AuthSettings
import nl.inholland.myvitality.data.entities.requestbody.AuthRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiClient {

    /** Authentication calls **/
    @POST("login")
    fun login(
        @Body body: AuthRequest
    ): Call<AuthSettings>

    @POST("user")
    fun register(
        @Body body: AuthRequest
    ): Call<Void>

}