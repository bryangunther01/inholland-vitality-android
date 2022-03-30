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
    @BindView(R.id.login_edit_text_email) lateinit var email: EditText
    @BindView(R.id.login_edit_text_password) lateinit var password: EditText
    @BindView(R.id.login_button) lateinit var loginButton: Button

    private final val SCOPES = arrayOf("api://35596f07-345f-4247-8d77-927e771c35c3/Access")
    val AUTHORITY: kotlin.String? = "https://login.microsoftonline.com/common"
    private var mSingleAccountApp: ISingleAccountPublicClientApplication? = null
    private var userEmail: String? = null

    override fun layoutResourceId(): Int {
        return R.layout.activity_login
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as VitalityApplication).appComponent.inject(this)

        // Set greeting message
        val titleTextView = findViewById<TextView>(R.id.login_title)
        titleTextView.append(TextViewUtils.getGreetingMessage(this) + ",")

        // Set register text
        val registerTextView = findViewById<TextView>(R.id.login_register)
        registerTextView.append(TextViewUtils.getColoredString(getString(R.string.login_register_info_1) + " ", ContextCompat.getColor(this, R.color.black)))
        registerTextView.append(TextViewUtils.getColoredString(getString(R.string.login_register_info_2), ContextCompat.getColor(this, R.color.primary)))

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
    }

    private fun loadAccount() {
        if (mSingleAccountApp == null) {
            return
        }

        mSingleAccountApp!!.getCurrentAccountAsync(object : CurrentAccountCallback {
            override fun onAccountLoaded(activeAccount: IAccount?) {
                // You can use the account data to update your UI or your app database.
                Log.e("LoginActivity", "ID: ${activeAccount?.id}")
                Log.e("LoginActivity", "Email: ${activeAccount?.username}")
                Log.e("LoginActivity", "Name: ${activeAccount?.claims?.get("name")}")
            }

            override fun onAccountChanged(
                priorAccount: IAccount?,
                currentAccount: IAccount?,
            ) {
                if (currentAccount == null) {
                    // Perform a cleanup task as the signed-in account changed.
                    //performOperationOnSignOut()
                    Log.e("LoginActivity" , "Current account = null")
                }
            }

            override fun onError(exception: MsalException) {
                Log.e("LoginActivity", exception.message.toString())
            }
        })
    }

    private fun getAuthInteractiveCallback(): com.microsoft.identity.client.AuthenticationCallback {
        return object : com.microsoft.identity.client.AuthenticationCallback {
            override fun onSuccess(authenticationResult: IAuthenticationResult) {
                /* Successfully got a token, use it to call a protected resource - MSGraph */
                sharedPrefs.azureToken = authenticationResult.getAccount().id
                userEmail = authenticationResult.getAccount().username
                Log.d("LoginActivity", "Successfully authenticated")
                Log.e("LoginActivity", "ID: ${authenticationResult.getAccount().id}")
                Log.e("LoginActivity", "Email: ${authenticationResult.getAccount().username}")
                Log.e("LoginActivity", "Name: ${authenticationResult.getAccount().claims!!.get("name")}")


//                if (apiClient.userExistsByAzureToken(authenticationResult.getAccount().id) == true) {
//                    Log.e("LoginActivity", "User exists and is being logged in")
//                    Dialogs.showGeneralLoadingDialog(this)
//                    tokenApiClient.login(AuthRequest(authenticationResult.getAccount().username,
//                        authenticationResult.getAccount().id)).enqueue(this)
//                } else {
//                    Log.e("LoginActivity", "User does not exist")
//                }
                sharedPrefs.azureToken?.let {
                    apiClient.userExistsByAzureToken(authenticationResult.getAccount().id).enqueue(object : Callback<Boolean> {
                        override fun onResponse(call: Call<Boolean>, response: Response<Boolean>){
                            if (response.isSuccessful) {
                                Log.e("LoginActivity" , "User exists")
                                //tokenApiClient.login(AuthRequest(authenticationResult.getAccount().username,
                                //    authenticationResult.getAccount().id)).enqueue(this)
                            }
                        }

                        override fun onFailure(call: Call<Boolean>, t: Throwable){
                            Log.e("LoginActivity" , "User does not exist")
                        }
                    })
                }
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

    @OnClick(R.id.azure_login_button)
    fun onClickAzureLogin() {
        Log.e("LoginActivity", "Executing onClickAzureLogin")

        if (mSingleAccountApp == null) {
            return
        }

        mSingleAccountApp!!.signIn(this, null, SCOPES, getAuthInteractiveCallback())
    }

    @OnClick(R.id.azure_logout_button)
    fun onClickAzureLogout() {
        Log.e("LoginActivity" , "Executing onClickAzureLogout")

        if (mSingleAccountApp == null) {
            return
        }

        mSingleAccountApp!!.signOut(object : ISingleAccountPublicClientApplication.SignOutCallback {
            override fun onSignOut() {
                Log.e("LoginActivity" , "Successfully signed out of Azure AD")
                loadAccount()
            }

            override fun onError(exception: MsalException) {
                Log.e("LoginActivity" , exception.message.toString())
            }
        })
    }

    @OnClick(R.id.login_button)
    fun onClickLogin(){
        val isValid = email.text.length > 3 &&
                Patterns.EMAIL_ADDRESS.matcher(email.text).matches() &&
                password.text.length > 3

        if(isValid){
            Dialogs.showGeneralLoadingDialog(this)
            tokenApiClient.login(AuthRequest(email.text.toString(), password.text.toString())).enqueue(this)
        }
    }

    @OnClick(R.id.login_forgot_password)
    fun onClickForgotPassword() {
        startActivity(Intent(this, AccountRecoverActivity::class.java))
        finish()
    }

    @OnTextChanged(R.id.login_edit_text_email, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    fun onEmailInputFieldChanged(){
        if(email.text.length > 3){
            FieldValidationUtil(this).setFieldState(email, Patterns.EMAIL_ADDRESS.matcher(email.text).matches(), errorField, getString(
                R.string.login_error_invalid_email))
        }
    }

    @OnTextChanged(value = [R.id.login_edit_text_email, R.id.login_edit_text_password], callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    fun onInputFieldsChanged(){
        val isValid = Patterns.EMAIL_ADDRESS.matcher(email.text).matches() && password.text.length > 3
        loginButton.isEnabled = isValid
    }

    @OnClick(R.id.login_register)
    fun onClickRegister(){
        val myIntent = Intent(this, RegisterActivity::class.java)
        startActivity(myIntent)

        finish()
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
                FieldValidationUtil(this).setFieldState(email, false, errorField, getString(
                    R.string.login_error_invalid_combination))
                FieldValidationUtil(this).setFieldState(password, false, errorField, getString(
                    R.string.login_error_invalid_combination))
            }
        }
    }

    override fun onFailure(call: Call<AuthSettings>, t: Throwable) {
        Toast.makeText(this,getString(R.string.api_error), Toast.LENGTH_LONG).show()

        // Hide the loading dialog
        Dialogs.hideCurrentDialog()
    }
}
