package nl.inholland.myvitality.ui.authentication.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import butterknife.BindView
import butterknife.OnClick
import butterknife.OnTextChanged
import nl.gunther.bryan.newsreader.utils.FieldValidationUtil
import nl.inholland.myvitality.util.SharedPreferenceHelper
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.architecture.base.BaseActivity
import nl.inholland.myvitality.data.TokenApiClient
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.data.entities.ApiResponse
import nl.inholland.myvitality.data.entities.AuthSettings
import nl.inholland.myvitality.data.entities.ResponseStatus
import nl.inholland.myvitality.data.entities.requestbody.AuthRequest
import nl.inholland.myvitality.data.entities.requestbody.RegisterRequest
import nl.inholland.myvitality.data.entities.requestbody.PushToken
import nl.inholland.myvitality.ui.MainActivity
import nl.inholland.myvitality.ui.authentication.recover.AccountRecoverActivity
import nl.inholland.myvitality.ui.authentication.register.details1.RegisterDetailsActivity
import nl.inholland.myvitality.ui.authentication.register.main.RegisterActivity
import nl.inholland.myvitality.ui.widgets.dialog.Dialogs
import nl.inholland.myvitality.util.TextViewUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import com.microsoft.identity.client.ISingleAccountPublicClientApplication
import com.microsoft.identity.client.AuthenticationCallback
import com.microsoft.identity.client.*
import com.microsoft.identity.client.exception.*
import com.microsoft.identity.client.exception.MsalException

import androidx.annotation.NonNull

import com.microsoft.identity.client.IAccount
import com.microsoft.identity.client.ISingleAccountPublicClientApplication.CurrentAccountCallback
import com.microsoft.identity.client.IAuthenticationResult

class LoginActivity : BaseActivity(), Callback<AuthSettings> {
    @Inject lateinit var tokenApiClient: TokenApiClient
    @Inject lateinit var apiClient: ApiClient
    @Inject lateinit var sharedPrefs: SharedPreferenceHelper
    @BindView(R.id.login_error) lateinit var errorField: TextView
    @BindView(R.id.login_button) lateinit var loginButton: Button

    private  val SCOPES = arrayOf("api://35596f07-345f-4247-8d77-927e771c35c3/Access")
    private var mSingleAccountApp: ISingleAccountPublicClientApplication? = null

    override fun layoutResourceId(): Int {
        return R.layout.activity_login
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as VitalityApplication).appComponent.inject(this)

        // Set greeting message
        val titleTextView = findViewById<TextView>(R.id.login_title)
        titleTextView.append(TextViewUtils.getGreetingMessage(this) + ",")

        PublicClientApplication.createSingleAccountPublicClientApplication(getApplicationContext(),
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

                val email = authenticationResult.getAccount().username
                val azureToken = authenticationResult.getAccount().id
                val name = authenticationResult.getAccount().claims!!.get("name").toString().split(", ")

                Dialogs.showGeneralLoadingDialog(this@LoginActivity)
                apiClient.userExistsByAzureToken(authenticationResult.getAccount().id).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>){
                        if (response.isSuccessful) {
                            Log.e("LoginActivity" , "User exists")
                            tokenApiClient.login(AuthRequest(email, azureToken)).enqueue(this@LoginActivity)
                        } else if (response.code() == 404) {
                            Log.e("LoginActivity" , "User does not exist, user is being registered")
                            apiClient.register(RegisterRequest(email, azureToken, name[1], name[0])).enqueue(object : Callback<Void> {
                                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                    if (response.isSuccessful) {
                                        Log.i("LoginActivity", "Successfully registered user")
                                        sharedPrefs.recentlyRegistered = true
                                        sharedPrefs.userFirstname = name[1]
                                        sharedPrefs.userLastname = name[0]
                                        tokenApiClient.login(AuthRequest(email, azureToken)).enqueue(this@LoginActivity)
                                    } else if (response.code() == 400) {
                                        Log.e("LoginActivity", "User couldn't be registered")
                                    }
                                }

                                override fun onFailure(call: Call<Void>, t: Throwable) {
                                    Log.e("LoginActivity", "User couldn't be registered")
                                }
                            })
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
        Log.e("LoginActivity", "Executing onClickAzureLogin")

        if (mSingleAccountApp == null) {
            return
        }

        mSingleAccountApp!!.signIn(this, null, SCOPES, getAuthInteractiveCallback())
    }

    override fun onResponse(call: Call<AuthSettings>, response: Response<AuthSettings>) {
        // Hide the loading dialog
        Dialogs.hideCurrentDialog()

        if(response.isSuccessful && response.body() != null){
            response.body()?.let {
                sharedPrefs.accessToken = it.accessToken
                sharedPrefs.refreshToken = it.refreshToken
                sharedPrefs.tokenExpireTime = it.expiresIn
            }

            var intent = Intent(this, MainActivity::class.java)

            tokenApiClient.createPushToken("Bearer ${sharedPrefs.accessToken}", PushToken(sharedPrefs.pushToken!!)).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    Log.i("LoginActivity", "New pushtoken sent to API")
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("LoginActivity", "onFailure: ", t)
                }
            })

            if(sharedPrefs.recentlyRegistered) {
                intent = Intent(this, RegisterDetailsActivity::class.java)
            }

            startActivity(intent)
            finish()
        } else {
            if(response.code() == 401){
                //log back out of Azure AD after failed API login
                azureLogout()
            }
        }
    }

    override fun onFailure(call: Call<AuthSettings>, t: Throwable) {
        Toast.makeText(this,getString(R.string.api_error), Toast.LENGTH_LONG).show()

        //log back out of Azure AD after failed API login
        azureLogout()

        // Hide the loading dialog
        Dialogs.hideCurrentDialog()
    }
}
