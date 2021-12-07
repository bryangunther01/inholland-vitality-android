package nl.inholland.myvitality.ui.timelinepost.create

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.OnTextChanged
import coil.load
import com.google.android.material.button.MaterialButton
import nl.gunther.bryan.newsreader.utils.SharedPreferenceHelper
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.architecture.ChosenFragment
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.data.adapters.CommentAdapter
import nl.inholland.myvitality.data.adapters.TimelinePostAdapter
import nl.inholland.myvitality.data.entities.*
import nl.inholland.myvitality.data.entities.requestbody.TimelinePostRequest
import nl.inholland.myvitality.ui.MainActivity
import nl.inholland.myvitality.ui.authentication.login.LoginActivity
import nl.inholland.myvitality.ui.timelinepost.TimelinePostActivity
import nl.inholland.myvitality.util.TextViewUtils
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.stream.Collectors
import javax.inject.Inject

class CreateTimelinePostActivity : AppCompatActivity(), Callback<Void> {
    @Inject
    lateinit var apiClient: ApiClient

    @BindView(R.id.create_post_profile_image)
    lateinit var profileImage: ImageView

    @BindView(R.id.create_post_user_name)
    lateinit var userName: TextView

    @BindView(R.id.create_post_message)
    lateinit var message: EditText

    @BindView(R.id.create_post_button)
    lateinit var postButton: Button

    @BindView(R.id.create_post_image_upload_button)
    lateinit var imageUploadButton: MaterialButton

    var sharedPrefs: SharedPreferenceHelper? = null
    var postId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)
        ButterKnife.bind(this)

        (application as VitalityApplication).appComponent.inject(this)
        sharedPrefs = SharedPreferenceHelper(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.navigation_post)

        postId = intent.getStringExtra("POST_ID")
        if(postId != null){
            findViewById<View>(R.id.create_post_hr_bottom).visibility = View.INVISIBLE
            imageUploadButton.visibility = View.INVISIBLE
        }
        loadCurrentUser()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        finish()
    }

    @OnClick(R.id.create_post_button)
    fun onClickPostButton() {
        sharedPrefs?.accessToken?.let {
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("Text", message.text.toString())
                .build()

            if(postId != null){
                apiClient.createComment("Bearer $it", postId!!, TimelinePostRequest(message.text.toString())).enqueue(this)
            } else {
                apiClient.createPost("Bearer $it", requestBody).enqueue(this)
            }

        }
    }

    @OnTextChanged(R.id.create_post_message)
    fun onTextChanged() {
        postButton.isEnabled = message.text.isNotEmpty()
    }

    private fun loadCurrentUser() {
        sharedPrefs?.accessToken?.let {
            apiClient.getUser("Bearer $it").enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful && response.body() != null) {
                        response.body()?.let { result ->
                            profileImage.load(result.profilePicture)
                            userName.text = "${result.firstName} ${result.lastName}"
                        }
                    } else {
                        if (response.code() == 401) {
                            startActivity(
                                Intent(
                                    this@CreateTimelinePostActivity,
                                    LoginActivity::class.java
                                )
                            )
                        }
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    Toast.makeText(
                        this@CreateTimelinePostActivity,
                        getString(R.string.api_error),
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
        }
    }

    override fun onResponse(call: Call<Void>, response: Response<Void>) {
        if (response.isSuccessful) {
            if(postId != null){
                startActivity(
                    Intent(this, TimelinePostActivity::class.java)
                        .putExtra("POST_ID", postId))
            } else {
                startActivity(
                    Intent(this, MainActivity::class.java)
                        .putExtra("FRAGMENT_TO_LOAD", ChosenFragment.FRAGMENT_TIMELINE.ordinal))
            }
        } else {
            Toast.makeText(this, getString(R.string.api_error), Toast.LENGTH_LONG)
                .show()
        }
    }

    override fun onFailure(call: Call<Void>, t: Throwable) {
        Toast.makeText(this, getString(R.string.api_error), Toast.LENGTH_LONG).show()
    }
}