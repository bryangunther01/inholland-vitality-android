package nl.inholland.myvitality.ui.authentication.register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
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
import nl.gunther.bryan.newsreader.utils.SharedPreferenceHelper
import nl.inholland.myvitality.ui.authentication.login.LoginActivity
import nl.inholland.myvitality.ui.widgets.dialog.Dialogs


class RegisterActivity : AppCompatActivity(), Callback<Void> {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        ButterKnife.bind(this)

        //(application as VitalityApplication).appComponent.inject(this)

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
        val isValid = email.text.length > 3 &&
                Patterns.EMAIL_ADDRESS.matcher(email.text).matches() &&
                password.text.length > 3 &&
                passwordMatch()

        if (isValid) {
            apiClient.register(AuthRequest(email.text.toString(), password.text.toString()))
                .enqueue(this)
        }
    }

    @OnTextChanged(
        R.id.register_edit_text_email,
        callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED
    )
    fun onEmailInputFieldChanged() {
        if (email.text.length > 3) {
            // TODO: Validate if is inholland email
            val isValid = FieldValidationUtil(this).setFieldState(
                email, Patterns.EMAIL_ADDRESS.matcher(email.text).matches(), errorField, getString(
                    R.string.register_error_invalid_email
                )
            )

            if (isValid && password.text.length > 3 && passwordMatch()) {
                registerButton.isEnabled = true
            }
        }
    }

    @OnTextChanged(value = [R.id.register_edit_text_password, R.id.register_edit_text_password_confirmation], callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    fun onPasswordFieldsChanged() {
        if (password.text.length > 3 && passwordConfirmation.text.length > 3) {
            val fieldValidationUtil = FieldValidationUtil(this)

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
        }
    }

    @OnTextChanged(value = [R.id.register_edit_text_email, R.id.register_edit_text_password, R.id.register_edit_text_password_confirmation], callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    fun onInputFieldsChanged() {
        val isValid = Patterns.EMAIL_ADDRESS.matcher(email.text).matches() &&
            password.text.length > 3 &&
            passwordConfirmation.text.length > 3 &&
            passwordMatch()

        registerButton.isEnabled = isValid
    }

    @OnClick(R.id.register_login)
    fun onClickLogin() {
        val myIntent = Intent(this, LoginActivity::class.java)
        startActivity(myIntent)
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
    }
}