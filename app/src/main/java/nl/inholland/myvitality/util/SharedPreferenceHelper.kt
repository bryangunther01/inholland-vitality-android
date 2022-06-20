package nl.inholland.myvitality.util

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject

class SharedPreferenceHelper @Inject constructor(val context: Context){

    private val preferences: SharedPreferences = context.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)

    // The access token to use through the app
    var accessToken: String?
        get() = preferences.getString(ACCESS_TOKEN, null)
        set(value) = preferences.edit().putString(ACCESS_TOKEN, value).apply()

    // The refresh token to use through the app
    var refreshToken: String?
        get() = preferences.getString(REFRESH_TOKEN, null)
        set(value) = preferences.edit().putString(REFRESH_TOKEN, value).apply()

    // The expire time of the token
    var tokenExpireTime: Long
        get() = preferences.getLong(TOKEN_EXPIRE_TIME, 0L)
        set(value) = preferences.edit().putLong(TOKEN_EXPIRE_TIME, value * 1000).apply()

    // If the user is using the app for the first time for things like the tutorial
    var isFirstAppUse: Boolean
        get() = preferences.getBoolean(IS_FIRST_APP_USE, true)
        set(value) = preferences.edit().putBoolean(IS_FIRST_APP_USE, value).apply()

    // To check if the user recently registered
    var recentlyRegistered: Boolean
        get() = preferences.getBoolean(RECENTLY_REGISTERED, false)
        set(value) = preferences.edit().putBoolean(RECENTLY_REGISTERED, value).apply()

    // The current userId
    var currentUserId: String?
        get() = preferences.getString(CURRENT_USER_ID, null)
        set(value) = preferences.edit().putString(CURRENT_USER_ID, value).apply()

    // The token that will be used to send push notifications to the user's device
    var pushToken: String?
        get() = preferences.getString(PUSH_TOKEN, null)
        set(value) = preferences.edit().putString(PUSH_TOKEN, value).apply()

    var userFirstname: String?
        get() = preferences.getString(USER_FIRSTNAME, null)
        set(value) = preferences.edit().putString(USER_FIRSTNAME, value).apply()

    var userLastname: String?
        get() = preferences.getString(USER_LASTNAME, null)
        set(value) = preferences.edit().putString(USER_LASTNAME, value).apply()


    fun isLoggedIn(): Boolean {
        return accessToken != null
    }

    fun logoutUser(){
        val editor = preferences.edit()

        editor.remove(ACCESS_TOKEN)
        editor.remove(REFRESH_TOKEN)
        editor.remove(TOKEN_EXPIRE_TIME)
        editor.remove(CURRENT_USER_ID)
        editor.remove(USER_FIRSTNAME)
        editor.remove(USER_LASTNAME)

        editor.apply()
    }

    companion object {
        private const val SHARED_PREF_KEY = "nl.inholland.myvitality"
        private const val ACCESS_TOKEN = "accessToken"
        private const val REFRESH_TOKEN = "refreshToken"
        private const val TOKEN_EXPIRE_TIME = "tokenExpireTime"
        private const val IS_FIRST_APP_USE = "isFirstAppUse"
        private const val RECENTLY_REGISTERED = "recentlyRegistered"
        private const val CURRENT_USER_ID = "currentUserId"
        private const val PUSH_TOKEN = "pushToken"
        private const val USER_FIRSTNAME = "userFirstname"
        private const val USER_LASTNAME = "userLastname"
    }
}