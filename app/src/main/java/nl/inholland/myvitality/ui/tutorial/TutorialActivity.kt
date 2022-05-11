package nl.inholland.myvitality.ui.tutorial

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import butterknife.BindView
import butterknife.OnClick
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.architecture.base.BaseFragmentActivity
import nl.inholland.myvitality.data.adapters.ViewPagerAdapter
import nl.inholland.myvitality.data.entities.TutorialType
import nl.inholland.myvitality.ui.authentication.login.LoginActivity
import nl.inholland.myvitality.util.SharedPreferenceHelper
import javax.inject.Inject


class TutorialActivity : BaseFragmentActivity() {

    @Inject
    lateinit var sharedPrefs: SharedPreferenceHelper

    @BindView(R.id.tutorial_skip)
    lateinit var skipButton: TextView

    @BindView(R.id.tutorial_viewpager)
    lateinit var viewPager: ViewPager2

    @BindView(R.id.tutorial_page_indicator)
    lateinit var tabLayout: TabLayout

    @BindView(R.id.tutorial_continue_button)
    lateinit var continueButton: TextView

    @BindView(R.id.tutorial_login_button)
    lateinit var registerButton: TextView

    override fun layoutResourceId(): Int {
        return R.layout.activity_tutorial
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as VitalityApplication).appComponent.inject(this)
        val adapter = ViewPagerAdapter(this,
            listOf(TutorialStartFragment(),
                TutorialTypeFragment(TutorialType.COMMUNITY),
                TutorialTypeFragment(TutorialType.TIMELINE),
                TutorialEndFragment(),
            ))

        viewPager.adapter = adapter
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val isLastPage = position == adapter.itemCount-1

                skipButton.visibility = if(isLastPage) View.INVISIBLE else View.VISIBLE
                tabLayout.visibility = if(isLastPage) View.GONE else View.VISIBLE

                continueButton.visibility = if(isLastPage) View.GONE else View.VISIBLE
                registerButton.visibility = if(isLastPage) View.VISIBLE else View.GONE
            }
        })

        TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()
    }

    override fun onBackPressed() {
        if (viewPager.currentItem == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed()
        } else {
            // Otherwise, select the previous step.
            viewPager.currentItem = viewPager.currentItem - 1

        }
    }

    @OnClick(R.id.achievement_continue_button)
    fun onClickContinue(){
        viewPager.currentItem = viewPager.currentItem+1
    }

    @OnClick(value = [R.id.tutorial_login_button, R.id.tutorial_skip])
    fun onClickSignup(){
        sharedPrefs.isFirstAppUse = false

        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

}