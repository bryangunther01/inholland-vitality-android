package nl.inholland.myvitality.ui.authentication.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import butterknife.OnClick
import com.microsoft.identity.client.*
import com.microsoft.identity.client.exception.MsalException
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.architecture.base.BaseActivity
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.data.TokenApiClient
import nl.inholland.myvitality.data.entities.ResponseStatus
import nl.inholland.myvitality.databinding.ActivityLoginBinding
import nl.inholland.myvitality.ui.MainActivity
import nl.inholland.myvitality.ui.authentication.register.details.RegisterDetailsActivity
import nl.inholland.myvitality.ui.widgets.dialog.Dialogs
import nl.inholland.myvitality.util.SharedPreferenceHelper
import nl.inholland.myvitality.util.TextViewUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class LoginActivity : BaseActivity<ActivityLoginBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivityLoginBinding
            = ActivityLoginBinding::inflate

    @Inject
    lateinit var factory: LoginViewModelFactory
    lateinit var viewModel: LoginViewModel

    @Inject lateinit var tokenApiClient: TokenApiClient
    @Inject lateinit var apiClient: ApiClient
    @Inject lateinit var sharedPrefs: SharedPreferenceHelper

    private var mSingleAccountApp: ISingleAccountPublicClientApplication? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as VitalityApplication).appComponent.inject(this)

        // Set greeting message
        val titleTextView = findViewById<TextView>(R.id.login_title)
        titleTextView.append(TextViewUtils.getGreetingMessage(this) + ",")

        (application as VitalityApplication).appComponent.inject(this)
        viewModel = ViewModelProviders.of(this, factory).get(LoginViewModel::class.java)

        initResponseHandler()

        PublicClientApplication.createSingleAccountPublicClientApplication(applicationContext,
            R.raw.auth_config_single_account, object : IPublicClientApplication.ISingleAccountApplicationCreatedListener {
                override fun onCreated(application: ISingleAccountPublicClientApplication?) {
                    mSingleAccountApp = application
                }
                override fun onError(exception: MsalException) {
                    Log.e("LoginActivity", exception.message.toString())
                }
            })
    }

    private fun getAuthInteractiveCallback(): AuthenticationCallback {
        return object : AuthenticationCallback {
            override fun onSuccess(authenticationResult: IAuthenticationResult) {
                Log.d("LoginActivity", "Successfully authenticated")

                val email = authenticationResult.account.username
                val azureToken = authenticationResult.account.id
                val name = authenticationResult.account.claims!!["name"].toString().split(", ")

                Dialogs.showGeneralLoadingDialog(this@LoginActivity)
                apiClient.userExistsByAzureToken(authenticationResult.account.id).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>){
                        if (response.isSuccessful) {
                            Log.e("LoginActivity" , "User exists")
                            viewModel.login(email, azureToken)
                        } else if (response.code() == 404) {
                            Log.e("LoginActivity" , "User does not exist, user is being registered")
                            viewModel.register(email, azureToken, name[1], name[0])
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable){
                        Log.e("LoginActivity" , t.message.toString())
                    }
                })
            }

            override fun onError(exception: MsalException) {
                /* Failed to acquireToken */
                Log.e("LoginActivity" , exception.message.toString())
            }

            override fun onCancel() {
                /* User canceled the authentication */
                Log.e("LoginActivity", "User cancelled login.")
            }
        }
    }

    private fun azureLogout() {
        mSingleAccountApp!!.signOut(object : ISingleAccountPublicClientApplication.SignOutCallback {
            override fun onSignOut() {
                Log.e("LoginActivity" , "Successfully signed out of Azure AD")
            }

            override fun onError(exception: MsalException) {
                Log.e("LoginActivity" , exception.message.toString())
            }
        })
    }

    @OnClick(R.id.login_button)
    fun onClickAzureLogin() {
        azureLogout()

        if (mSingleAccountApp == null) {
            return
        }

        mSingleAccountApp!!.signIn(this, null, SCOPES, getAuthInteractiveCallback())
    }

    private fun initResponseHandler() {
        viewModel.apiResponse.observe(this) { response ->
            when (response.status) {
                ResponseStatus.API_ERROR -> {
                    Toast.makeText(
                        this,
                        getString(R.string.api_error),
                        Toast.LENGTH_LONG
                    ).show()

                    azureLogout()
                }
                ResponseStatus.SUCCESSFUL -> {
                    Dialogs.hideCurrentDialog()

                    var intent = Intent(this, MainActivity::class.java)

                    if(sharedPrefs.recentlyRegistered) {
                        intent = Intent(this, RegisterDetailsActivity::class.java)
                    }

                    viewModel.registerPushToken()

                    startActivity(intent)
                    finish()
                }
                ResponseStatus.UNAUTHORIZED -> {
                    Dialogs.hideCurrentDialog()
                    azureLogout()
                }
                else -> {
                }
            }
        }
    }

    companion object {
        private val SCOPES = arrayOf("api://35596f07-345f-4247-8d77-927e771c35c3/Access")
    }
}
