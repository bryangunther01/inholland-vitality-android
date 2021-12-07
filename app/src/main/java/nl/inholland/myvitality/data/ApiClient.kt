package nl.inholland.myvitality.data

import nl.inholland.myvitality.data.entities.*
import nl.inholland.myvitality.data.entities.requestbody.AuthRequest
import nl.inholland.myvitality.data.entities.requestbody.ProfileDetails
import nl.inholland.myvitality.data.entities.requestbody.TimelinePostRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiClient {

    /** Authentication calls **/
    @POST("login")
    fun login(
        @Body body: AuthRequest
    ): Call<AuthSettings>

    @POST("user")
    fun register(
        @Body body: AuthRequest
    ): Call<Void>

    /** User calls **/

    @GET("user")
    fun getUser(
        @Header("Authorization") token: String
    ): Call<User>

    @GET("users/{name}")
    fun searchUser(
        @Header("Authorization") token: String,
        @Path("name") name: String
    ): Call<List<SimpleUser>>

    @PUT("user")
    fun updateUserProfile(
        @Header("Authorization") token: String,
        @Body body: ProfileDetails
    ): Call<Void>

    @Multipart
    @PUT("user/profilepicture")
    fun updateProfileImage(
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part
    ): Call<Void>

    /** Challenge calls **/
    @GET("challenge")
    fun getChallenges(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("challengeType") challengeType: Int? = null,
        @Query("progress") progress: Int? = null,
        @Query("userId") userId: Int? = null
    ): Call<List<Challenge>>

    @GET("challenge/{challengeId}")
    fun getChallenge(
        @Header("Authorization") token: String,
        @Path("challengeId") challengeId: String,
    ): Call<Challenge>

    @PUT("challenge/{challengeId}/progress")
    fun updateChallengeProgress(
        @Header("Authorization") token: String,
        @Path("challengeId") challengeId: String,
        @Query("challengeProgress") progress: Int
    ): Call<Void>

    /** Timeline calls **/
    @GET("timelinepost")
    fun getTimelinePosts(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
    ): Call<List<TimelinePost>>

    /** Timeline calls **/
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

    @POST("timelinepost")
    fun createPost(
        @Header("Authorization") token: String,
        @Body body: RequestBody,
    ): Call<Void>

}