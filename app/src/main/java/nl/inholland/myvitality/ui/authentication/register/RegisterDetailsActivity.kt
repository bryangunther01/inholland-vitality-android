package nl.inholland.myvitality.ui.authentication.register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.data.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import butterknife.*
import nl.gunther.bryan.newsreader.utils.SharedPreferenceHelper
import nl.inholland.myvitality.data.entities.requestbody.ProfileDetails
import nl.inholland.myvitality.ui.MainActivity
import nl.inholland.myvitality.ui.home.HomeFragment
import nl.inholland.myvitality.ui.widgets.dialog.Dialogs
import nl.inholland.myvitality.util.RequestUtils
import okhttp3.MultipartBody
import okhttp3.RequestBody


class RegisterDetailsActivity : AppCompatActivity(), Callback<Void> {
    @Inject
    lateinit var apiClient: ApiClient

    @Inject
    lateinit var sharedPrefs: SharedPreferenceHelper

    @BindView(R.id.register_details_error)
    lateinit var error: TextView

    @BindView(R.id.register_details_edit_text_first_name)
    lateinit var firstName: EditText

    @BindView(R.id.register_details_edit_text_last_name)
    lateinit var lastName: EditText

    @BindView(R.id.register_details_edit_text_department)
    lateinit var department: EditText

    @BindView(R.id.register_details_edit_text_jobtitle)
    lateinit var jobTitle: EditText

    @BindView(R.id.register_details_edit_text_location)
    lateinit var location: EditText

    @BindView(R.id.register_details_1_button)
    lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_details_1)
        ButterKnife.bind(this)

        (application as VitalityApplication).appComponent.inject(this)
    }

    @OnTextChanged(
        value = [R.id.register_details_edit_text_first_name,
            R.id.register_details_edit_text_last_name,
            R.id.register_details_edit_text_department,
            R.id.register_details_edit_text_jobtitle,
            R.id.register_details_edit_text_location],
        callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED
    )
    fun onFieldsChanged() {
        val isValid = firstName.text.isNotEmpty() &&
                lastName.text.isNotEmpty() &&
                department.text.isNotEmpty() &&
                jobTitle.text.isNotEmpty() &&
                location.text.isNotEmpty()

        button.isEnabled = isValid
    }

    @OnClick(R.id.register_details_1_button)
    fun onClickContinue() {
        val isValid = firstName.text.isNotEmpty() &&
                lastName.text.isNotEmpty() &&
                department.text.isNotEmpty() &&
                jobTitle.text.isNotEmpty() &&
                location.text.isNotEmpty()

        if (isValid) {

            sharedPrefs.accessToken?.let {
                apiClient.updateUserProfile("Bearer $it",
                    firstName = RequestUtils.createPartFromString(firstName.text.toString()),
                    lastName = RequestUtils.createPartFromString(lastName.text.toString()),
                    jobTitle = RequestUtils.createPartFromString(jobTitle.text.toString()),
                    location = RequestUtils.createPartFromString(location.text.toString())).enqueue(this)

                Dialogs.showGeneralLoadingDialog(this)
            }
            // TODO: Extra error handling
        } else {
            error.visibility = View.VISIBLE
            error.text = getString(R.string.register_details_error)
        }
    }

    override fun onResponse(call: Call<Void>, response: Response<Void>) {
        if (response.isSuccessful) {
            val sharedPref =  SharedPreferenceHelper(this)
            sharedPref.userFullName = firstName.text.toString() + " " + lastName.text.toString()

            val intent = Intent(this, RegisterDetails2Activity::class.java)
            startActivity(intent)
            finish()

        } else {
            Toast.makeText(this, getString(R.string.api_error), Toast.LENGTH_LONG).show()
        }

        // Hide the loading dialog
        Dialogs.hideCurrentLoadingDialog()
    }

    override fun onFailure(call: Call<Void>, t: Throwable) {
        Toast.makeText(this, getString(R.string.api_error), Toast.LENGTH_LONG).show()

        // Hide the loading dialog
        Dialogs.hideCurrentLoadingDialog()
    }
}