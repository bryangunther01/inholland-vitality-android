package nl.inholland.myvitality.ui.challenge

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.OnClick
import com.bumptech.glide.Glide
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.architecture.base.BaseActivity
import nl.inholland.myvitality.data.adapters.ExploreChallengeAdapter
import nl.inholland.myvitality.data.entities.ChallengeProgress
import nl.inholland.myvitality.data.entities.ChallengeType
import nl.inholland.myvitality.data.entities.ResponseStatus
import nl.inholland.myvitality.ui.MainActivity
import nl.inholland.myvitality.ui.widgets.dialog.Dialogs
import nl.inholland.myvitality.util.DateUtils
import nl.inholland.myvitality.util.TextViewUtils
import javax.inject.Inject

class ChallengeActivity : BaseActivity() {

    @BindView(R.id.challenge_image) lateinit var image: ImageView
    @BindView(R.id.challenge_type) lateinit var type: TextView
    @BindView(R.id.challenge_date) lateinit var date: TextView
    @BindView(R.id.challenge_title) lateinit var title: TextView
    @BindView(R.id.challenge_points) lateinit var points: TextView
    @BindView(R.id.challenge_description) lateinit var description: TextView
    @BindView(R.id.challenge_video_container) lateinit var videoContainer: FrameLayout
    @BindView(R.id.challenge_video) lateinit var videoView: VideoView
    @BindView(R.id.challenge_participants) lateinit var participantsCount: TextView
    @BindView(R.id.challenge_exp_chl_recyclerview) lateinit var exploreActivitiesRecyclerView: RecyclerView
    @BindView(R.id.challenge_start_button) lateinit var startChallengeButton: Button
    @BindView(R.id.challenge_cancel_button) lateinit var cancelChallengeButton: AppCompatButton
    @BindView(R.id.challenge_complete_button) lateinit var completeChallengeButton: Button

    @Inject
    lateinit var factory: ChallengeViewModelFactory
    lateinit var viewModel: ChallengeViewModel

    private var layoutManager: LinearLayoutManager? = null
    var adapter: ExploreChallengeAdapter? = null
    var currentChallengeId: String = ""

    override fun layoutResourceId(): Int {
        return R.layout.activity_challenge
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as VitalityApplication).appComponent.inject(this)
        val challengeId = intent.getStringExtra("CHALLENGE_ID")
        if(challengeId == null) finish() else currentChallengeId = challengeId

        viewModel = ViewModelProviders.of(this, factory).get(ChallengeViewModel::class.java)

        setupRecyclerView()
        setupVideoView()

        initResponseHandler()
        initChallenge()
    }

    @OnClick(R.id.back_button)
    override fun onBackPressed() {
        super.onBackPressed()
    }

    private fun initChallenge() {
        viewModel.getChallenge(currentChallengeId)
        viewModel.currentChallenge.observe(this, Observer { challenge ->
            // Load the correct views based on the challenge progress
            when(challenge.challengeProgress){
                ChallengeProgress.NOT_SUBSCRIBED, ChallengeProgress.CANCELLED -> {
                    startChallengeButton.visibility = View.VISIBLE
                    participantsCount.visibility = View.VISIBLE

                    // Load explorable challenges
                    initExplorableChallenges(challenge.challengeType)

                    startChallengeButton.setOnClickListener {
                        updateChallengeProgress(ChallengeProgress.IN_PROGRESS)
                    }
                }
                ChallengeProgress.IN_PROGRESS -> {
                    cancelChallengeButton.visibility = View.VISIBLE
                    completeChallengeButton.visibility = View.VISIBLE

                    cancelChallengeButton.setOnClickListener {
                        Dialogs.showCancelChallengeDialog(this) {
                            updateChallengeProgress(ChallengeProgress.CANCELLED)
                        }
                    }

                    completeChallengeButton.setOnClickListener {
                        updateChallengeProgress(ChallengeProgress.DONE)
                    }
                }
                ChallengeProgress.DONE -> {}
            }

            Glide.with(this)
                .load(challenge.imageLink)
                .into(image)

            // Set the challenge type
            when (challenge.challengeType) {
                ChallengeType.EXERCISE ->
                    type.text = getString(R.string.challenge_type_exercise)
                ChallengeType.DIET ->
                    type.text = getString(R.string.challenge_type_diet)
                ChallengeType.MIND ->
                    type.text = getString(R.string.challenge_type_mind)
            }

            // Set the start date
            date.append(getString(R.string.challenge_date_text) + " ")
            date.append(DateUtils.formatDate(challenge.startDate))

            // Set the title of the challenge
            title.text = challenge.title

            challenge.videoLink?.let {
                videoContainer.visibility = View.VISIBLE
                videoView.setVideoPath(it)
                videoView.start()
            }

            points.append(
                TextViewUtils.getColoredString(
                    getString(R.string.challenge_points_message_1),
                    getColor(R.color.black)
                )
            )
            points.append(
                TextViewUtils.getColoredString(
                    " ${challenge.points} ",
                    getColor(R.color.primary)
                )
            )
            points.append(
                TextViewUtils.getColoredString(
                    getString(R.string.challenge_points_message_2),
                    getColor(R.color.black)
                )
            )

            description.text = Html.fromHtml(challenge.description, Html.FROM_HTML_MODE_LEGACY)
            if (challenge.totalSubscribers ?: 0 >= 1) {
                participantsCount.text = null
                participantsCount.append("${challenge.totalSubscribers} collega('s) doen al mee")
            }
        })
    }

    private fun initExplorableChallenges(challengeType: ChallengeType) {
        viewModel.getChallenges(challengeType, currentChallengeId)
        viewModel.explorableChallenges.observe(this, Observer {
            val visibility = if(it.isEmpty()) View.INVISIBLE else View.VISIBLE
            exploreActivitiesRecyclerView.visibility = visibility
            findViewById<ImageView>(R.id.challenge_exp_chl_icon).visibility = visibility
            findViewById<TextView>(R.id.challenge_exp_chl_title).visibility = visibility

            adapter?.addItems(it)
        })
    }

    private fun updateChallengeProgress(challengeProgress: ChallengeProgress){
        viewModel.updateChallengeProgress(challengeProgress, currentChallengeId)
    }

    private fun setupRecyclerView() {
        layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        adapter = ExploreChallengeAdapter(this)

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


    private fun initResponseHandler(){
        viewModel.apiResponse.observe(this, { response ->
            when(response.status){
                ResponseStatus.NOT_FOUND -> finish()
                ResponseStatus.API_ERROR -> Toast.makeText(this, getString(R.string.api_error), Toast.LENGTH_LONG).show()
                ResponseStatus.UPDATED_VALUE -> {
                    startActivity(Intent(this@ChallengeActivity, MainActivity::class.java))
                    finish()
                }
                else -> {}
            }
        })
    }
}