package nl.gunther.bryan.newsreader.utils

import android.content.Context

class SharedPreferenceHelper constructor(private var context: Context?){

    private val SHARED_PREF_KEY = "inholland.myvitality"
    private val ACCESS_TOKEN = "accessToken"
    private val IS_FIRST_USE = "isFirstUse"

    fun isFirstUse(): Boolean? {
        val settings = context?.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)
        return settings?.getBoolean(IS_FIRST_USE, true)
    }

    fun setFirstUsed(){
        val settings = context?.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)
        settings?.edit()?.putBoolean(IS_FIRST_USE, false)?.apply()
    }

    fun isLoggedIn(): Boolean {
        return getAccessToken() != null
    }

    fun deleteAccessToken(){
        val settings = context?.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)
        settings?.edit()?.remove(ACCESS_TOKEN)?.apply()
    }

    fun setAccessToken(accessToken: String){
        val settings = context?.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)
        settings?.edit()?.putString(ACCESS_TOKEN, accessToken)?.apply()
    }

    fun getAccessToken(): String?{
        val settings = context?.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)
        return settings?.getString(ACCESS_TOKEN, null)
    }
}