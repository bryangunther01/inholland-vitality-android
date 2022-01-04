package nl.inholland.myvitality.data

import android.content.Context
import nl.gunther.bryan.newsreader.utils.SharedPreferenceHelper
import okhttp3.*

class TokenAuthenticator(
    context: Context,
    private val apiClient: ApiClient
) : Authenticator {

    private val appContext = context.applicationContext
    private val sharedPrefs = SharedPreferenceHelper(appContext)

    override fun authenticate(route: Route?, response: Response): Request? {
        // This is a synchronous call

        if(response.code() == 401) {
            val updatedToken = getUpdatedToken()
            return response.request().newBuilder()
                .header("Authorization", "Bearer " +  updatedToken)
                .build()
        }

        return null
    }

    private fun getUpdatedToken(): String {
        var newToken = ""

        sharedPrefs.refreshToken?.let{
            val authTokenResponse = apiClient.refreshToken(it).execute().body()!!
            newToken = "$authTokenResponse.accessToken"

            sharedPrefs.accessToken = authTokenResponse.accessToken
            sharedPrefs.refreshToken = authTokenResponse.refreshToken
            sharedPrefs.tokenExpireTime = authTokenResponse.expiresIn
        }

        return newToken
    }
}