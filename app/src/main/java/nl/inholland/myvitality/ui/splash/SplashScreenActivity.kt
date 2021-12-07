package nl.inholland.myvitality.ui.splash

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import nl.gunther.bryan.newsreader.utils.SharedPreferenceHelper
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.data.entities.User
import nl.inholland.myvitality.ui.MainActivity
import nl.inholland.myvitality.ui.authentication.login.LoginActivity
import nl.inholland.myvitality.ui.authentication.register.RegisterDetailsActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class SplashScreenActivity : AppCompatActivity() {
    @Inject
    lateinit var apiClient: ApiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        (application as VitalityApplication).appComponent.inject(this)


        val sharedPref = SharedPreferenceHelper(this)

        Handler(Looper.getMainLooper()).postDelayed({
            // Start your app main activity
            var intent = Intent(this, LoginActivity::class.java)

            // TODO: Remove this if tutorial is there
            sharedPref.isFirstAppUse = false
            if(sharedPref.isFirstAppUse) {
                // TODO: Set tutorial intent
//                intent = Intent(this, TutorialActivity::class.java)
            } else {
                if(sharedPref.isLoggedIn()){
                    if(sharedPref.recentlyRegistered){
                        intent = Intent(this, RegisterDetailsActivity::class.java)
                    } else {
                        intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("fragmentToLoad", 0)
                    }
                }
            }

            startActivity(intent)
            // close this activity
            finish()
        }, 3000)
    }
}