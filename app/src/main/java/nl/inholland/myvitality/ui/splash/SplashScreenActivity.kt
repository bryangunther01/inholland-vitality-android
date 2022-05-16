package nl.inholland.myvitality.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import com.microsoft.identity.client.IAccount
import com.microsoft.identity.client.IPublicClientApplication
import com.microsoft.identity.client.ISingleAccountPublicClientApplication
import com.microsoft.identity.client.PublicClientApplication
import com.microsoft.identity.client.exception.MsalException
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.architecture.base.BaseActivity
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.databinding.ActivitySplashScreenBinding
import nl.inholland.myvitality.ui.MainActivity
import nl.inholland.myvitality.ui.authentication.login.LoginActivity
import nl.inholland.myvitality.ui.authentication.register.details.RegisterDetailsActivity
import nl.inholland.myvitality.ui.tutorial.TutorialActivity
import nl.inholland.myvitality.util.SharedPreferenceHelper
import javax.inject.Inject

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : BaseActivity<ActivitySplashScreenBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivitySplashScreenBinding
            = ActivitySplashScreenBinding::inflate


    @Inject lateinit var apiClient: ApiClient
    @Inject lateinit var sharedPrefs: SharedPreferenceHelper

    private var mSingleAccountApp: ISingleAccountPublicClientApplication? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as VitalityApplication).appComponent.inject(this)

        PublicClientApplication.createSingleAccountPublicClientApplication(getApplicationContext(),
            R.raw.auth_config_single_account, object : IPublicClientApplication.ISingleAccountApplicationCreatedListener {
                override fun onCreated(application: ISingleAccountPublicClientApplication?) {
                    mSingleAccountApp = application
                    loadAccount()
                }
                override fun onError(exception: MsalException) {
                    Log.e("LoginActivity", exception.message.toString())
                }
            })

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
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                }
            }

            startActivity(intent)
            finish()
        }, 3000)
    }

    private fun loadAccount() {
        if (mSingleAccountApp == null) {
            return
        }

        mSingleAccountApp!!.getCurrentAccountAsync(object :
            ISingleAccountPublicClientApplication.CurrentAccountCallback {
            override fun onAccountLoaded(activeAccount: IAccount?) {
                // You can use the account data to update your UI or your app database.
                if (activeAccount?.id != null) {
                    Log.e("SplashScreenActivity", "User is logged in to Azure AD")
                } else {
                    Log.e("SplashScreenActivity", "User is not logged in")
                    sharedPrefs.logoutUser()
                }
            }

            override fun onAccountChanged(priorAccount: IAccount?, currentAccount: IAccount?, ) {
                if (currentAccount == null) {
                    Log.e("LoginActivity" , "Current account = null")
                }
            }

            override fun onError(exception: MsalException) {
                Log.e("LoginActivity", exception.message.toString())
            }
        })
    }
}