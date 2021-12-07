package nl.inholland.myvitality.data.entities

import com.squareup.moshi.Json

data class TimelinePost (
    @Json(name = "timelinePostId") val postId: String,
    @Json(name = "userId") val userId: String,
    @Json(name = "profilePicture") var profilePicture : String?,
    @Json(name = "fullName") var fullName : String,
    @Json(name = "publishDate") var publishDate : String,
    @Json(name = "text") var text : String,
    @Json(name = "imageUrl") var imageUrl : String?,
    @Json(name = "countOfLikes") var countOfLikes : Int,
    @Json(name = "countOfComments") var countOfComments : Int,
    @Json(name = "iLikedPost") var iLikedPost : Boolean,
)