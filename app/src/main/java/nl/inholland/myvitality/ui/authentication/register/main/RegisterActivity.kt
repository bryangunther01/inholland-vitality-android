package nl.inholland.myvitality.ui.authentication.register.main

import android.content.Intent
import android.os.Bundle
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
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.data.entities.requestbody.AuthRequest
import nl.inholland.myvitality.ui.authentication.login.LoginActivity
import nl.inholland.myvitality.ui.widgets.dialog.Dialogs
import nl.inholland.myvitality.util.TextViewUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject


class RegisterActivity : BaseActivity(), Callback<Void> {
    @Inject
    lateinit var apiClient: ApiClient
    @BindView(R.id.register_error)
    lateinit var errorField: TextView
    @BindView(R.id.register_edit_text_email)
    lateinit var email: EditText
    @BindView(R.id.register_edit_text_password)
    lateinit var password: EditText
    @BindView(R.id.register_edit_text_password_confirmation)
    lateinit var passwordConfirmation: EditText
    @BindView(R.id.register_button)
    lateinit var registerButton: Button

    private val PASSWORD_LENGTH = 8

    override fun layoutResourceId(): Int {
        return R.layout.activity_register
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as VitalityApplication).appComponent.inject(this)

        // Set login text
        val loginTextView = findViewById<TextView>(R.id.register_login)
        loginTextView.append(
            TextViewUtils.getColoredString(
                getString(R.string.register_login_info_1) + " ",
                ContextCompat.getColor(this, R.color.black)
            )
        )
        loginTextView.append(
            TextViewUtils.getColoredString(
                getString(R.string.register_login_info_2),
                ContextCompat.getColor(this, R.color.primary)
            )
        )
    }

    @OnClick(R.id.register_button)
    fun onClickRegister() {
        val isValid = email.text.length >= 3 &&
                Patterns.EMAIL_ADDRESS.matcher(email.text).matches() &&
                password.text.length >= PASSWORD_LENGTH &&
                passwordMatch()

        if (isValid) {
            apiClient.register(AuthRequest(email.text.toString(), password.text.toString()))
                .enqueue(this)
            registerButton.isEnabled = false
        }
    }

    @OnTextChanged(R.id.register_edit_text_email, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    fun onEmailInputFieldChanged() {
        if (email.text.length >= 3) {
            val isValid = FieldValidationUtil(this).setFieldState(
                email, Patterns.EMAIL_ADDRESS.matcher(email.text).matches(), errorField, getString(
                    R.string.register_error_invalid_email
                )
            )

            if (isValid && password.text.length > PASSWORD_LENGTH && passwordMatch()) {
                registerButton.isEnabled = true
            }
        }
    }

    @OnTextChanged(value = [R.id.register_edit_text_password, R.id.register_edit_text_password_confirmation], callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    fun onPasswordFieldsChanged() {
        val fieldValidationUtil = FieldValidationUtil(this)

        if (password.text.length > PASSWORD_LENGTH && passwordConfirmation.text.length > PASSWORD_LENGTH) {
            fieldValidationUtil.setFieldState(
                password, passwordMatch(), errorField, getString(
                    R.string.register_error_password_no_match
                )
            )

            fieldValidationUtil.setFieldState(
                passwordConfirmation, passwordMatch(), errorField, getString(
                    R.string.register_error_password_no_match
                )
            )

            return
        }

        fieldValidationUtil.setFieldState(password, password.text.length > PASSWORD_LENGTH, errorField, getString(R.string.register_error_password_length, PASSWORD_LENGTH))
        fieldValidationUtil.setFieldState(passwordConfirmation, password.text.length > PASSWORD_LENGTH, errorField, getString(R.string.register_error_password_length, PASSWORD_LENGTH))
    }

    @OnTextChanged(value = [R.id.register_edit_text_email, R.id.register_edit_text_password, R.id.register_edit_text_password_confirmation], callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    fun onInputFieldsChanged() {
        val isValid = Patterns.EMAIL_ADDRESS.matcher(email.text).matches() &&
            password.text.length >= 3 &&
            passwordConfirmation.text.length >= 3 &&
            passwordMatch()

        registerButton.isEnabled = isValid
    }

    @OnClick(R.id.register_login)
    fun onClickLogin() {
        val myIntent = Intent(this, LoginActivity::class.java)
        startActivity(myIntent)
        finish()
    }

    private fun passwordMatch(): Boolean {
        return passwordConfirmation.text.toString() == password.text.toString();
    }

    override fun onResponse(call: Call<Void>, response: Response<Void>) {
        if (response.isSuccessful) {
            Dialogs.showEmailVerificationDialog(this)
            SharedPreferenceHelper(this).recentlyRegistered = true
        } else {
            if(response.code() == 400){
                FieldValidationUtil(this).setFieldState(email, false, errorField, getString(R.string.register_error_email_in_use))
            } else {
                Toast.makeText(this, getString(R.string.api_error), Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onFailure(call: Call<Void>, t: Throwable) {
        Toast.makeText(this, getString(R.string.api_error), Toast.LENGTH_LONG).show()
        registerButton.isEnabled = true
    }
}