package nl.inholland.myvitality.data.entities

import com.squareup.moshi.Json

data class User (
    @Json(name = "userId") val userId: String,
    @Json(name = "firstName") var firstName : String?,
    @Json(name = "lastName") var lastName : String?,
    var fullName: String = "$firstName $lastName",
    @Json(name = "jobTitle") var jobTitle : String?,
    @Json(name = "location") var location : String?,
    @Json(name = "description") var description : String?,
    @Json(name = "profilePicture") var profilePicture : String?,
    @Json(name = "points") var points : Int? = 0,
    @Json(name = "interests") var interests : List<Interest>?,
    @Json(name = "following") var isFollowing : Boolean?,
    @Json(name = "canViewDetails") var canViewDetails : Boolean?
)