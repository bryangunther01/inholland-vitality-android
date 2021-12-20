package nl.inholland.myvitality.ui.profile

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import butterknife.BindView
import coil.load
import nl.gunther.bryan.newsreader.utils.SharedPreferenceHelper
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.architecture.base.BaseActivity
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.data.adapters.CurrentChallengeAdapter
import nl.inholland.myvitality.data.adapters.ExploreChallengeAdapter
import nl.inholland.myvitality.data.entities.User
import nl.inholland.myvitality.util.TextViewUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class ProfileActivity : BaseActivity(), Callback<User> {

    @Inject lateinit var apiClient: ApiClient
    @Inject lateinit var sharedPrefs: SharedPreferenceHelper
    @BindView(R.id.profile_image) lateinit var profileImage: ImageView
    @BindView(R.id.profile_fullname) lateinit var fullname: TextView
    @BindView(R.id.profile_details) lateinit var details: TextView
    @BindView(R.id.profile_description) lateinit var description: TextView
    @BindView(R.id.profile_points) lateinit var points: TextView
    @BindView(R.id.profile_button) lateinit var button: Button
    @BindView(R.id.profile_finn_chl_title) lateinit var finnChlTitle: TextView

    var crtLayoutManager: LinearLayoutManager? = null
    var finnLayoutManager: LinearLayoutManager? = null
    var crtChlAdapter: CurrentChallengeAdapter? = null
    var finnChlAdapter: ExploreChallengeAdapter? = null

    var userId: String? = null

    override fun layoutResourceId(): Int {
        return R.layout.activity_profile
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as VitalityApplication).appComponent.inject(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.navigation_profile)
        supportActionBar?.elevation = 0F

        userId = intent.getStringExtra("USER_ID")

        // Build in to support other accounts
        tryLoadUser()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun tryLoadUser(){
        sharedPrefs.accessToken?.let {
            apiClient.getUser("Bearer $it", userId).enqueue(this)
        }
    }

    override fun onResponse(call: Call<User>, response: Response<User>) {
        if(response.isSuccessful && response.body() != null){
            response.body()?.let { user ->
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
                } else {
                    finnChlTitle.text = getString(R.string.profile_prize_cabinet, user.firstName)

                    if(user.isFollowing == true){
                        button.text = getString(R.string.profile_unfollow)
                    } else {
                        button.text = getString(R.string.profile_follow)
                    }
                }
            }
        }
    }

    override fun onFailure(call: Call<User>, t: Throwable) {
        Toast.makeText(this, getString(R.string.api_error), Toast.LENGTH_LONG).show()
    }
}