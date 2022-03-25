package nl.inholland.myvitality.ui.authentication.register.details1

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import butterknife.BindView
import butterknife.OnClick
import butterknife.OnTextChanged
import nl.inholland.myvitality.util.SharedPreferenceHelper
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.architecture.base.BaseActivity
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.ui.authentication.register.details2.RegisterAdditionalDetailsActivity
import nl.inholland.myvitality.ui.widgets.dialog.Dialogs
import nl.inholland.myvitality.util.RequestUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject


class RegisterDetailsActivity : BaseActivity(), Callback<Void> {
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

    @BindView(R.id.register_details_edit_text_jobtitle)
    lateinit var jobTitle: EditText

    @BindView(R.id.register_details_edit_text_location)
    lateinit var location: EditText

    @BindView(R.id.register_details_1_button)
    lateinit var button: Button

    override fun layoutResourceId(): Int {
        return R.layout.activity_register_details_1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as VitalityApplication).appComponent.inject(this)
    }

    @OnTextChanged(
        value = [R.id.register_details_edit_text_first_name,
            R.id.register_details_edit_text_last_name,
            R.id.register_details_edit_text_jobtitle,
            R.id.register_details_edit_text_location],
        callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED
    )
    fun onFieldsChanged() {
        val isValid = firstName.text.isNotEmpty() &&
                lastName.text.isNotEmpty() &&
                jobTitle.text.isNotEmpty() &&
                location.text.isNotEmpty()

        button.isEnabled = isValid
    }

    @OnClick(R.id.register_details_1_button)
    fun onClickContinue() {
        val isValid = firstName.text.isNotEmpty() &&
                lastName.text.isNotEmpty() &&
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
        } else {
            error.visibility = View.VISIBLE
            error.text = getString(R.string.register_details_error)
        }
    }

    override fun onResponse(call: Call<Void>, response: Response<Void>) {
        if (response.isSuccessful) {
            val intent = Intent(this, RegisterAdditionalDetailsActivity::class.java)
            startActivity(intent)
            finish()

        } else {
            Toast.makeText(this, getString(R.string.api_error), Toast.LENGTH_LONG).show()
        }

        // Hide the loading dialog
        Dialogs.hideCurrentDialog()
    }

    override fun onFailure(call: Call<Void>, t: Throwable) {
        Toast.makeText(this, getString(R.string.api_error), Toast.LENGTH_LONG).show()

        // Hide the loading dialog
        Dialogs.hideCurrentDialog()
    }
}