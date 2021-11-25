package nl.inholland.myvitality.ui.splash

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import nl.gunther.bryan.newsreader.utils.SharedPreferenceHelper
import nl.inholland.myvitality.R
import nl.inholland.myvitality.ui.authentication.login.LoginActivity

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        val sharedPreferenceHelper = SharedPreferenceHelper(this)


        Handler(Looper.getMainLooper()).postDelayed({
            // Start your app main activity
            var intent = Intent(this, LoginActivity::class.java)

            if(sharedPreferenceHelper.isFirstUse() == true) {
                // TODO: Set tutorial intent
//                intent = Intent(this, TutorialActivity::class.java)
            } else {
                if(sharedPreferenceHelper.isLoggedIn()){
                    // TODO: Set home intent
//                intent = Intent(this, HomeActivity::class.java)
                }
            }

            startActivity(intent)
            // close this activity
            finish()
        }, 3000)
    }
}