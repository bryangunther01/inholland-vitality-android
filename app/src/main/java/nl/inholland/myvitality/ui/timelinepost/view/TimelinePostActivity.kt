package nl.inholland.myvitality.ui.timelinepost.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.OnClick
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.button.MaterialButton
import nl.gunther.bryan.newsreader.utils.SharedPreferenceHelper
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.architecture.base.BaseActivity
import nl.inholland.myvitality.data.adapters.CommentAdapter
import nl.inholland.myvitality.data.entities.ResponseStatus
import nl.inholland.myvitality.data.entities.TimelinePost
import nl.inholland.myvitality.ui.profile.overview.ProfileActivity
import nl.inholland.myvitality.ui.timeline.liked.TimelineLikedActivity
import nl.inholland.myvitality.ui.timelinepost.create.CreateTimelinePostActivity
import nl.inholland.myvitality.ui.widgets.dialog.Dialogs
import nl.inholland.myvitality.util.DateUtils
import javax.inject.Inject

class TimelinePostActivity : BaseActivity() {
    @Inject
    lateinit var sharedPrefs: SharedPreferenceHelper

    @BindView(R.id.post_profile_image)
    lateinit var profileImage: ImageView

    @BindView(R.id.post_delete)
    lateinit var deleteIcon: ImageView

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

    @BindView(R.id.comments_empty_text)
    lateinit var commentsEmptyText: TextView

    @BindView(R.id.comment_recyclerview)
    lateinit var recyclerView: RecyclerView

    @BindView(R.id.post_like_button)
    lateinit var likeButton: MaterialButton

    @BindView(R.id.post_comment_button)
    lateinit var commentButton: MaterialButton

    @Inject
    lateinit var factory: TimelinePostViewModelFactory
    lateinit var viewModel: TimelinePostViewModel

    var layoutManager: LinearLayoutManager? = null
    var adapter: CommentAdapter? = null
    var isCalling: Boolean = false

    var currentPostId: String = ""
    var currentPost: TimelinePost? = null

    private var page = 0
    private var limit = 10
    private var likeCount = 0

    override fun layoutResourceId(): Int {
        return R.layout.activity_post
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.navigation_timeline)

        (application as VitalityApplication).appComponent.inject(this)
        viewModel = ViewModelProviders.of(this, factory).get(TimelinePostViewModel::class.java)

        val postId = intent.getStringExtra("POST_ID")
        if (postId == null) finish() else currentPostId = postId

        if (intent.getBooleanExtra("COMMENT", false)) {
            startActivity(
                Intent(this, CreateTimelinePostActivity::class.java)
                    .putExtra("POST_ID", currentPostId)
            )
        }

        setupRecyclerViews()
        initResponseHandler()

        initPost()
        Handler(Looper.getMainLooper()).postDelayed({
            initComments()
        }, 100)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    @OnClick(R.id.post_like_count)
    fun onClickLikeCount() {
        startActivity(
            Intent(this, TimelineLikedActivity::class.java)
                .putExtra("POST_ID", currentPostId)
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

                    if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                        tryLoadComments()
                    }
                }
            }
        })
    }

    @OnClick(value = [R.id.post_profile_image, R.id.post_user_name])
    fun onClickUser() {
        startActivity(
            Intent(this, ProfileActivity::class.java)
                .putExtra("USER_ID", currentPost?.userId)
        )
    }

    @OnClick(R.id.post_like_button)
    fun onClickLike() {
        viewModel.updateLike(currentPostId)
    }

    @OnClick(R.id.post_comment_button)
    fun onClickComment() {
        val intent = Intent(this, CreateTimelinePostActivity::class.java)
        intent.putExtra("POST_ID", currentPostId)
        intent.putExtra("COMMENT", true)
        startActivity(intent)
    }

    fun updateLikeCount(likeCount: Int) {
        val isLiked = viewModel.isLiked.value ?: false

        if (isLiked) {
            val otherCount = likeCount - 1

            val visibility = View.VISIBLE

            likedCountIcon.visibility = visibility
            likedCount.visibility = visibility

            if (otherCount == 0) {
                likedCount.text = getString(R.string.post_like_count_you)
            } else {
                likedCount.text = getString(R.string.post_like_count, otherCount.toString())
            }
            currentPost?.iLikedPost = true
        } else {
            val visibility = if (likeCount > 0) View.VISIBLE else View.INVISIBLE

            likedCountIcon.visibility = visibility
            likedCount.visibility = visibility
            likedCount.text = likeCount.toString()
        }
    }

    fun toggleLike(isLiked: Boolean) {
        if (isLiked) {
            likeButton.setIconResource(R.drawable.ic_thumbsup_fill)
            likeButton.setIconTintResource(R.color.primary)
        } else {
            likeButton.setIconResource(R.drawable.ic_thumbsup)
            likeButton.setIconTintResource(R.color.black)
        }
    }

    private fun initPost() {
        viewModel.getPost(currentPostId)
        viewModel.post.observe(this) { timelinePost ->
            currentPost = timelinePost

            sharedPrefs.currentUserId?.let {
                if (it == timelinePost.userId) {
                    deleteIcon.visibility = View.VISIBLE
                    deleteIcon.setOnClickListener {
                        Dialogs.showDeletePostDialog(this) {
                            viewModel.deletePost(currentPostId)
                        }
                    }
                }
            }

            Glide.with(this)
                .load(timelinePost.profilePicture)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(profileImage)

            userName.text = timelinePost.fullName
            date.append(DateUtils.formatDateToTimeAgo(timelinePost.publishDate))
            content.text = timelinePost.text

            timelinePost.imageUrl?.let {
                image.visibility = View.VISIBLE
                Glide.with(this)
                    .load(it)
                    .into(image)
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

        viewModel.isLiked.observe(this, { toggleLike(it) })
        viewModel.likedCount.observe(this, { updateLikeCount(it) })
    }

    private fun initComments() {
        viewModel.comments.observe(this) {
            if (page == 0 && it.isEmpty()) {
                commentsEmptyText.visibility = View.VISIBLE
                recyclerView.visibility = View.INVISIBLE
            } else {
                commentsEmptyText.visibility = View.INVISIBLE
                recyclerView.visibility = View.VISIBLE
            }

            if (it.isNotEmpty()) {
                adapter?.addItems(it)
                page += 1
            }
        }

        tryLoadComments()
    }

    private fun tryLoadComments(refresh: Boolean = false) {
        if (isCalling) return
        if (refresh) page = 0

        isCalling = true
        viewModel.getComments(currentPostId, limit, limit * page)
    }

    private fun initResponseHandler() {
        viewModel.apiResponse.observe(this, { response ->
            when (response.status) {
                ResponseStatus.API_ERROR -> Toast.makeText(
                    this,
                    getString(R.string.api_error),
                    Toast.LENGTH_LONG
                ).show()
                ResponseStatus.DELETED -> finish()
                ResponseStatus.NOT_FOUND -> {
                    Toast.makeText(this, getString(R.string.api_error_post), Toast.LENGTH_LONG)
                        .show()
                    finish()
                }
                else -> {
                }
            }

            isCalling = false
        })
    }
}