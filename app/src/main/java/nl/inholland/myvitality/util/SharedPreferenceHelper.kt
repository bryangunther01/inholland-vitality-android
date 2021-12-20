package nl.gunther.bryan.newsreader.utils

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject

class SharedPreferenceHelper @Inject constructor(private val context: Context){

    private val SHARED_PREF_KEY = "nl.inholland.myvitality"
    private val ACCESS_TOKEN = "accessToken"
    private val REFRESH_TOKEN = "refreshToken"
    private val TOKEN_EXPIRE_TIME = "tokenExpireTime"
    private val IS_FIRST_APP_USE = "isFirstAppUse"
    private val RECENTLY_REGISTERED = "recentlyRegistered"
    private val USER_FULL_NAME = "userName"
    private val USER_PROFILE_IMAGE_URL = "userProfileImageUrl"

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
    var tokenExpireTime: Int
        get() = preferences.getInt(TOKEN_EXPIRE_TIME, 0)
        set(value) = preferences.edit().putInt(TOKEN_EXPIRE_TIME, 0).apply()

    // If the user is using the app for the first time for things like the tutorial
    var isFirstAppUse: Boolean
        get() = preferences.getBoolean(IS_FIRST_APP_USE, true)
        set(value) = preferences.edit().putBoolean(IS_FIRST_APP_USE, value).apply()

    // To check if the user recently registered
    var recentlyRegistered: Boolean
        get() = preferences.getBoolean(RECENTLY_REGISTERED, false)
        set(value) = preferences.edit().putBoolean(RECENTLY_REGISTERED, value).apply()

    // The users full name to use through the app
    var userFullName: String?
        get() = preferences.getString(USER_FULL_NAME, null)
        set(value) = preferences.edit().putString(USER_FULL_NAME, value).apply()

    // The users profile image to use through the app
    var userProfileImageUrl: String?
        get() = preferences.getString(USER_PROFILE_IMAGE_URL, null)
        set(value) = preferences.edit().putString(USER_PROFILE_IMAGE_URL, value).apply()

    fun isLoggedIn(): Boolean {
        return accessToken != null
    }

    fun deleteAccessToken(){
        val settings = context.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)
        settings?.edit()?.remove(ACCESS_TOKEN)?.apply()
    }
}