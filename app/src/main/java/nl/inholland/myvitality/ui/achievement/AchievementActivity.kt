package nl.inholland.myvitality.ui.achievement

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
import nl.inholland.myvitality.data.entities.Achievement
import nl.inholland.myvitality.ui.MainActivity
import nl.inholland.myvitality.util.SharedPreferenceHelper
import java.util.ArrayList
import javax.inject.Inject


class AchievementActivity : BaseFragmentActivity() {

    @Inject
    lateinit var sharedPrefs: SharedPreferenceHelper

    @BindView(R.id.achievement_viewpager)
    lateinit var viewPager: ViewPager2

    @BindView(R.id.achievement_page_indicator)
    lateinit var tabLayout: TabLayout

    @BindView(R.id.achievement_continue_button)
    lateinit var continueButton: TextView

    override fun layoutResourceId(): Int {
        return R.layout.activity_achievement
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as VitalityApplication).appComponent.inject(this)

        val achievements: ArrayList<Achievement>? = intent.getParcelableArrayListExtra("ACHIEVEMENT_LIST")
        if(achievements == null) finish()

        val fragments = mutableListOf<AchievementFragment>()

        achievements?.forEach { achievement ->
            fragments.add(AchievementFragment(achievement))
        }

        viewPager.adapter = ViewPagerAdapter(this, fragments)
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
        finishAffinity()
        startActivity(Intent(this, MainActivity::class.java))
    }
}