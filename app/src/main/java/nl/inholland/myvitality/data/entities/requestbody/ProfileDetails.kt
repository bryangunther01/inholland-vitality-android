package nl.inholland.myvitality.data.entities.requestbody

import com.squareup.moshi.Json


data class ProfileDetails (
    @Json(name = "Firstname") var firstName : String? = null,
    @Json(name = "Lastname") var lastName : String? = null,
    @Json(name = "JobTitle") var jobTitle : String? = null,
    @Json(name = "Location") var location : String? = null,
    @Json(name = "Description") var description : String? = null,
)