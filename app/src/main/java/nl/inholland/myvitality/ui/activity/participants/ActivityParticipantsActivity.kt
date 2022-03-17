package nl.inholland.myvitality.ui.activity.participants

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import nl.gunther.bryan.newsreader.utils.SharedPreferenceHelper
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.architecture.base.BaseActivity
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.data.adapters.UserListAdapter
import nl.inholland.myvitality.data.entities.ResponseStatus
import javax.inject.Inject

class ActivityParticipantsActivity : BaseActivity() {

    @BindView(R.id.user_recyclerview)
    lateinit var recyclerView: RecyclerView

    @Inject
    lateinit var factory: ActivityParticipantsViewModelFactory
    lateinit var viewModel: ActivityParticipantsViewModel

    var layoutManager: LinearLayoutManager? = null
    var adapter: UserListAdapter? = null
    var isCalling: Boolean = false

    private var page = 0
    private var limit = 10

    var currentActivityId: String = ""

    override fun layoutResourceId(): Int {
        return R.layout.activity_user_list
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.navigation_activity)

        (application as VitalityApplication).appComponent.inject(this)
        viewModel = ViewModelProviders.of(this, factory).get(ActivityParticipantsViewModel::class.java)

        val activityId = intent.getStringExtra("ACTIVITY_ID")
        if (activityId == null) finish() else currentActivityId = activityId

        initResponseHandler()
        initUsersObserver()

        setupRecyclerViews()
        tryLoadUsers()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setupRecyclerViews() {
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

                    if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                        tryLoadUsers()
                    }
                }
            }
        })
    }

    fun tryLoadUsers() {
        if (isCalling) return

        isCalling = true
        viewModel.getSubscribers(currentActivityId, limit, page * limit)
    }

    private fun initUsersObserver() {
        viewModel.results.observe(this) {
            if (page == 0) adapter?.clearItems()

            if (it.isNotEmpty()) {
                adapter?.addItems(it)
                if (it.size == limit) page += 1
            }

            isCalling = false
        }
    }

    private fun initResponseHandler() {
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