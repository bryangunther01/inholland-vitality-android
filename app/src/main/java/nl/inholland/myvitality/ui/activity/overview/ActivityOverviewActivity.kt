package nl.inholland.myvitality.ui.activity.overview

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.constraintlayout.widget.Group
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.architecture.base.BaseActivity
import nl.inholland.myvitality.data.adapters.ActivityAdapter
import nl.inholland.myvitality.data.entities.ActivityState
import nl.inholland.myvitality.data.entities.ResponseStatus
import javax.inject.Inject


class ActivityOverviewActivity : BaseActivity() {

    @BindView(R.id.act_overview_user_activities_recyclerview)
    lateinit var userActivitiesRecyclerView: RecyclerView

    @BindView(R.id.act_overview_available_activities_recyclerview)
    lateinit var availableActivitiesRecyclerView: RecyclerView

    @BindView(R.id.act_overview_upcoming_activities_recyclerview)
    lateinit var upcomingActivitiesRecyclerView: RecyclerView

    @Inject
    lateinit var factory: ActivityOverviewModelFactory
    lateinit var viewModel: ActivityOverviewViewModel

    private var userActivitiesAdapter: ActivityAdapter? = null
    private var availableActivitiesAdapter: ActivityAdapter? = null
    private var upcomingActivitiesAdapter: ActivityAdapter? = null

    override fun layoutResourceId(): Int {
        return R.layout.activity_overview_activities
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.navigation_home)

        (application as VitalityApplication).appComponent.inject(this)
        viewModel = ViewModelProviders.of(this, factory).get(ActivityOverviewViewModel::class.java)

        val categoryId = intent.getStringExtra("CATEGORY_ID")
        if(categoryId == null) finish()

        setupRecyclerViews()
        initResponseHandler()
        initActivities(categoryId!!)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setupRecyclerViews() {
        userActivitiesAdapter = ActivityAdapter(this)
        userActivitiesRecyclerView.let {
            it.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            it.adapter = userActivitiesAdapter
        }

        availableActivitiesAdapter = ActivityAdapter(this)
        availableActivitiesRecyclerView.let {
            it.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            it.adapter = availableActivitiesAdapter
        }

        upcomingActivitiesAdapter = ActivityAdapter(this)
        upcomingActivitiesRecyclerView.let {
            it.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            it.adapter = upcomingActivitiesAdapter
        }
    }

    private fun initActivities(categoryId: String){
        viewModel.getActivities(categoryId, ActivityState.USER)

        viewModel.userActivities.observe(this) {
            userActivitiesAdapter?.addItems(it)
            viewModel.getActivities(categoryId, ActivityState.AVAILABLE)

            userActivitiesRecyclerView.visibility = if(it.isEmpty()) View.INVISIBLE else View.VISIBLE
            findViewById<Group>(R.id.act_overview_user_activities_empty).visibility = if(it.isEmpty()) View.VISIBLE else View.INVISIBLE

        }

        viewModel.availableActivities.observe(this) {
            availableActivitiesAdapter?.addItems(it)
            viewModel.getActivities(categoryId, ActivityState.UPCOMING)

            availableActivitiesRecyclerView.visibility = if(it.isEmpty()) View.INVISIBLE else View.VISIBLE
            findViewById<Group>(R.id.act_overview_available_activities_empty).visibility = if(it.isEmpty()) View.VISIBLE else View.INVISIBLE
        }

        viewModel.upcomingActivities.observe(this) {
            upcomingActivitiesAdapter?.addItems(it)

            upcomingActivitiesRecyclerView.visibility = if(it.isEmpty()) View.INVISIBLE else View.VISIBLE
            findViewById<Group>(R.id.act_overview_upcoming_activities_empty).visibility = if(it.isEmpty()) View.VISIBLE else View.INVISIBLE
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