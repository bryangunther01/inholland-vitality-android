package nl.inholland.myvitality.ui.authentication.register.additionaldetails

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import butterknife.OnClick
import butterknife.OnTextChanged
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.architecture.base.BaseActivity
import nl.inholland.myvitality.data.entities.ResponseStatus
import nl.inholland.myvitality.databinding.ActivityRegisterAdditionalDetailsBinding
import nl.inholland.myvitality.ui.MainActivity
import nl.inholland.myvitality.ui.widgets.dialog.Dialogs
import nl.inholland.myvitality.util.ImageUploadUtil
import nl.inholland.myvitality.util.SharedPreferenceHelper
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject


class RegisterAdditionalDetailsActivity : BaseActivity<ActivityRegisterAdditionalDetailsBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivityRegisterAdditionalDetailsBinding =
        ActivityRegisterAdditionalDetailsBinding::inflate

    @Inject
    lateinit var sharedPrefs: SharedPreferenceHelper

    @Inject
    lateinit var factory: RegisterAdditionalDetailsViewModelFactory
    lateinit var viewModel: RegisterAdditionalDetailsViewModel

    private var selectedImage: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as VitalityApplication).appComponent.inject(this)
        viewModel = ViewModelProviders.of(this, factory).get(RegisterAdditionalDetailsViewModel::class.java)

        initResponseHandler()

        binding.pickImageButton.setOnClickListener {
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, PICK_IMAGE)
        }

        binding.description.hint = getString(R.string.hint_profile_description, DESCRIPTION_LENGTH)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE) if (resultCode == RESULT_OK) {
            selectedImage = data?.data
            binding.pickImageButton.setImageURI(selectedImage)
            binding.pickImageButton.scaleType = ImageView.ScaleType.CENTER_CROP
        }
    }

    @OnTextChanged(R.id.description)
    fun onDescriptionChanged(){
        val isValid = binding.description.text.length >= DESCRIPTION_LENGTH
        binding.button.isEnabled = isValid
    }

    @OnClick(R.id.button)
    fun onClickStart(){
        val isValid = binding.description.text.length >= DESCRIPTION_LENGTH

        if(isValid){

            if(binding.pickImageButton.drawable is BitmapDrawable) {
                selectedImage?.let {
                    val bitmap = (binding.pickImageButton.drawable as BitmapDrawable).bitmap
                    val file = ImageUploadUtil.convertBitmapToFile(this,"profile_image.png", bitmap)
                    val reqFile = RequestBody.create(MediaType.parse("image/jpeg"), file)

                    val filePart = MultipartBody.Part.createFormData("file", file.name, reqFile)

                    sharedPrefs.accessToken?.let{
                        viewModel.updateUserProfile(binding.description.text.toString(), filePart)
                        Dialogs.showGeneralLoadingDialog(this)
                    }

                    return
                }
            }

            sharedPrefs.accessToken?.let{
                viewModel.updateUserProfile(binding.description.text.toString())
                Dialogs.showGeneralLoadingDialog(this)
            }
        }
    }

    private fun initResponseHandler() {
        viewModel.apiResponse.observe(this) { response ->
            when (response.status) {
                ResponseStatus.UPDATED_VALUE -> {
                    sharedPrefs.recentlyRegistered = false

                    startActivity(Intent(this, MainActivity::class.java))
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

    companion object {
        private const val PICK_IMAGE = 1000
        private const val DESCRIPTION_LENGTH = 10
    }
}