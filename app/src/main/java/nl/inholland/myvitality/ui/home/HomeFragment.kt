package nl.inholland.myvitality.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import butterknife.OnClick
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ethanhua.skeleton.RecyclerViewSkeletonScreen
import com.ethanhua.skeleton.Skeleton
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.architecture.base.BaseFragment
import nl.inholland.myvitality.data.adapters.ActivityCategoryAdapter
import nl.inholland.myvitality.data.entities.ActivityCategory
import nl.inholland.myvitality.data.entities.ResponseStatus
import nl.inholland.myvitality.databinding.FragmentHomeBinding
import nl.inholland.myvitality.ui.scoreboard.ScoreboardActivity
import nl.inholland.myvitality.util.TextViewUtils
import javax.inject.Inject

class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentHomeBinding
            = FragmentHomeBinding::inflate

    @Inject
    lateinit var factory: HomeViewModelFactory
    lateinit var viewModel: HomeViewModel

    private var layoutManager: StaggeredGridLayoutManager? = null
    private var adapter: ActivityCategoryAdapter? = null
    private var skeletonScreen: RecyclerViewSkeletonScreen? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        // Set greeting message
        binding.greeting.append(TextViewUtils.getGreetingMessage(requireActivity()))

        viewModel = ViewModelProviders.of(this, factory).get(HomeViewModel::class.java)
        return view
    }

    override fun onAttach(context: Context) {
        (requireActivity().application as VitalityApplication).appComponent.inject(this)
        super.onAttach(context)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        setupSkeleton()

        initResponseHandler()
        initUser()
    }

    @OnClick(value = [R.id.points, R.id.scoreboard])
    fun onClickViewScoreboard(){
        startActivity(Intent(requireContext(), ScoreboardActivity::class.java))
    }

    private fun setupSkeleton() {
        skeletonScreen = Skeleton.bind(binding.recyclerview)
            .adapter(adapter)
            .frozen(true)
            .duration(2400)
            .count(10)
            .load(R.layout.activity_skeleton_view_item)
            .show()

        skeletonScreen?.hide()
    }

    private fun setupRecyclerViews() {
        layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        adapter = ActivityCategoryAdapter(requireActivity())

        binding.recyclerview.let {
            it.adapter = adapter
            it.layoutManager = layoutManager
        }
    }

    private fun initUser(){
        if(view == null) return

        viewModel.getLoggedInUser()
        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            initCategories()

            binding.name.text = ""
            binding.name.append("${user.firstName} ${user.lastName}")

            Glide.with(this)
                .load(user.profilePicture)
                .placeholder(R.drawable.person_placeholder)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(binding.profileImage)

            // Set greeting message
            binding.points.text = getString(R.string.home_points_text, (user.points ?: 0).toString())

        }
    }

    private fun initCategories(){
        if(view == null) return

        adapter?.addItem(ActivityCategory("", getString(R.string.home_my_activities_text), "", "", true))
        viewModel.getActivityCategories()
    }

    private fun initResponseHandler(){
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            adapter?.addItems(categories)

            binding.recyclerview.visibility = View.VISIBLE
            skeletonScreen?.hide()
        }

        if(view == null) return

        viewModel.apiResponse.observe(viewLifecycleOwner) { response ->
            when (response.status) {
                ResponseStatus.API_ERROR -> Toast.makeText(
                    requireContext(),
                    getString(R.string.api_error),
                    Toast.LENGTH_LONG
                ).show()
                else -> {
                }
            }
        }
    }
}