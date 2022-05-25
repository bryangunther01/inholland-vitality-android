package nl.inholland.myvitality.ui.tutorial

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import butterknife.OnClick
import com.google.android.material.tabs.TabLayoutMediator
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.architecture.base.BaseFragmentActivity
import nl.inholland.myvitality.data.adapters.ViewPagerAdapter
import nl.inholland.myvitality.data.entities.TutorialType
import nl.inholland.myvitality.databinding.ActivityTutorialBinding
import nl.inholland.myvitality.ui.authentication.login.LoginActivity
import nl.inholland.myvitality.util.SharedPreferenceHelper
import javax.inject.Inject


class TutorialActivity : BaseFragmentActivity<ActivityTutorialBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivityTutorialBinding
            = ActivityTutorialBinding::inflate

    @Inject
    lateinit var sharedPrefs: SharedPreferenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as VitalityApplication).appComponent.inject(this)
        val adapter = ViewPagerAdapter(this,
            listOf(TutorialStartFragment(),
                TutorialTypeFragment(TutorialType.COMMUNITY),
                TutorialTypeFragment(TutorialType.TIMELINE),
                TutorialTypeFragment(TutorialType.ACHIEVEMENTS),
                TutorialEndFragment(),
            ))

        binding.viewpager.adapter = adapter
        binding.viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val isLastPage = position == adapter.itemCount-1

                binding.skipButton.visibility = if(isLastPage) View.INVISIBLE else View.VISIBLE
                binding.pageIndicator.visibility = if(isLastPage) View.GONE else View.VISIBLE

                binding.continueButton.visibility = if(isLastPage) View.GONE else View.VISIBLE
                binding.loginButton.visibility = if(isLastPage) View.VISIBLE else View.GONE
            }
        })

        TabLayoutMediator(binding.pageIndicator, binding.viewpager) { _, _ -> }.attach()
    }

    override fun onBackPressed() {
        if (binding.viewpager.currentItem == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed()
        } else {
            // Otherwise, select the previous step.
            binding.viewpager.currentItem = binding.viewpager.currentItem - 1

        }
    }

    @OnClick(R.id.continue_button)
    fun onClickContinue(){
        binding.viewpager.currentItem = binding.viewpager.currentItem+1
    }

    @OnClick(value = [R.id.login_button, R.id.skip_button])
    fun onClickSignup(){
        sharedPrefs.isFirstAppUse = false

        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

}