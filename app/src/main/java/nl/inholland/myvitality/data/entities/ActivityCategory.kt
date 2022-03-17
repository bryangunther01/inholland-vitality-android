package nl.inholland.myvitality.data.entities

import com.squareup.moshi.Json

data class ActivityCategory (
    @Json(name = "categoryId") val categoryId: String,
    @Json(name = "title") var title : String,
    @Json(name = "description") var description : String,
    @Json(name = "imageLink") var imageLink : String?,
    @Json(name = "isActive") var isActive : Boolean,

)