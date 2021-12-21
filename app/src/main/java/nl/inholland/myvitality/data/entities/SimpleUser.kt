package nl.inholland.myvitality.data.entities

import com.squareup.moshi.Json

data class SimpleUser (
    @Json(name = "userId") val userId: String,
    @Json(name = "profilePicture") val profileImage: String?,
    @Json(name = "fullName") var fullName : String,
    @Json(name = "jobTitle") var jobTitle : String,
    @Json(name = "location") var location : String,
)