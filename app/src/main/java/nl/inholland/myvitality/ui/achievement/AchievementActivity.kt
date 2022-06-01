package nl.inholland.myvitality.ui.achievement

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import butterknife.OnClick
import com.google.android.material.tabs.TabLayoutMediator
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.architecture.base.BaseFragmentActivity
import nl.inholland.myvitality.data.adapters.ViewPagerAdapter
import nl.inholland.myvitality.data.entities.Achievement
import nl.inholland.myvitality.databinding.ActivityAchievementBinding
import nl.inholland.myvitality.ui.MainActivity

class AchievementActivity : BaseFragmentActivity<ActivityAchievementBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivityAchievementBinding
            = ActivityAchievementBinding::inflate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as VitalityApplication).appComponent.inject(this)

        val achievements: ArrayList<Achievement>? = intent.getParcelableArrayListExtra("ACHIEVEMENT_LIST")
        if(achievements == null) finish()

        val fragments = mutableListOf<AchievementFragment>()

        achievements?.forEach { achievement ->
            fragments.add(AchievementFragment(achievement))
        }

        binding.viewpager.adapter = ViewPagerAdapter(this, fragments)
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
        finishAffinity()
        startActivity(Intent(this, MainActivity::class.java))
    }
}