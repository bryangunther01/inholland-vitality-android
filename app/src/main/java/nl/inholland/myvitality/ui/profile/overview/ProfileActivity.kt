package nl.inholland.myvitality.ui.profile.overview

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ethanhua.skeleton.RecyclerViewSkeletonScreen
import com.ethanhua.skeleton.Skeleton
import com.microsoft.identity.client.IPublicClientApplication
import com.microsoft.identity.client.ISingleAccountPublicClientApplication
import com.microsoft.identity.client.PublicClientApplication
import com.microsoft.identity.client.exception.MsalException
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.architecture.base.BaseActivity
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.data.adapters.AchievementAdapter
import nl.inholland.myvitality.data.adapters.ActivityAdapter
import nl.inholland.myvitality.data.entities.ResponseStatus
import nl.inholland.myvitality.data.entities.User
import nl.inholland.myvitality.databinding.ActivityProfileBinding
import nl.inholland.myvitality.ui.authentication.login.LoginActivity
import nl.inholland.myvitality.ui.profile.edit.ProfileEditActivity
import nl.inholland.myvitality.ui.widgets.dialog.Dialogs
import nl.inholland.myvitality.util.SharedPreferenceHelper
import nl.inholland.myvitality.util.StringUtils.toHtmlSpan
import nl.inholland.myvitality.util.TextViewUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject


class ProfileActivity : BaseActivity<ActivityProfileBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivityProfileBinding
            = ActivityProfileBinding::inflate

    @Inject lateinit var sharedPrefs: SharedPreferenceHelper
    @Inject lateinit var apiClient: ApiClient

    @Inject
    lateinit var factory: ProfileViewModelFactory
    lateinit var viewModel: ProfileViewModel

    private var mSingleAccountApp: ISingleAccountPublicClientApplication? = null
    var userActivitiesSkeletonScreen: RecyclerViewSkeletonScreen? = null
    var personalScoreboardSkeletonScreen: RecyclerViewSkeletonScreen? = null
    var userActivitiesAdapter: ActivityAdapter? = null
    var achievementAdapter: AchievementAdapter? = null

    var userId: String? = null
    var currentUser: User? = null

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

        setupButton()
        setupRecyclerViews()
        setupSkeletons()

        initResponseHandler()
        initUser()

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

    private fun setupRecyclerViews() {
        userActivitiesAdapter = ActivityAdapter(this)

        binding.currentActivitiesRecyclerview.let {
            it.adapter = userActivitiesAdapter
            it.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        }

        achievementAdapter = AchievementAdapter(this)

        binding.achievementRecyclerView.let {
            it.adapter = achievementAdapter
            it.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun setupButton() {
        binding.button.setOnClickListener {
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
    }

    private fun setupSkeletons() {
        userActivitiesSkeletonScreen = Skeleton.bind(binding.currentActivitiesRecyclerview)
            .adapter(userActivitiesAdapter)
            .frozen(true)
            .duration(2400)
            .count(10)
            .load(R.layout.activity_skeleton_view_item)
            .show()

        personalScoreboardSkeletonScreen = Skeleton.bind(binding.achievementRecyclerView)
            .adapter(achievementAdapter)
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

            if(userId != null){
                if(user.canViewDetails == true){
                    initActivities()
                } else {
                    binding.currentActivities.visibility = View.GONE
                    binding.achievements.visibility = View.GONE
                    binding.profileLocked.visibility = View.VISIBLE

                    binding.lockedText.text = getString(R.string.profile_locked, user.firstName)
                }
            } else {
                initActivities()
            }

            Glide.with(this)
                .load(user.profilePicture)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(binding.profilePicture)

            binding.fullName.text = null
            binding.fullName.append("${user.firstName} ${user.lastName}")

            binding.details.text = null
            binding.details.append("${user.jobTitle}, ${user.location}")

            binding.description.text = user.description

            binding.points.append(
                TextViewUtils.getColoredString(
                    user.points.toString() + " ",
                    getColor(R.color.primary)
                )
            )
            binding.points.append(
                TextViewUtils.getColoredString(
                    getString(R.string.profile_points),
                    getColor(R.color.black)
                )
            )

            if(user.interests.isNullOrEmpty()) binding.interests.visibility = View.GONE

            if (userId.isNullOrBlank()) {
                binding.interests.text = getString(R.string.profile_your_interests, user.interests?.joinToString { it.name }).toHtmlSpan()

                binding.achievementTitle.text = getString(R.string.profile_your_prize_cabinet)
                binding.button.text = getString(R.string.profile_edit)
                binding.button.visibility = View.VISIBLE
            } else {
                binding.interests.text = getString(R.string.profile_interests, user.firstName, user.interests?.joinToString { it.name }).toHtmlSpan()
                binding.achievementTitle.text = getString(R.string.profile_prize_cabinet, user.firstName)
            }
        }

        if(userId != null) viewModel.isFollowing.observe(this) { isFollowing ->
            Dialogs.hideCurrentDialog()

            if (isFollowing) {
                binding.button.text = getString(R.string.profile_unfollow)
            } else {
                binding.button.text = getString(R.string.profile_follow)
            }

            binding.button.visibility = View.VISIBLE
        }
    }

    private fun initActivities(){
        viewModel.getActivities(userId)

        viewModel.currentActivities.observe(this) { activities ->
            initScoreboard()

            val visibility = if(activities.isEmpty()) View.GONE else View.VISIBLE
            binding.currentActivities.visibility = visibility

            userActivitiesAdapter?.addItems(activities)
            userActivitiesSkeletonScreen?.hide()
        }
    }

    private fun initScoreboard(){
        viewModel.getAchievements(userId)

        viewModel.achievements.observe(this) { results ->
            val visibility = if(results.isEmpty()) View.GONE else View.VISIBLE
            binding.achievements.visibility = visibility

            achievementAdapter?.addItems(results)
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