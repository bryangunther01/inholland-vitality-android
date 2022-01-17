package nl.inholland.myvitality.ui.profile.edit

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import butterknife.BindView
import butterknife.OnClick
import butterknife.OnTextChanged
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.imageview.ShapeableImageView
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.architecture.base.BaseActivity
import nl.inholland.myvitality.data.entities.ResponseStatus
import nl.inholland.myvitality.ui.profile.overview.ProfileActivity
import nl.inholland.myvitality.ui.widgets.dialog.Dialogs
import nl.inholland.myvitality.util.ImageUploadUtil
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class ProfileEditActivity : BaseActivity() {

    @Inject
    lateinit var factory: ProfileEditViewModelFactory
    lateinit var viewModel: ProfileEditViewModel

    @BindView(R.id.profile_edit_image)
    lateinit var image: ShapeableImageView

    @BindView(R.id.profile_edit_text_first_name)
    lateinit var firstName: EditText

    @BindView(R.id.profile_edit_text_last_name)
    lateinit var lastName: EditText

    @BindView(R.id.profile_edit_text_jobtitle)
    lateinit var jobTitle: EditText

    @BindView(R.id.profile_edit_text_location)
    lateinit var location: EditText

    @BindView(R.id.profile_edit_text_description)
    lateinit var description: EditText

    @BindView(R.id.profile_edit_button)
    lateinit var button: Button

    private val DESCRIPTION_LENGTH = 10
    private val PICK_IMAGE = 1000
    private var selectedImage: Uri? = null
    private var filePath: String? = null

    override fun layoutResourceId(): Int {
        return R.layout.activity_profile_edit
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as VitalityApplication).appComponent.inject(this)
        viewModel = ViewModelProviders.of(this, factory).get(ProfileEditViewModel::class.java)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.navigation_settings)

        description.hint = getString(R.string.hint_profile_description, DESCRIPTION_LENGTH)

        initResponseHandler()
        initUser()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, ProfileActivity::class.java))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE) if (resultCode == RESULT_OK) {
            selectedImage = data?.data
            filePath = selectedImage?.path

            image.setImageURI(selectedImage)
        }
    }

    @OnTextChanged(
        value = [R.id.profile_edit_text_first_name,
            R.id.profile_edit_text_last_name,
            R.id.profile_edit_text_jobtitle,
            R.id.profile_edit_text_location,
            R.id.profile_edit_text_description, ],
        callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED
    )
    fun onFieldsChanged() {
        val isValid = firstName.text.isNotEmpty() &&
                lastName.text.isNotEmpty() &&
                jobTitle.text.isNotEmpty() &&
                location.text.isNotEmpty() &&
                description.text.length >= DESCRIPTION_LENGTH

        button.isEnabled = isValid
    }

    @OnClick(value = [R.id.profile_edit_image, R.id.profile_edit_image_modify])
    fun onClickImage(){
        val photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, PICK_IMAGE)
    }

    @OnClick(R.id.profile_edit_button)
    fun onClickSave() {
        val isValid = firstName.text.isNotEmpty() &&
                lastName.text.isNotEmpty() &&
                jobTitle.text.isNotEmpty() &&
                location.text.isNotEmpty() &&
                description.text.length >= DESCRIPTION_LENGTH

        if (isValid) {
            var filePart: MultipartBody.Part? = null

            if(image.drawable is BitmapDrawable) {
                selectedImage?.let {
                    val bitmap = (image.drawable as BitmapDrawable).bitmap
                    val file = ImageUploadUtil.convertBitmapToFile(this,"post_image.png", bitmap)
                    val reqFile = RequestBody.create(MediaType.parse("image/jpeg"), file)

                    filePart = MultipartBody.Part.createFormData("file", file.name, reqFile)
                }
            }

            viewModel.updateUserProfile(
                firstName.text.toString(),
                lastName.text.toString(),
                jobTitle.text.toString(),
                location.text.toString(),
                description.text.toString(),
                filePart)
            Dialogs.showGeneralLoadingDialog(this)
        } else {
            Toast.makeText(this, getString(R.string.login_error_empty_fields), Toast.LENGTH_LONG).show()
        }
    }

    private fun initUser() {
        viewModel.getLoggedInUser()
        viewModel.currentUser.observe(this, { user ->
            Glide.with(this)
                .load(user.profilePicture)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(image)
            firstName.setText(user.firstName)
            lastName.setText(user.lastName)
            jobTitle.setText(user.jobTitle)
            location.setText(user.location)
            description.setText(user.description)
        })
    }

    private fun initResponseHandler() {
        viewModel.apiResponse.observe(this, { response ->
            when (response.status) {
                ResponseStatus.API_ERROR -> Toast.makeText(
                    this,
                    getString(R.string.api_error),
                    Toast.LENGTH_LONG
                ).show()
                ResponseStatus.UPDATED_VALUE -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                }
                else -> {
                }
            }
        })
    }
}