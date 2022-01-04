package nl.inholland.myvitality.ui.profile

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.OnClick
import coil.load
import com.ethanhua.skeleton.RecyclerViewSkeletonScreen
import com.ethanhua.skeleton.Skeleton
import nl.gunther.bryan.newsreader.utils.SharedPreferenceHelper
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.architecture.base.BaseActivity
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.data.adapters.CurrentChallengeAdapter
import nl.inholland.myvitality.data.adapters.ExploreChallengeAdapter
import nl.inholland.myvitality.data.entities.ResponseStatus
import nl.inholland.myvitality.data.entities.User
import nl.inholland.myvitality.ui.authentication.login.LoginActivity
import nl.inholland.myvitality.ui.home.HomeViewModel
import nl.inholland.myvitality.ui.home.HomeViewModelFactory
import nl.inholland.myvitality.ui.widgets.dialog.Dialogs
import nl.inholland.myvitality.util.TextViewUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class ProfileActivity : BaseActivity() {

    @BindView(R.id.profile_image) lateinit var profileImage: ImageView
    @BindView(R.id.profile_fullname) lateinit var fullname: TextView
    @BindView(R.id.profile_details) lateinit var details: TextView
    @BindView(R.id.profile_description) lateinit var description: TextView
    @BindView(R.id.profile_points) lateinit var points: TextView
    @BindView(R.id.profile_button) lateinit var button: Button
    @BindView(R.id.profile_curr_chl_recyclerview) lateinit var currentChallengesRecyclerView: RecyclerView
    @BindView(R.id.profile_finn_chl_recyclerview) lateinit var finishedChallengesRecyclerView: RecyclerView
    @BindView(R.id.profile_curr_chl_title) lateinit var currChlTitle: TextView
    @BindView(R.id.profile_finn_chl_title) lateinit var finnChlTitle: TextView


    @Inject
    lateinit var factory: ProfileViewModelFactory
    lateinit var viewModel: ProfileViewModel

    var crtSkeletonScreen: RecyclerViewSkeletonScreen? = null
    var finnSkeletonScreen: RecyclerViewSkeletonScreen? = null
    var crtLayoutManager: LinearLayoutManager? = null
    var finnLayoutManager: LinearLayoutManager? = null
    var crtChlAdapter: CurrentChallengeAdapter? = null
    var finnChlAdapter: ExploreChallengeAdapter? = null

    var userId: String? = null
    var currentUser: User? = null

    override fun layoutResourceId(): Int {
        return R.layout.activity_profile
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as VitalityApplication).appComponent.inject(this)
        viewModel = ViewModelProviders.of(this, factory).get(ProfileViewModel::class.java)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.navigation_profile)
        supportActionBar?.elevation = 0F

        userId = intent.getStringExtra("USER_ID")

        setupRecyclerViews()
        setupSkeletons()

        initResponseHandler()
        initUser()
        Handler(Looper.getMainLooper()).postDelayed({
            initChallenges()
        }, 1000)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    @OnClick(R.id.profile_button)
    fun onProfileButtonClicked(){
        if(userId != null){
            userId?.let { id ->
                if(viewModel.isFollowing.value == true){
                    Dialogs.showUnfollowDialog(this, currentUser?.firstName) {
                        viewModel.toggleUserFollow(id, false)
                    }
                } else {
                    viewModel.toggleUserFollow(id, true)
                }
            }
        } else {
            Toast.makeText(this, "WIP - Go to profile edit", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupRecyclerViews() {
        crtLayoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        crtChlAdapter = CurrentChallengeAdapter(this, false)

        currentChallengesRecyclerView.let {
            it.adapter = crtChlAdapter
            it.layoutManager = crtLayoutManager
        }

        finnLayoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        finnChlAdapter = ExploreChallengeAdapter(this, false)

        finishedChallengesRecyclerView.let {
            it.adapter = finnChlAdapter
            it.layoutManager = finnLayoutManager
        }
    }

    private fun setupSkeletons() {
        crtSkeletonScreen = Skeleton.bind(currentChallengesRecyclerView)
            .adapter(crtChlAdapter)
            .frozen(true)
            .duration(2400)
            .count(10)
            .load(R.layout.challenge_skeleton_view_item)
            .show()

        finnSkeletonScreen = Skeleton.bind(finishedChallengesRecyclerView)
            .adapter(finnChlAdapter)
            .frozen(true)
            .duration(2400)
            .count(10)
            .load(R.layout.challenge_skeleton_view_item)
            .show()
    }

    private fun initUser(){
        viewModel.getUser(userId)

        viewModel.currentUser.observe(this, { user ->
            currentUser = user
            profileImage.load(user.profilePicture)

            fullname.text = null
            fullname.append("${user.firstName} ${user.lastName}")

            details.text = null
            details.append("${user.jobTitle}, ${user.location}")

            description.text = user.description

            points.append(
                TextViewUtils.getColoredString(
                    user.points.toString() + " ",
                    getColor(R.color.primary)
                )
            )
            points.append(
                TextViewUtils.getColoredString(
                    getString(R.string.profile_points),
                    getColor(R.color.black)
                )
            )

            if(userId.isNullOrBlank()){
                finnChlTitle.text = getString(R.string.profile_your_prize_cabinet)
                button.text = getString(R.string.profile_edit)
                button.visibility = View.VISIBLE
            } else {
                finnChlTitle.text = getString(R.string.profile_prize_cabinet, user.firstName)
            }
        })

        if(userId != null) viewModel.isFollowing.observe(this, { isFollowing ->
            Dialogs.hideCurrentDialog()

            if(isFollowing){
                button.text = getString(R.string.profile_unfollow)
            } else {
                button.text = getString(R.string.profile_follow)
            }

            button.visibility = View.VISIBLE
        })
    }

    private fun initChallenges(){
        viewModel.getChallenges(userId)

        viewModel.currentChallenges.observe(this, { challenges ->
            if(challenges.isEmpty()) {
                currChlTitle.visibility = View.GONE
                currentChallengesRecyclerView.visibility = View.GONE
            } else {
                crtChlAdapter?.addItems(challenges)
            }

            crtSkeletonScreen?.hide()
        })

        viewModel.finishedChallenges.observe(this, { challenges ->
            if(challenges.isEmpty()) {
                finnChlTitle.visibility = View.GONE
                finishedChallengesRecyclerView.visibility = View.GONE
            } else {
                finnChlAdapter?.addItems(challenges)
            }

            finnSkeletonScreen?.hide()
        })
    }

    private fun initResponseHandler(){
        viewModel.apiResponse.observe(this, { response ->
            when(response.status){
                ResponseStatus.UNAUTHORIZED -> {}
                ResponseStatus.API_ERROR -> Toast.makeText(this, getString(R.string.api_error), Toast.LENGTH_LONG).show()
                ResponseStatus.UPDATED_VALUE -> {}
            }
        })
    }
}