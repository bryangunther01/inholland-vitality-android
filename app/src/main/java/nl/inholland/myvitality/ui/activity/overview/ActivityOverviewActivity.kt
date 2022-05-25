package nl.inholland.myvitality.ui.activity.overview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.architecture.base.BaseActivity
import nl.inholland.myvitality.data.adapters.ActivityAdapter
import nl.inholland.myvitality.data.entities.ActivityState
import nl.inholland.myvitality.data.entities.ResponseStatus
import nl.inholland.myvitality.databinding.ActivityOverviewActivitiesBinding
import javax.inject.Inject


class ActivityOverviewActivity : BaseActivity<ActivityOverviewActivitiesBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivityOverviewActivitiesBinding
            = ActivityOverviewActivitiesBinding::inflate

    @Inject
    lateinit var factory: ActivityOverviewModelFactory
    lateinit var viewModel: ActivityOverviewViewModel

    private var userActivitiesAdapter: ActivityAdapter? = null
    private var availableActivitiesAdapter: ActivityAdapter? = null
    private var upcomingActivitiesAdapter: ActivityAdapter? = null

    private var categoryId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.navigation_home)

        (application as VitalityApplication).appComponent.inject(this)
        viewModel = ViewModelProviders.of(this, factory).get(ActivityOverviewViewModel::class.java)

        categoryId = intent.getStringExtra("CATEGORY_ID")
        if(categoryId == null) finish()

        setupRecyclerViews()
        initResponseHandler()
        loadActivities()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setupRecyclerViews() {
        userActivitiesAdapter = ActivityAdapter(this)
        binding.userActivitiesRecyclerview.let {
            it.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            it.adapter = userActivitiesAdapter
        }

        availableActivitiesAdapter = ActivityAdapter(this)
        binding.availableActivitiesRecyclerview.let {
            it.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            it.adapter = availableActivitiesAdapter
        }

        upcomingActivitiesAdapter = ActivityAdapter(this)
        binding.upcomingActivitiesRecyclerview.let {
            it.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            it.adapter = upcomingActivitiesAdapter
        }
    }

    private fun loadActivities(){
        categoryId?.let { categoryId ->
            viewModel.getActivities(categoryId, ActivityState.USER)

            viewModel.userActivities.observe(this) {
                userActivitiesAdapter?.addItems(it)
                viewModel.getActivities(categoryId, ActivityState.AVAILABLE)

                binding.userActivitiesRecyclerview.visibility = if(it.isEmpty()) View.INVISIBLE else View.VISIBLE
                binding.userActivitiesEmpty.visibility = if(it.isEmpty()) View.VISIBLE else View.INVISIBLE
0
            }

            viewModel.availableActivities.observe(this) {
                availableActivitiesAdapter?.addItems(it)
                viewModel.getActivities(categoryId, ActivityState.UPCOMING)

                binding.availableActivitiesRecyclerview.visibility = if(it.isEmpty()) View.INVISIBLE else View.VISIBLE
                binding.availableActivitiesEmpty.visibility = if(it.isEmpty()) View.VISIBLE else View.INVISIBLE
            }

            viewModel.upcomingActivities.observe(this) {
                upcomingActivitiesAdapter?.addItems(it)

                binding.upcomingActivitiesRecyclerview.visibility = if(it.isEmpty()) View.INVISIBLE else View.VISIBLE
                binding.upcomingActivitiesEmpty.visibility = if(it.isEmpty()) View.VISIBLE else View.INVISIBLE
            }
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