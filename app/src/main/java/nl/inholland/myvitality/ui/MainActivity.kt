package nl.inholland.myvitality.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import nl.inholland.myvitality.R
import nl.inholland.myvitality.architecture.ChosenFragment
import nl.inholland.myvitality.databinding.ActivityMainBinding
import nl.inholland.myvitality.ui.home.HomeFragment
import nl.inholland.myvitality.ui.notification.NotificationActivity
import nl.inholland.myvitality.ui.profile.overview.ProfileActivity
import nl.inholland.myvitality.ui.timeline.overview.TimelineOverviewFragment
import nl.inholland.myvitality.ui.timelinepost.create.CreateTimelinePostActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseMessaging.getInstance().subscribeToTopic("test")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        val navView: BottomNavigationView = binding.bottomNavView

        when(intent.getIntExtra("FRAGMENT_TO_LOAD", 0)){
            ChosenFragment.FRAGMENT_HOME.ordinal -> {
                setCurrentFragment(HomeFragment())
            }
            ChosenFragment.FRAGMENT_TIMELINE.ordinal -> {
                setCurrentFragment(TimelineOverviewFragment())
            }
            else -> {
                setCurrentFragment(HomeFragment())
            }
        }

        navView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    setCurrentFragment(HomeFragment())
                }
                R.id.navigation_timeline -> {
                    setCurrentFragment(TimelineOverviewFragment())
                }
                R.id.navigation_post -> {
                    startActivity(Intent(this, CreateTimelinePostActivity::class.java))
                    return@setOnNavigationItemSelectedListener false
                }
                R.id.navigation_notification -> {
                    startActivity(Intent(this, NotificationActivity::class.java))
                    return@setOnNavigationItemSelectedListener false
                }
                R.id.navigation_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    return@setOnNavigationItemSelectedListener false
                }
            }

            return@setOnNavigationItemSelectedListener true
        }
    }

    private fun setCurrentFragment(fragment: Fragment) {
        val currentFragment = this.supportFragmentManager.findFragmentById(R.id.fragment_container)

        currentFragment?.let{
            if(fragment::class.java == currentFragment::class.java) return
            if(currentFragment.isRemoving) return
        }

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, fragment)
            commit()
        }

        fragmentManager.executePendingTransactions()
    }
}