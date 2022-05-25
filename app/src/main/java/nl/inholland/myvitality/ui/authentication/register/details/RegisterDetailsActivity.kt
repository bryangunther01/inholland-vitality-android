package nl.inholland.myvitality.ui.authentication.register.details

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import butterknife.OnClick
import butterknife.OnTextChanged
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.architecture.base.BaseActivity
import nl.inholland.myvitality.data.entities.ResponseStatus
import nl.inholland.myvitality.databinding.ActivityRegisterDetailsBinding
import nl.inholland.myvitality.ui.authentication.register.additionaldetails.RegisterAdditionalDetailsActivity
import nl.inholland.myvitality.ui.widgets.dialog.Dialogs
import nl.inholland.myvitality.util.SharedPreferenceHelper
import javax.inject.Inject


class RegisterDetailsActivity : BaseActivity<ActivityRegisterDetailsBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivityRegisterDetailsBinding =
        ActivityRegisterDetailsBinding::inflate

    @Inject
    lateinit var factory: RegisterDetailsViewModelFactory
    lateinit var viewModel: RegisterDetailsViewModel

    @Inject
    lateinit var sharedPrefs: SharedPreferenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as VitalityApplication).appComponent.inject(this)
        viewModel = ViewModelProviders.of(this, factory).get(RegisterDetailsViewModel::class.java)

        binding.firstName.setText(sharedPrefs.userFirstname)
        binding.lastName.setText(sharedPrefs.userLastname)

        initResponseHandler()
    }

    @OnTextChanged(
        value = [R.id.first_name,
            R.id.last_name,
            R.id.job_title,
            R.id.location],
        callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED
    )
    fun onFieldsChanged() {
        val isValid = binding.firstName.text.isNotEmpty() &&
                binding.lastName.text.isNotEmpty() &&
                binding.jobTitle.text.isNotEmpty() &&
                binding.location.text.isNotEmpty()

        binding.button.isEnabled = isValid
    }

    @OnClick(R.id.button)
    fun onClickContinue() {
        val isValid = binding.firstName.text.isNotEmpty() &&
                binding.lastName.text.isNotEmpty() &&
                binding.jobTitle.text.isNotEmpty() &&
                binding.location.text.isNotEmpty()

        if (isValid) {
            viewModel.updateUserProfile(
                binding.firstName.text.toString(),
                binding.lastName.text.toString(),
                binding.jobTitle.text.toString(),
                binding.location.text.toString()
            )

            Dialogs.showGeneralLoadingDialog(this)
        } else {
            binding.error.visibility = View.VISIBLE
            binding.error.text = getString(R.string.register_details_error)
        }
    }

    private fun initResponseHandler() {
        viewModel.apiResponse.observe(this) { response ->
            when (response.status) {
                ResponseStatus.UPDATED_VALUE -> {
                    startActivity(Intent(this, RegisterAdditionalDetailsActivity::class.java))
                    finish()
                }
                else -> {
                    Toast.makeText(
                        this,
                        getString(R.string.api_error),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            Dialogs.hideCurrentDialog()
        }
    }
}