package nl.inholland.myvitality.ui.timelinepost

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.OnClick
import coil.load
import com.google.android.material.button.MaterialButton
import nl.gunther.bryan.newsreader.utils.SharedPreferenceHelper
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.architecture.ChosenFragment
import nl.inholland.myvitality.architecture.base.BaseActivity
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.data.adapters.CommentAdapter
import nl.inholland.myvitality.data.entities.Challenge
import nl.inholland.myvitality.data.entities.ChallengeProgress
import nl.inholland.myvitality.data.entities.Comment
import nl.inholland.myvitality.data.entities.TimelinePost
import nl.inholland.myvitality.ui.MainActivity
import nl.inholland.myvitality.ui.authentication.login.LoginActivity
import nl.inholland.myvitality.ui.timeline.TimelineLikedActivity
import nl.inholland.myvitality.ui.timelinepost.create.CreateTimelinePostActivity
import nl.inholland.myvitality.util.TextViewUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.stream.Collectors
import javax.inject.Inject

class TimelinePostActivity : BaseActivity(), Callback<List<Comment>> {
    @Inject
    lateinit var apiClient: ApiClient

    @Inject
    lateinit var sharedPrefs: SharedPreferenceHelper

    @BindView(R.id.post_profile_image)
    lateinit var profileImage: ImageView

    @BindView(R.id.post_user_name)
    lateinit var userName: TextView

    @BindView(R.id.post_date)
    lateinit var date: TextView

    @BindView(R.id.post_content)
    lateinit var content: TextView

    @BindView(R.id.post_image)
    lateinit var image: ImageView

    @BindView(R.id.post_like_count_icon)
    lateinit var likedCountIcon: ImageView

    @BindView(R.id.post_like_count)
    lateinit var likedCount: TextView

    @BindView(R.id.post_comment_count)
    lateinit var commentCount: TextView

    @BindView(R.id.comment_recyclerview)
    lateinit var recyclerView: RecyclerView

    @BindView(R.id.post_like_button)
    lateinit var likeButton: MaterialButton

    @BindView(R.id.post_comment_button)
    lateinit var commentButton: MaterialButton

    var layoutManager: LinearLayoutManager? = null
    var adapter: CommentAdapter? = null
    var isLoading: Boolean = true

    var timelinePostId: String = ""
    var currentPost: TimelinePost? = null

    private var page = 0
    private var limit = 10
    private var likeCount = 0

    override fun layoutResourceId(): Int {
        return R.layout.activity_post
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as VitalityApplication).appComponent.inject(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.navigation_timeline)

        val postId = intent.getStringExtra("POST_ID")
        if (postId == null) {
            finish()
        } else {
            timelinePostId = postId
        }

        if (intent.getBooleanExtra("COMMENT", false)) {
            startActivity(
                Intent(this, CreateTimelinePostActivity::class.java)
                    .putExtra("POST_ID", timelinePostId)
            )
        }

        tryLoadPost()
        setupRecyclerViews()
    }

    @OnClick(R.id.post_like_count)
    fun onClickLikeCount() {
        startActivity(
            Intent(this, TimelineLikedActivity::class.java)
                .putExtra("POST_ID", timelinePostId)
        )
    }

    private fun setupRecyclerViews() {
        layoutManager = LinearLayoutManager(this)
        adapter = CommentAdapter(this)

        recyclerView.let {
            it.adapter = adapter
            it.layoutManager = layoutManager
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (dy > 0) {
                    val visibleItemCount = layoutManager?.childCount ?: 0
                    val totalItemCount = layoutManager?.itemCount ?: 0
                    val pastVisibleItems = layoutManager?.findFirstVisibleItemPosition() ?: -1

                    if (isLoading) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            isLoading = false
                            tryLoadComments()
                        }
                    }
                }
            }
        })
    }

    @OnClick(R.id.post_like_button)
    fun onClickLike() {
        sharedPrefs.accessToken.let {
            if(currentPost?.iLikedPost == true){
                apiClient.unlikePost("Bearer $it", timelinePostId).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if(response.isSuccessful){
                            likeCount--
                            toggleLike(false, isUpdated = true)
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@TimelinePostActivity, getString(R.string.api_error), Toast.LENGTH_LONG).show()
                    }
                })
            } else {
                apiClient.likePost("Bearer $it", timelinePostId).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if(response.isSuccessful){
                            likeCount++
                            toggleLike(true, isUpdated = true)
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@TimelinePostActivity, getString(R.string.api_error), Toast.LENGTH_LONG).show()
                    }
                })
            }

        }

    }

    fun toggleLike(isLiked: Boolean, isUpdated: Boolean? = false){
        if(isLiked){
            val otherCount = likeCount-1
            val visibility = View.VISIBLE

            likedCountIcon.visibility = visibility
            likedCount.visibility = visibility

            likeButton.setIconResource(R.drawable.ic_thumbsup_fill)
            likeButton.setIconTintResource(R.color.primary)

            likedCount.text = getString(R.string.post_like_count, otherCount.toString())
            currentPost?.iLikedPost = true
        } else {
            val visibility = if(likeCount > 0) View.VISIBLE else View.INVISIBLE

            likedCountIcon.visibility = visibility
            likedCount.visibility = visibility

            likeButton.setIconResource(R.drawable.ic_thumbsup)
            likeButton.setIconTintResource(R.color.black)

            likedCount.text = likeCount.toString()
            currentPost?.iLikedPost = false
        }
    }

    @OnClick(R.id.post_comment_button)
    fun onClickComment() {
        val intent = Intent(this, CreateTimelinePostActivity::class.java)
        intent.putExtra("POST_ID", timelinePostId)
        intent.putExtra("COMMENT", true)
        startActivity(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        startActivity(
            Intent(this, MainActivity::class.java)
                .putExtra("FRAGMENT_TO_LOAD", ChosenFragment.FRAGMENT_TIMELINE.ordinal)
        )
    }

    private fun tryLoadPost() {
        sharedPrefs.accessToken?.let {
            apiClient.getTimelinePost("Bearer $it", timelinePostId)
                .enqueue(object : Callback<TimelinePost> {
                    override fun onResponse(
                        call: Call<TimelinePost>,
                        response: Response<TimelinePost>
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            val timelinePost = response.body()!!
                            loadPost(timelinePost)
                            tryLoadComments()
                        } else {
                            startActivity(
                                Intent(this@TimelinePostActivity, MainActivity::class.java)
                                    .putExtra(
                                        "FRAGMENT_TO_LOAD",
                                        ChosenFragment.FRAGMENT_TIMELINE.ordinal
                                    )
                            )
                            Toast.makeText(
                                this@TimelinePostActivity,
                                getString(R.string.api_error_post),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<TimelinePost>, t: Throwable) {
                        Toast.makeText(
                            this@TimelinePostActivity,
                            getString(R.string.api_error_post),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
        }
    }

    private fun tryLoadComments(refresh: Boolean = false) {
        if (refresh) page = 0

        sharedPrefs.accessToken?.let {
            apiClient.getTimelinePostComments("Bearer $it", timelinePostId, limit, page * limit)
                .enqueue(this)
        }
    }

    private fun loadPost(timelinePost: TimelinePost) {
        currentPost = timelinePost

        profileImage.load(timelinePost.profilePicture)
        userName.text = timelinePost.fullName
        date.append(TextViewUtils.formatDate(timelinePost.publishDate))
        content.text = timelinePost.text

        timelinePost.imageUrl?.let {
            image.visibility = View.VISIBLE
            image.load(it)
        }

        if (timelinePost.countOfLikes > 0) {
            likedCount.visibility = View.VISIBLE
            findViewById<ImageView>(R.id.post_like_count_icon).visibility = View.VISIBLE
            likedCount.text = timelinePost.countOfLikes.toString()
        }

        likeCount = timelinePost.countOfLikes

        toggleLike(timelinePost.iLikedPost)

        if (timelinePost.countOfComments > 0) {
            commentCount.visibility = View.VISIBLE
            commentCount.text = null
            commentCount.append(timelinePost.countOfComments.toString() + " ")
            commentCount.append(getString(R.string.post_text_comments))
        }

    }

    override fun onResponse(call: Call<List<Comment>>, response: Response<List<Comment>>) {
        if (response.isSuccessful && response.body() != null) {
            response.body()?.let {
                if (it.isNotEmpty()) {
                    adapter?.addItems(it)
                    page += 1
                }

                isLoading = true
                // TODO:
//                refreshLayout.isRefreshing = false
            }
        } else {
            Toast.makeText(this, getString(R.string.api_error_comments), Toast.LENGTH_LONG).show()
        }

    }

    override fun onFailure(call: Call<List<Comment>>, t: Throwable) {
        Toast.makeText(this, "HIER", Toast.LENGTH_LONG).show()
    }
}