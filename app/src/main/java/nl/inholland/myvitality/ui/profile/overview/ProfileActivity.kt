package nl.inholland.myvitality.ui.profile.overview

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.Group
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.OnClick
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ethanhua.skeleton.RecyclerViewSkeletonScreen
import com.ethanhua.skeleton.Skeleton
import com.microsoft.identity.client.IPublicClientApplication
import com.microsoft.identity.client.ISingleAccountPublicClientApplication
import com.microsoft.identity.client.PublicClientApplication
import com.microsoft.identity.client.exception.MsalException
import nl.inholland.myvitality.util.SharedPreferenceHelper
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.architecture.base.BaseActivity
import nl.inholland.myvitality.data.TokenApiClient
import nl.inholland.myvitality.data.adapters.ActivityAdapter
import nl.inholland.myvitality.data.adapters.PersonalScoreboardAdapter
import nl.inholland.myvitality.data.entities.ResponseStatus
import nl.inholland.myvitality.data.entities.User
import nl.inholland.myvitality.ui.authentication.login.LoginActivity
import nl.inholland.myvitality.ui.profile.edit.ProfileEditActivity
import nl.inholland.myvitality.ui.widgets.dialog.Dialogs
import nl.inholland.myvitality.util.TextViewUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject


class ProfileActivity : BaseActivity() {

    @Inject lateinit var sharedPrefs: SharedPreferenceHelper
    @Inject lateinit var apiClient: TokenApiClient

    @BindView(R.id.profile_image) lateinit var profileImage: ImageView
    @BindView(R.id.profile_fullname) lateinit var fullname: TextView
    @BindView(R.id.profile_details) lateinit var details: TextView
    @BindView(R.id.profile_description) lateinit var description: TextView
    @BindView(R.id.profile_points) lateinit var points: TextView
    @BindView(R.id.profile_button) lateinit var button: Button
    @BindView(R.id.profile_current_activities_recyclerview) lateinit var userActivitiesRecyclerView: RecyclerView
    @BindView(R.id.profile_personal_scoreboard_recyclerview) lateinit var personalScoreboardRecyclerView: RecyclerView
    @BindView(R.id.profile_personal_scoreboard_title) lateinit var personalScoreboardTitle: TextView

    @Inject
    lateinit var factory: ProfileViewModelFactory
    lateinit var viewModel: ProfileViewModel

    private var mSingleAccountApp: ISingleAccountPublicClientApplication? = null
    var userActivitiesSkeletonScreen: RecyclerViewSkeletonScreen? = null
    var personalScoreboardSkeletonScreen: RecyclerViewSkeletonScreen? = null
    var userActivitiesAdapter: ActivityAdapter? = null
    var personalScoreboardAdapter: PersonalScoreboardAdapter? = null

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

        sharedPrefs.currentUserId?.let{
            val foundUserId = intent.getStringExtra("USER_ID")

            if(it != foundUserId){
                userId = foundUserId
            }
        }

        setupRecyclerViews()
        setupSkeletons()

        initResponseHandler()
        initUser()

        Handler(Looper.getMainLooper()).postDelayed({
            initChallenges()
        }, 1000)

        Handler(Looper.getMainLooper()).postDelayed({
            initScoreboard()
        }, 1000)

        PublicClientApplication.createSingleAccountPublicClientApplication(getApplicationContext(),
            R.raw.auth_config_single_account, object : IPublicClientApplication.ISingleAccountApplicationCreatedListener {
                override fun onCreated(application: ISingleAccountPublicClientApplication?) {
                    mSingleAccountApp = application
                }
                override fun onError(exception: MsalException) {
                    Log.i("ProfileActivity", exception.message.toString())
                }
            })
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if(userId == null) menuInflater.inflate(R.menu.profile_nav_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        return if(id == R.id.profile_logout){
            sharedPrefs.pushToken?.let {
                apiClient.deletePushToken("Bearer ${sharedPrefs.accessToken}", it).enqueue(object :
                    Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        Log.i("ProfileActivity", "Push token deleted")
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Log.e("ProfileActivity", "onFailure: ", t)
                    }
                })
            }

            Log.e("ProfileActivity" , "Executing Azure Logout")

            mSingleAccountApp!!.signOut(object : ISingleAccountPublicClientApplication.SignOutCallback {
                override fun onSignOut() {
                    Log.i("ProfileActivity" , "Successfully signed out of Azure AD")
                }

                override fun onError(exception: MsalException) {
                    Log.i("ProfileActivity" , exception.message.toString())
                }
            })

            sharedPrefs.logoutUser()
            finishAffinity()
            startActivity(Intent(this, LoginActivity::class.java))
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    @OnClick(R.id.profile_button)
    fun onProfileButtonClicked(){
        if(userId != null){
            userId?.let { id ->
                if(viewModel.isFollowing.value == true){
                    Dialogs.showUnfollowDialog(this, currentUser?.firstName) {
                        viewModel.unfollowUser(id)
                    }
                } else {
                    viewModel.followUser(id)
                }
            }
        } else {
            startActivity(Intent(this, ProfileEditActivity::class.java))
            finish()
        }
    }

    private fun setupRecyclerViews() {
        userActivitiesAdapter = ActivityAdapter(this)

        userActivitiesRecyclerView.let {
            it.adapter = userActivitiesAdapter
            it.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        }

        personalScoreboardAdapter = PersonalScoreboardAdapter(this)

        personalScoreboardRecyclerView.let {
            it.adapter = personalScoreboardAdapter
            it.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun setupSkeletons() {
        userActivitiesSkeletonScreen = Skeleton.bind(userActivitiesRecyclerView)
            .adapter(userActivitiesAdapter)
            .frozen(true)
            .duration(2400)
            .count(10)
            .load(R.layout.activity_skeleton_view_item)
            .show()

        personalScoreboardSkeletonScreen = Skeleton.bind(personalScoreboardRecyclerView)
            .adapter(personalScoreboardAdapter)
            .frozen(true)
            .duration(2400)
            .count(10)
            .load(R.layout.activity_skeleton_view_item)
            .show()
    }

    private fun initUser(){
        viewModel.getUser(userId)

        viewModel.currentUser.observe(this) { user ->
            currentUser = user

            Glide.with(this)
                .load(user.profilePicture)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(profileImage)

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

            if (userId.isNullOrBlank()) {
                personalScoreboardTitle.text = getString(R.string.profile_your_prize_cabinet)
                button.text = getString(R.string.profile_edit)
                button.visibility = View.VISIBLE
            } else {
                personalScoreboardTitle.text = getString(R.string.profile_prize_cabinet, user.firstName)
            }
        }

        if(userId != null) viewModel.isFollowing.observe(this) { isFollowing ->
            Dialogs.hideCurrentDialog()

            if (isFollowing) {
                button.text = getString(R.string.profile_unfollow)
            } else {
                button.text = getString(R.string.profile_follow)
            }

            button.visibility = View.VISIBLE
        }
    }

    private fun initChallenges(){
        viewModel.getActivities(userId)

        viewModel.currentActivities.observe(this) { activities ->
            val visibility = if(activities.isEmpty()) View.GONE else View.VISIBLE
            findViewById<Group>(R.id.profile_current_activities).visibility = visibility

            userActivitiesAdapter?.addItems(activities)
            userActivitiesSkeletonScreen?.hide()
        }
    }

    private fun initScoreboard(){
        viewModel.getUserScoreboard(userId)

        viewModel.personalScoreboard.observe(this) { results ->
            val visibility = if(results.isEmpty()) View.GONE else View.VISIBLE
            findViewById<Group>(R.id.profile_personal_scoreboard).visibility = visibility

            personalScoreboardAdapter?.addItems(results)
            personalScoreboardSkeletonScreen?.hide()
        }
    }

    private fun initResponseHandler(){
        viewModel.apiResponse.observe(this) { response ->
            when (response.status) {
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