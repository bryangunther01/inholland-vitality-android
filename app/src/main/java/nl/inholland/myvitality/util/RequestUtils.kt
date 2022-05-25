package nl.inholland.myvitality.util

import okhttp3.MultipartBody
import okhttp3.RequestBody

object RequestUtils {

    fun createPartFromString(value: String): RequestBody {
        return RequestBody.create(MultipartBody.FORM, value)
    }

    fun createPartFromOptionalString(value: String?): RequestBody? {
        if(value.isNullOrBlank()) return null
        return RequestBody.create(MultipartBody.FORM, value)
    }
}