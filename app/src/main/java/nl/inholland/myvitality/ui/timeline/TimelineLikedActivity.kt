package nl.inholland.myvitality.ui.timeline

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.OnTextChanged
import nl.gunther.bryan.newsreader.utils.SharedPreferenceHelper
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.architecture.ChosenFragment
import nl.inholland.myvitality.architecture.base.BaseActivity
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.data.adapters.CommentAdapter
import nl.inholland.myvitality.data.adapters.UserListAdapter
import nl.inholland.myvitality.data.entities.SimpleUser
import nl.inholland.myvitality.data.entities.TimelinePost
import nl.inholland.myvitality.ui.MainActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class TimelineLikedActivity : BaseActivity(), Callback<List<SimpleUser>> {

    @Inject
    lateinit var apiClient: ApiClient
    @Inject
    lateinit var sharedPrefs: SharedPreferenceHelper
    @BindView(R.id.user_recyclerview)
    lateinit var recyclerView: RecyclerView

    var layoutManager: LinearLayoutManager? = null
    var adapter: UserListAdapter? = null
    var isLoading: Boolean = false

    private var page = 0
    private var limit = 10

    var timelinePostId: String = ""

    override fun layoutResourceId(): Int {
        return R.layout.activity_post_likers
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.navigation_timeline)

        (application as VitalityApplication).appComponent.inject(this)

        val postId = intent.getStringExtra("POST_ID")
        if(postId == null){
            finish()
        } else {
            timelinePostId = postId
        }

        setupRecyclerViews()
        tryLoadUsers()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setupRecyclerViews(){
        layoutManager = LinearLayoutManager(this)
        adapter = UserListAdapter(this)

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

                    if (!isLoading) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            isLoading = true
                            tryLoadUsers()
                        }
                    }
                }
            }
        })
    }

    fun tryLoadUsers(){
        sharedPrefs.accessToken?.let {
            apiClient.getTimelinePostLikes("Bearer $it", timelinePostId, limit , page * limit).enqueue(this)
        }
    }

    override fun onResponse(call: Call<List<SimpleUser>>, response: Response<List<SimpleUser>>) {
        if(response.isSuccessful && response.body() != null){
            response.body()?.let {
                if(it.isNotEmpty()){
                    adapter?.addItems(it)

                    if(it.size == limit){
                        page += 1
                    }
                }

                isLoading = false
            }
        }
    }

    override fun onFailure(call: Call<List<SimpleUser>>, t: Throwable) {
        Toast.makeText(this, t.message, Toast.LENGTH_LONG).show()
    }
}