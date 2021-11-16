package nl.inholland.myvitality.ui.authentication.login

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.data.entities.AuthSettings
import nl.inholland.myvitality.data.entities.requestbody.LoginRequest
import nl.inholland.myvitality.util.TextViewUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class LoginActivity : AppCompatActivity(), Callback<AuthSettings> {
    @Inject lateinit var apiClient: ApiClient
    @BindView(R.id.login_edit_text_email) lateinit var email: EditText
    @BindView(R.id.login_edit_text_password) lateinit var password: EditText
    @BindView(R.id.button_login) lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        ButterKnife.bind(this)

        (application as VitalityApplication).appComponent.inject(this)

        // Set greeting message
        val titleTextView = findViewById<TextView>(R.id.login_title)
        titleTextView.append(TextViewUtils.getGreetingMessage(this) + ",")

        // Set register text
        val registerTextView = findViewById<TextView>(R.id.login_register)
        registerTextView.append(TextViewUtils.getColoredString(getString(R.string.login_register_info_1) + " ", ContextCompat.getColor(this, R.color.black)))
        registerTextView.append(TextViewUtils.getColoredString(getString(R.string.login_register_info_2), ContextCompat.getColor(this, R.color.primary)))
    }

    @OnClick(R.id.button_login)
    fun onClickLogin(){
        val isValid = email.text.length > 3 && password.text.length > 3
        email.setTextColor(getColor(R.color.red))
        password.setTextColor(getColor(R.color.red))
        loginButton.isEnabled = false
    }

    @OnClick(R.id.login_register)
    fun onClickRegister(){
        // TODO: Replace with real intent change
        Toast.makeText(this,"Go to register", Toast.LENGTH_LONG).show()
    }

    override fun onResponse(call: Call<AuthSettings>, response: Response<AuthSettings>) {
        if(response.isSuccessful){
            println("SUCCESS")
        } else {
            println("ERROR")
        }
    }

    override fun onFailure(call: Call<AuthSettings>, t: Throwable) {
        print("onFailure")
    }
}