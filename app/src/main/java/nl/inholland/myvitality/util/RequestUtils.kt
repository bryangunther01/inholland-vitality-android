package nl.inholland.myvitality.util

import okhttp3.MultipartBody
import okhttp3.RequestBody

object RequestUtils {

    /**
     * Method to create a multipart form data part
     * @param value the value to create a part of
     * @return the requestBody with the value
     */
    fun createPartFromString(value: String): RequestBody {
        return RequestBody.create(MultipartBody.FORM, value)
    }

    /**
     * Method to create a multipart form data part of an optional value
     * @param value the value to create a part of
     * @return the requestBody with the value
     */
    fun createPartFromOptionalString(value: String?): RequestBody? {
        if(value.isNullOrBlank()) return null
        return RequestBody.create(MultipartBody.FORM, value)
    }
}