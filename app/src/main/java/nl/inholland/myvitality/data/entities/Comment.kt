package nl.inholland.myvitality.data.entities

import com.squareup.moshi.Json

data class Comment (
    @Json(name = "commentId") val commentId: String,
    @Json(name = "timelinePostId") val timelinePostId: String,
    @Json(name = "userId") val userId: String,
    @Json(name = "text") val text: String,
    @Json(name = "timestamp") val timestamp: String,
    @Json(name = "fullName") val fullName: String,
    @Json(name = "imageUrl") val imageUrl: String?,
)