package nl.inholland.myvitality.ui.challenge

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import coil.load
import nl.gunther.bryan.newsreader.utils.SharedPreferenceHelper
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.architecture.base.BaseActivity
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.data.adapters.ExploreChallengeAdapter
import nl.inholland.myvitality.data.entities.Challenge
import nl.inholland.myvitality.data.entities.ChallengeProgress
import nl.inholland.myvitality.data.entities.ChallengeType
import nl.inholland.myvitality.ui.MainActivity
import nl.inholland.myvitality.ui.authentication.login.LoginActivity
import nl.inholland.myvitality.ui.widgets.dialog.Dialogs
import nl.inholland.myvitality.util.TextViewUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.stream.Collectors
import javax.inject.Inject

class ChallengeActivity : BaseActivity(), Callback<Challenge> {
    @Inject lateinit var apiClient: ApiClient
    @Inject lateinit var sharedPrefs: SharedPreferenceHelper

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

    var layoutManager: LinearLayoutManager? = null
    var adapter: ExploreChallengeAdapter? = null
    var currentChallengeId: String = ""

    override fun layoutResourceId(): Int {
        return R.layout.activity_challenge
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as VitalityApplication).appComponent.inject(this)
        sharedPrefs = SharedPreferenceHelper(this)

        val challengeId = intent.getStringExtra("CHALLENGE_ID")

        if(challengeId == null){
            finish()
        } else {
            currentChallengeId = challengeId
        }

        setupRecyclerView()
        setupVideoView()
        tryLoadChallenge()
    }

    @OnClick(R.id.back_button)
    override fun onBackPressed() {
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun setupRecyclerView() {
        layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        adapter = ExploreChallengeAdapter(this)

        exploreActivitiesRecyclerView.let {
            it.adapter = adapter
            it.layoutManager = layoutManager
        }
    }

    private fun tryLoadChallenge() {
        sharedPrefs.accessToken?.let {
            apiClient.getChallenge("Bearer $it", currentChallengeId).enqueue(this)
        }
    }

    private fun tryLoadExpChallenges(challengeType: ChallengeType) {
        sharedPrefs.accessToken?.let {
            apiClient.getChallenges(
                "Bearer $it",
                10,
                0,
                challengeType = challengeType.id,
                progress = ChallengeProgress.NOT_SUBSCRIBED.id
            ).enqueue(object :
                Callback<List<Challenge>> {
                override fun onResponse(
                    call: Call<List<Challenge>>,
                    response: Response<List<Challenge>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        response.body()?.let { result ->
                            val exploreChallenges =
                                result.stream().filter { o -> o.challengeId != currentChallengeId }
                                    .collect(
                                        Collectors.toList()
                                    )

                            adapter?.addItems(exploreChallenges)
                        }
                    }
                }

                override fun onFailure(call: Call<List<Challenge>>, t: Throwable) {
                    Log.e("HTTP", "Could net fetch articles", t)
                }
            })
        }
    }

    private fun loadChallenge(challenge: Challenge){
        // Load the correct views based on the challenge progress
        when(challenge.challengeProgress){
            ChallengeProgress.NOT_SUBSCRIBED, ChallengeProgress.CANCELLED -> {
                startChallengeButton.visibility = View.VISIBLE
                participantsCount.visibility = View.VISIBLE
                exploreActivitiesRecyclerView.visibility = View.VISIBLE
                findViewById<ImageView>(R.id.challenge_exp_chl_icon).visibility = View.VISIBLE
                findViewById<TextView>(R.id.challenge_exp_chl_title).visibility = View.VISIBLE

                // Load explorable challenges
                tryLoadExpChallenges(challenge.challengeType)

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

        image.load(challenge.imageLink)

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
        date.append(TextViewUtils.formatDate(challenge.startDate))

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
            participantsCount.text = "${challenge.totalSubscribers} collega('s) doen al mee"
        }
    }

    private fun setupVideoView() {
        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)
    }

    private fun updateChallengeProgress(challengeProgress: ChallengeProgress){
        sharedPrefs.accessToken?.let {
            apiClient.updateChallengeProgress("Bearer $it", currentChallengeId, challengeProgress.id)
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if(response.isSuccessful){
                            startActivity(Intent(this@ChallengeActivity, MainActivity::class.java))
                            finish()
                        } else {
                            if(response.code() == 401){
                                startActivity(Intent(this@ChallengeActivity, LoginActivity::class.java))
                            }
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@ChallengeActivity, getString(R.string.api_error), Toast.LENGTH_LONG).show()
                    }
                })
        }
    }


    override fun onResponse(call: Call<Challenge>, response: Response<Challenge>) {
        if (response.isSuccessful && response.body() != null) {
            val challenge = response.body()!!
            loadChallenge(challenge)
        } else {
            Toast.makeText(this, getString(R.string.api_error), Toast.LENGTH_LONG).show()
        }
    }


    override fun onFailure(call: Call<Challenge>, t: Throwable) {
        Toast.makeText(this, getString(R.string.api_error), Toast.LENGTH_LONG).show()
    }
}