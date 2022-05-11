package nl.inholland.myvitality.ui.activity.participants

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.architecture.base.BaseActivityAdvanced
import nl.inholland.myvitality.data.adapters.UserListAdapter
import nl.inholland.myvitality.data.entities.ResponseStatus
import nl.inholland.myvitality.databinding.ActivityUserListBinding
import javax.inject.Inject

class ActivityParticipantsActivity : BaseActivityAdvanced<ActivityUserListBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivityUserListBinding
            = ActivityUserListBinding::inflate

    @Inject
    lateinit var factory: ActivityParticipantsViewModelFactory
    lateinit var viewModel: ActivityParticipantsViewModel

    var layoutManager: LinearLayoutManager? = null
    var adapter: UserListAdapter? = null
    var isCalling: Boolean = false

    private var page = 0
    private var limit = 10

    var currentActivityId: String = ""


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

        binding.recyclerView.let {
            it.adapter = adapter
            it.layoutManager = layoutManager
        }

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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