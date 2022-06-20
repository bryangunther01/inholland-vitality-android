package nl.inholland.myvitality.ui.activity.detail

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.MediaController
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import butterknife.OnClick
import com.bumptech.glide.Glide
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_details_activity.*
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.architecture.base.BaseActivity
import nl.inholland.myvitality.data.adapters.ActivityAdapter
import nl.inholland.myvitality.data.entities.Activity
import nl.inholland.myvitality.data.entities.ActivityProgress
import nl.inholland.myvitality.data.entities.ActivityType
import nl.inholland.myvitality.data.entities.ResponseStatus
import nl.inholland.myvitality.databinding.ActivityDetailsActivityBinding
import nl.inholland.myvitality.ui.achievement.AchievementActivity
import nl.inholland.myvitality.ui.activity.participants.ActivityParticipantsActivity
import nl.inholland.myvitality.ui.widgets.dialog.Dialogs
import nl.inholland.myvitality.util.DateUtils
import nl.inholland.myvitality.util.StringUtils.toHtmlSpan
import nl.inholland.myvitality.util.TextViewUtils
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.inject.Inject

class ActivityDetailActivity : BaseActivity<ActivityDetailsActivityBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivityDetailsActivityBinding
            = ActivityDetailsActivityBinding::inflate

    @Inject
    lateinit var factory: ActivityViewModelFactory
    lateinit var viewModel: ActivityViewModel

    private var layoutManager: LinearLayoutManager? = null
    private var adapter: ActivityAdapter? = null
    private var currentActivity: Activity? = null
    private var currentActivityId: String = ""
    private var requestedShareUrl = false

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

    /**
     * Method which checks if there is a dynamic links
     * true = Read the activityId from the link
     * false = Read the activityId from the intent extra
     */
    private fun handleDynamicLink() {
        Firebase.dynamicLinks
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
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

            // Open the share intent with the shareable link
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

    /**
     * Method which observes changes of the progress to show the right buttons and information at specific progress states
     */
    private fun handleProgressChanges(){
        viewModel.activityProgress.observe(this) { progress ->
            Dialogs.hideCurrentDialog()

            val showWhenInProgress = if(progress.equals(ActivityProgress.IN_PROGRESS)) View.VISIBLE else View.GONE
            val showWhenNotSubscribed = if(progress.equals(ActivityProgress.NOT_SUBSCRIBED) || progress.equals(ActivityProgress.CANCELLED) && currentActivity?.hasEnded == false) View.VISIBLE else View.GONE

            binding.startButton.visibility = showWhenNotSubscribed
            binding.participantsCount.visibility = showWhenNotSubscribed
            binding.recommendedActivities.visibility = showWhenNotSubscribed

            binding.cancelButton.visibility = showWhenInProgress
            binding.completeButton.visibility = showWhenInProgress

            currentActivity?.url?.let {
                binding.openUrlButton.visibility = showWhenInProgress
            }

            binding.calendarButton.visibility = showWhenInProgress

            when(progress){
                ActivityProgress.NOT_SUBSCRIBED, ActivityProgress.CANCELLED -> {
                    // Load recommended activities
                    initRecommendedActivities()

                    binding.startButton.setOnClickListener {
                        updateActivityProgress(ActivityProgress.IN_PROGRESS)
                    }
                }
                ActivityProgress.IN_PROGRESS -> {
                    binding.cancelButton.setOnClickListener {
                        Dialogs.showCancelChallengeDialog(this) {
                            updateActivityProgress(ActivityProgress.CANCELLED)
                        }
                    }

                    binding.completeButton.setOnClickListener {
                        updateActivityProgress(ActivityProgress.DONE)
                    }
                }
                ActivityProgress.DONE -> {
                    finish()
                }
            }
        }
    }

    private fun initActivity() {
        viewModel.getActivity(currentActivityId)
        viewModel.currentActivity.observe(this) { activity ->
            currentActivity = activity

            if(activity.activityProgress == ActivityProgress.NOT_SUBSCRIBED
                || activity.activityProgress == ActivityProgress.CANCELLED){
                if (!activity.signUpOpen) binding.startButton.isEnabled = false
            }

            binding.completeButton.isEnabled = activity.hasStarted

            activity.url?.let { url ->
                binding.openUrlButton.setOnClickListener {
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
            when {
                DateUtils.isInPast(activity.endDate) -> {
                    binding.subtitle.text = getString(
                        R.string.activity_ended_text,
                        DateUtils.formatDate(activity.endDate, "dd MMMM yyyy")
                    )
                }
                DateUtils.isInPast(activity.startDate) -> {
                    binding.subtitle.text = getString(
                        R.string.activity_end_date_text,
                        DateUtils.formatDate(activity.endDate, "dd MMMM yyyy HH:mm")
                    )
                }
                else -> {
                    binding.subtitle.text = getString(
                        R.string.activity_start_date_text,
                        DateUtils.formatDate(activity.startDate, "dd MMMM yyyy HH:mm")
                    )
                }
            }

            // Set the title of the activity
            binding.title.text = activity.title

            activity.videoLink?.let {
                binding.videoContainer.visibility = View.VISIBLE
                binding.video.setVideoPath(it)
                binding.video.start()
            }

            if(points.text.isEmpty()) {
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
            }

            description.text = activity.description.toHtmlSpan()
            location.text = getString(R.string.activity_location_text, activity.location).toHtmlSpan()

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
            binding.recommendedActivities.visibility = visibility

            adapter?.addItems(it)
        }
    }

    private fun updateActivityProgress(activityProgress: ActivityProgress) {
        viewModel.updateActivityProgress(activityProgress, currentActivity!!.activityId)

        viewModel.achievements.observe(this) {
            startActivity(
                Intent(this, AchievementActivity::class.java)
                    .putParcelableArrayListExtra("ACHIEVEMENT_LIST", ArrayList(it)))
        }
    }

    private fun setupRecyclerView() {
        layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        adapter = ActivityAdapter(this)

        binding.recommendedActivitiesRecyclerview.let {
            it.adapter = adapter
            it.layoutManager = layoutManager
        }
    }

    private fun setupVideoView() {
        val mediaController = MediaController(this)
        mediaController.setAnchorView(binding.video)
        binding.video.setMediaController(mediaController)
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