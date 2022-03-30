package nl.inholland.myvitality.ui.activity.detail

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.Group
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.OnClick
import com.bumptech.glide.Glide
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.architecture.base.BaseActivity
import nl.inholland.myvitality.data.adapters.ActivityAdapter
import nl.inholland.myvitality.data.entities.Activity
import nl.inholland.myvitality.data.entities.ActivityProgress
import nl.inholland.myvitality.data.entities.ActivityType
import nl.inholland.myvitality.data.entities.ResponseStatus
import nl.inholland.myvitality.ui.activity.overview.ActivityOverviewActivity
import nl.inholland.myvitality.ui.activity.participants.ActivityParticipantsActivity
import nl.inholland.myvitality.ui.widgets.dialog.Dialogs
import nl.inholland.myvitality.util.DateUtils
import nl.inholland.myvitality.util.TextViewUtils
import java.lang.Exception
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import javax.inject.Inject

class ActivityDetailActivity : BaseActivity() {

    @BindView(R.id.activity_image)
    lateinit var image: ImageView
    @BindView(R.id.activity_type)
    lateinit var type: TextView
    @BindView(R.id.activity_date)
    lateinit var date: TextView
    @BindView(R.id.activity_title)
    lateinit var title: TextView
    @BindView(R.id.activity_points)
    lateinit var points: TextView
    @BindView(R.id.activity_description)
    lateinit var description: TextView
    @BindView(R.id.activity_video_container)
    lateinit var videoContainer: FrameLayout
    @BindView(R.id.activity_video)
    lateinit var videoView: VideoView
    @BindView(R.id.activity_participants)
    lateinit var participantsCount: TextView
    @BindView(R.id.activity_recommended_activities_recyclerview)
    lateinit var exploreActivitiesRecyclerView: RecyclerView
    @BindView(R.id.activity_start_button)
    lateinit var startActivityButton: Button
    @BindView(R.id.activity_cancel_button)
    lateinit var cancelActivityButton: AppCompatButton
    @BindView(R.id.activity_open_url_button)
    lateinit var urlButton: AppCompatButton
    @BindView(R.id.activity_complete_button)
    lateinit var completeActivityButton: Button
    @BindView(R.id.activity_recommended_activities)
    lateinit var recommendedActivities: Group

    @Inject
    lateinit var factory: ActivityViewModelFactory
    lateinit var viewModel: ActivityViewModel

    private var layoutManager: LinearLayoutManager? = null
    var adapter: ActivityAdapter? = null
    var currentActivity: Activity? = null
    var currentActivityId: String = ""
    var requestedShareUrl = false

    override fun layoutResourceId(): Int {
        return R.layout.activity_challenge
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as VitalityApplication).appComponent.inject(this)
        viewModel = ViewModelProviders.of(this, factory).get(ActivityViewModel::class.java)

        handleDynamicLink()

        setupRecyclerView()
        setupVideoView()

        initResponseHandler()
        handleProgressChanges()
    }

    private fun handleDynamicLink() {
        Firebase.dynamicLinks
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
                Log.i("ActivityDetailsActivity", "We have a dynamic link")

                var deepLink: Uri? = null
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                }

                currentActivityId = if (deepLink != null) {
                    deepLink.getQueryParameter("activityId") ?: ""
                } else {
                    intent.getStringExtra("ACTIVITY_ID") ?: ""
                }

                if (currentActivityId.isEmpty()) finish() else initActivity()
            }
            .addOnFailureListener(this) { e ->
                Log.w(
                    "ActivityDetailsActivity",
                    "getDynamicLink:onFailure",
                    e
                )
            }
    }


    @OnClick(R.id.back_button)
    override fun onBackPressed() {
        super.onBackPressed()
    }

    @OnClick(R.id.share_button)
    fun onClickShare() {
        if(requestedShareUrl) return

        viewModel.createShareLink(currentActivityId)
        requestedShareUrl = true

        viewModel.shareableLink.observe(this) {
            requestedShareUrl = false

            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(
                    Intent.EXTRA_TEXT,
                    getString(R.string.activity_share_message, currentActivity?.title) + "\n $it"
                )
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }
    }

    private fun handleProgressChanges(){
        viewModel.activityProgress.observe(this) { progress ->
            Dialogs.hideCurrentDialog()

            val showWhenInProgress = if(progress.equals(ActivityProgress.IN_PROGRESS)) View.VISIBLE else View.GONE
            val showWhenNotSubscribed = if(progress.equals(ActivityProgress.NOT_SUBSCRIBED) || progress.equals(ActivityProgress.CANCELLED)) View.VISIBLE else View.GONE

            startActivityButton.visibility = showWhenNotSubscribed
            participantsCount.visibility = showWhenNotSubscribed
            recommendedActivities.visibility = showWhenNotSubscribed

            cancelActivityButton.visibility = showWhenInProgress
            completeActivityButton.visibility = showWhenInProgress
            findViewById<ImageButton>(R.id.calendar_button).visibility = showWhenInProgress

            when(progress){
                ActivityProgress.NOT_SUBSCRIBED, ActivityProgress.CANCELLED -> {
                    // Load recommended activities
                    initRecommendedActivities()

                    startActivityButton.setOnClickListener {
                        updateActivityProgress(ActivityProgress.IN_PROGRESS)
                    }
                }
                ActivityProgress.IN_PROGRESS -> {
                    cancelActivityButton.setOnClickListener {
                        Dialogs.showCancelChallengeDialog(this) {
                            updateActivityProgress(ActivityProgress.CANCELLED)
                        }
                    }

                    completeActivityButton.setOnClickListener {
                        updateActivityProgress(ActivityProgress.DONE)
                    }
                }
                ActivityProgress.DONE -> {
                    startActivity(
                        Intent(this@ActivityDetailActivity, ActivityOverviewActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            .putExtra("CATEGORY_ID", currentActivity?.category?.categoryId))
                    finish()
                }
                else -> {}
            }
        }
    }

    private fun initActivity() {
        viewModel.getActivity(currentActivityId)
        viewModel.currentActivity.observe(this) { activity ->
            currentActivity = activity

            if(activity.activityProgress == ActivityProgress.NOT_SUBSCRIBED
                || activity.activityProgress == ActivityProgress.CANCELLED){
                if (!activity.signUpOpen) startActivityButton.isEnabled = false
            }

            completeActivityButton.isEnabled = activity.hasStarted

            activity.url?.let { url ->
                urlButton.visibility = View.VISIBLE
                urlButton.setOnClickListener {
                    val uri: Uri = Uri.parse(url)

                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }
            }

            Glide.with(this)
                .load(activity.imageLink)
                .into(image)

            // Set the activity type
            when (activity.activityType) {
                ActivityType.EXERCISE ->
                    type.text = getString(R.string.activity_type_exercise)
                ActivityType.DIET ->
                    type.text = getString(R.string.activity_type_diet)
                ActivityType.MIND ->
                    type.text = getString(R.string.activity_type_mind)
            }

            // Set the date
            if (DateUtils.isInPast(activity.startDate)) {
                date.text = getString(
                    R.string.activity_end_date_text,
                    DateUtils.formatDate(activity.endDate, "dd MMMM yyyy HH:mm")
                )
            } else {
                date.text = getString(
                    R.string.activity_start_date_text,
                    DateUtils.formatDate(activity.startDate, "dd MMMM yyyy HH:mm")
                )
            }

            // Set the title of the activity
            title.text = activity.title

            activity.videoLink?.let {
                videoContainer.visibility = View.VISIBLE
                videoView.setVideoPath(it)
                videoView.start()
            }

            points.append(
                TextViewUtils.getColoredString(
                    getString(R.string.activity_points_message_1),
                    getColor(R.color.black)
                )
            )
            points.append(
                TextViewUtils.getColoredString(
                    " ${activity.points} ",
                    getColor(R.color.primary)
                )
            )
            points.append(
                TextViewUtils.getColoredString(
                    getString(R.string.activity_points_message_2),
                    getColor(R.color.black)
                )
            )

            description.text = Html.fromHtml(activity.description, Html.FROM_HTML_MODE_LEGACY)
            if (activity.signUpOpen) {
                if (activity.totalSubscribers ?: 0 >= 1) {
                    participantsCount.text = null
                    participantsCount.append(
                        getString(
                            R.string.activity_participating_text,
                            activity.totalSubscribers
                        )
                    )

                    participantsCount.setOnClickListener {
                        startActivity(
                            Intent(this, ActivityParticipantsActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                .putExtra("ACTIVITY_ID", currentActivity?.activityId)
                        )
                    }
                }
            } else {
                participantsCount.text = getString(R.string.activity_starting_soon)
            }
        }
    }

    private fun initRecommendedActivities() {
        viewModel.getRecommendedActivities()
        viewModel.recommendedActivities.observe(this) {
            val visibility = if (it.isEmpty()) View.INVISIBLE else View.VISIBLE
            findViewById<Group>(R.id.activity_recommended_activities).visibility = visibility

            adapter?.addItems(it)
        }
    }

    private fun updateActivityProgress(activityProgress: ActivityProgress) {
        viewModel.updateActivityProgress(activityProgress, currentActivity!!.activityId)
    }

    private fun setupRecyclerView() {
        layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        adapter = ActivityAdapter(this)

        exploreActivitiesRecyclerView.let {
            it.adapter = adapter
            it.layoutManager = layoutManager
        }
    }

    private fun setupVideoView() {
        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)
    }

    @OnClick(R.id.calendar_button)
    fun onClickCalendarIcon() {
        Dialogs.showCalendarEventDialog(this) {
            val intent = Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.Events.TITLE, currentActivity!!.title)
                .putExtra(
                    CalendarContract.Events.DESCRIPTION,
                    currentActivity!!.description
                )
                .putExtra(
                    CalendarContract.Events.EVENT_LOCATION,
                    currentActivity!!.location
                )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val startDate = LocalDateTime.parse(currentActivity!!.startDate)
                val endDate = LocalDateTime.parse(currentActivity!!.endDate)
                intent.putExtra(
                    CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                    startDate.atZone(ZoneOffset.ofHours(1)).toInstant().toEpochMilli()
                )
                intent.putExtra(
                    CalendarContract.EXTRA_EVENT_END_TIME,
                    endDate.atZone(ZoneOffset.ofHours(1)).toInstant().toEpochMilli()
                )
            }

            Dialogs.hideCurrentDialog()
            startActivity(intent)
        }
    }

    private fun initResponseHandler() {
        viewModel.apiResponse.observe(this) { response ->
            when (response.status) {
                ResponseStatus.NOT_FOUND -> finish()
                ResponseStatus.API_ERROR -> Toast.makeText(
                    this,
                    getString(R.string.api_error),
                    Toast.LENGTH_LONG
                ).show()
                else -> {
                }
            }
        }
    }
}