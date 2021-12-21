package nl.inholland.myvitality.ui.authentication.login

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import nl.gunther.bryan.newsreader.utils.FieldValidationUtil
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.data.entities.AuthSettings
import nl.inholland.myvitality.data.entities.requestbody.AuthRequest
import nl.inholland.myvitality.util.TextViewUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import butterknife.*
import android.content.Intent
import nl.gunther.bryan.newsreader.utils.SharedPreferenceHelper
import nl.inholland.myvitality.architecture.base.BaseActivity
import nl.inholland.myvitality.ui.MainActivity
import nl.inholland.myvitality.ui.authentication.register.RegisterActivity
import nl.inholland.myvitality.ui.authentication.register.RegisterDetails2Activity
import nl.inholland.myvitality.ui.widgets.dialog.Dialogs


class LoginActivity : BaseActivity(), Callback<AuthSettings> {
    @Inject lateinit var apiClient: ApiClient
    @Inject lateinit var sharedPrefs: SharedPreferenceHelper
    @BindView(R.id.login_error) lateinit var errorField: TextView
    @BindView(R.id.login_edit_text_email) lateinit var email: EditText
    @BindView(R.id.login_edit_text_password) lateinit var password: EditText
    @BindView(R.id.login_button) lateinit var loginButton: Button

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
    }

    @OnClick(R.id.login_button)
    fun onClickLogin(){
        val isValid = email.text.length > 3 &&
                Patterns.EMAIL_ADDRESS.matcher(email.text).matches() &&
                password.text.length > 3

        if(isValid){
            apiClient.login(AuthRequest(email.text.toString(), password.text.toString())).enqueue(this)
            Dialogs.showGeneralLoadingDialog(this)
        }
    }

    @OnTextChanged(R.id.login_edit_text_email, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    fun onEmailInputFieldChanged(){
        if(email.text.length > 3){
            // TODO: Validate if is inholland email
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
            }

            var intent = Intent(this, MainActivity::class.java)

            if(sharedPrefs.recentlyRegistered) {
                intent = Intent(this, RegisterDetails2Activity::class.java)
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