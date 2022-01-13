package nl.inholland.myvitality.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import nl.gunther.bryan.newsreader.utils.SharedPreferenceHelper
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.architecture.base.BaseActivity
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.ui.MainActivity
import nl.inholland.myvitality.ui.authentication.login.LoginActivity
import nl.inholland.myvitality.ui.authentication.register.details1.RegisterDetailsActivity
import nl.inholland.myvitality.ui.tutorial.TutorialActivity
import javax.inject.Inject

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class SplashScreenActivity : BaseActivity() {
    @Inject lateinit var apiClient: ApiClient
    @Inject lateinit var sharedPrefs: SharedPreferenceHelper

    override fun layoutResourceId(): Int {
        return R.layout.activity_splash_screen
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as VitalityApplication).appComponent.inject(this)

        Handler(Looper.getMainLooper()).postDelayed({
            // Start your app main activity
            var intent = Intent(this, LoginActivity::class.java)

            if(sharedPrefs.isFirstAppUse) {
                intent = Intent(this, TutorialActivity::class.java)
            } else {
                if(sharedPrefs.isLoggedIn()){
                    if(sharedPrefs.recentlyRegistered){
                        intent = Intent(this, RegisterDetailsActivity::class.java)
                    } else {
                        intent = Intent(this, MainActivity::class.java)
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    }
                }
            }

            startActivity(intent)
            // close this activity
            finish()
        }, 3000)
    }
}