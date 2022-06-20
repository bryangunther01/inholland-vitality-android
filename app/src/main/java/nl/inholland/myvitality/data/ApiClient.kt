package nl.inholland.myvitality.data

import nl.inholland.myvitality.data.entities.*
import nl.inholland.myvitality.data.entities.requestbody.PushToken
import nl.inholland.myvitality.data.entities.requestbody.RegisterRequest
import nl.inholland.myvitality.data.entities.requestbody.TimelinePostRequest
import nl.inholland.myvitality.data.entities.responsebody.ActivityProgressResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiClient {

    /** User calls **/
    @GET("user")
    fun getUser(
        @Header("Authorization") token: String,
        @Query("userId") userId: String? = null
    ): Call<User>

    @GET("users/{name}")
    fun searchUser(
        @Header("Authorization") token: String,
        @Path("name") name: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
    ): Call<List<SimpleUser>>

    @Multipart
    @PUT("user")
    fun updateUserProfile(
        @Header("Authorization") token: String,
        @Part("firstname") firstName: RequestBody? = null,
        @Part("lastname") lastName: RequestBody? = null,
        @Part("jobTitle") jobTitle: RequestBody? = null,
        @Part("location") location: RequestBody? = null,
        @Part("description") description: RequestBody? = null,
        @Part("interests") interest: RequestBody? = null,
        @Part file: MultipartBody.Part? = null
    ): Call<Void>

    @POST("user/follow")
    fun followUser(
        @Header("Authorization") token: String,
        @Query("userId") userId: String,
    ): Call<Void>

    @DELETE("user/follow")
    fun unfollowUser(
        @Header("Authorization") token: String,
        @Query("userId") userId: String,
    ): Call<Void>

    @DELETE("user")
    fun deleteUser(
        @Header("Authorization") token: String
    ): Call<Void>

    @GET("user/achievements")
    fun getAchievements(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("userId") userId: String? = null,
    ): Call<List<Achievement>>

    /** Activity Calls **/
    @GET("category")
    fun getActivityCategories(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
    ): Call<List<ActivityCategory>>

    @GET("activity/{activityId}")
    fun getActivity(
        @Header("Authorization") token: String,
        @Path("activityId") activityId: String,
    ): Call<Activity>

    @GET("activity")
    fun getActivities(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("categoryId") categoryId: String? = null,
        @Query("activityState") state: Int? = null,
        @Query("progress") progress: Int? = null,
        @Query("userId") userId: String? = null,
        ): Call<List<Activity>>

    @GET("activity/{activityId}/subscribers")
    fun getActivitySubscribers(
        @Header("Authorization") token: String,
        @Path("activityId") activityId: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
    ): Call<List<SimpleUser>>

    @PUT("activity/{activityId}/progress")
    fun updateActivityProgress(
        @Header("Authorization") token: String,
        @Path("activityId") activityId: String,
        @Query("activityProgress") progress: Int
    ): Call<ActivityProgressResponse>

    /** Scoreboard Calls **/
    @GET("scoreboard")
    fun getScoreboard(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
    ): Call<List<ScoreboardUser>>

    /** Interest calls **/
    @GET("interest")
    fun getInterests(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
    ): Call<List<Interest>>

    /** Timeline calls **/
    @GET("timelinepost")
    fun getTimelinePosts(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
    ): Call<List<TimelinePost>>

    @GET("timelinepost/{timelinePostId}")
    fun getTimelinePost(
        @Header("Authorization") token: String,
        @Path("timelinePostId") timelinePostId: String,
    ): Call<TimelinePost>

    @GET("timelinepost/{timelinePostId}/comments")
    fun getTimelinePostComments(
        @Header("Authorization") token: String,
        @Path("timelinePostId") timelinePostId: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
    ): Call<List<Comment>>

    @POST("timelinepost/{timelinePostId}/comment")
    fun createComment(
        @Header("Authorization") token: String,
        @Path("timelinePostId") timelinePostId: String,
        @Body body: TimelinePostRequest,
    ): Call<Void>

    @Multipart
    @POST("timelinepost")
    fun createPost(
        @Header("Authorization") token: String,
        @Part("Text") message: RequestBody,
        @Part file: MultipartBody.Part? = null
    ): Call<Void>

    @DELETE("timelinepost/{timelinePostId}")
    fun deletePost(
        @Header("Authorization") token: String,
        @Path("timelinePostId") timelinePostId: String,
    ): Call<Void>

    @GET("timelinepost/{timelinePostId}/likers")
    fun getTimelinePostLikes(
        @Header("Authorization") token: String,
        @Path("timelinePostId") timelinePostId: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
    ): Call<List<SimpleUser>>

    @POST("timelinepost/{timelinePostId}/like")
    fun likePost(
        @Header("Authorization") token: String,
        @Path("timelinePostId") timelinePostId: String,
    ): Call<Void>

    @DELETE("timelinepost/{timelinePostId}/like")
    fun unlikePost(
        @Header("Authorization") token: String,
        @Path("timelinePostId") timelinePostId: String,
    ): Call<Void>

    /** Notification calls **/
    @GET("notification")
    fun getNotifications(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
    ): Call<List<Notification>>

    /** Push token calls **/

    @POST("notification/pushtoken")
    fun createPushToken(
        @Header("Authorization") token: String,
        @Body body: PushToken
    ): Call<Void>

    @DELETE("notification/pushtoken")
    fun deletePushToken(
        @Header("Authorization") token: String,
        @Query("pushToken") pushToken: String
    ): Call<Void>

    @HEAD("notification/pushtoken/validate/{pushToken}")
    fun validateToken(
        @Header("Authorization") token: String,
        @Path("pushToken") pushToken: String
    ): Call<Void>
}