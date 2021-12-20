package nl.inholland.myvitality.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.fragment.app.Fragment
import nl.inholland.myvitality.R
import nl.inholland.myvitality.architecture.ChosenFragment
import nl.inholland.myvitality.databinding.ActivityMainBinding
import nl.inholland.myvitality.ui.authentication.login.LoginActivity
import nl.inholland.myvitality.ui.home.HomeFragment
import nl.inholland.myvitality.ui.profile.ProfileActivity
import nl.inholland.myvitality.ui.timeline.TimelineFragment
import nl.inholland.myvitality.ui.timelinepost.create.CreateTimelinePostActivity


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide();
        val navView: BottomNavigationView = binding.bottomNavView

        when(intent.getIntExtra("FRAGMENT_TO_LOAD", 0)){
            ChosenFragment.FRAGMENT_HOME.ordinal -> {
                setCurrentFragment(HomeFragment())
            }
            ChosenFragment.FRAGMENT_NOTIFICATIONS.ordinal -> {
                //  TODO: Go to not fragment
                Toast.makeText(this, "WIP",  Toast.LENGTH_LONG).show()
            }
            ChosenFragment.FRAGMENT_TIMELINE.ordinal -> {
                setCurrentFragment(TimelineFragment())
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
                    setCurrentFragment(TimelineFragment())
                }
                R.id.navigation_post -> {
                    startActivity(Intent(this, CreateTimelinePostActivity::class.java))
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
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, fragment)
            commit()
        }

        fragmentManager.executePendingTransactions()
    }
}