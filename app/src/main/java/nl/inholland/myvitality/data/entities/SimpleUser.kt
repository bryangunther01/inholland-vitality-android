package nl.inholland.myvitality.data.entities

import com.squareup.moshi.Json

data class SimpleUser (
    @Json(name = "userId") val userId: String,
    @Json(name = "profilePicture") val profileImage: String?,
    @Json(name = "firstName") var firstName : String?,
    @Json(name = "lastName") var lastName : String?,
    var fullName: String = "$firstName $lastName",
    @Json(name = "jobTitle") var jobTitle : String?,
    @Json(name = "location") var location : String?,
)