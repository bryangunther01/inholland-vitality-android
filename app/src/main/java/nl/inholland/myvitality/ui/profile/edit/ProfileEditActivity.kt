package nl.inholland.myvitality.ui.profile.edit

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.devstune.searchablemultiselectspinner.SearchableItem
import com.devstune.searchablemultiselectspinner.SearchableMultiSelectSpinner
import com.devstune.searchablemultiselectspinner.SelectionCompleteListener
import com.microsoft.identity.client.IPublicClientApplication
import com.microsoft.identity.client.ISingleAccountPublicClientApplication
import com.microsoft.identity.client.PublicClientApplication
import com.microsoft.identity.client.exception.MsalException
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.architecture.base.BaseActivityTest
import nl.inholland.myvitality.data.entities.ResponseStatus
import nl.inholland.myvitality.databinding.ActivityProfileEditBinding
import nl.inholland.myvitality.ui.authentication.login.LoginActivity
import nl.inholland.myvitality.ui.profile.overview.ProfileActivity
import nl.inholland.myvitality.ui.widgets.dialog.Dialogs
import nl.inholland.myvitality.util.ImageUploadUtil
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject


class ProfileEditActivity : BaseActivityTest<ActivityProfileEditBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivityProfileEditBinding
            = ActivityProfileEditBinding::inflate

    @Inject
    lateinit var factory: ProfileEditViewModelFactory
    lateinit var viewModel: ProfileEditViewModel

    private var mSingleAccountApp: ISingleAccountPublicClientApplication? = null
    private var selectedImage: Uri? = null
    private var filePath: String? = null

    private var searchableInterests = mutableListOf<SearchableItem>()
    private var hasOpenedInterestDialog: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as VitalityApplication).appComponent.inject(this)
        viewModel = ViewModelProviders.of(this, factory).get(ProfileEditViewModel::class.java)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.navigation_settings)

        binding.description.hint = getString(R.string.hint_profile_description, DESCRIPTION_LENGTH)

        initResponseHandler()
        initUser()

        Handler(Looper.getMainLooper()).postDelayed({
            initInterests()
        }, 1000)

        setupListeners()

        PublicClientApplication.createSingleAccountPublicClientApplication(applicationContext,
            R.raw.auth_config_single_account, object : IPublicClientApplication.ISingleAccountApplicationCreatedListener {
                override fun onCreated(application: ISingleAccountPublicClientApplication?) {
                    mSingleAccountApp = application
                }
                override fun onError(exception: MsalException) {
                    Log.i("ProfileEditActivity", exception.message.toString())
                }
            })
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

            binding.profilePicture.setImageURI(selectedImage)
        }
    }

    private fun setupListeners(){
        // Setup the text watcher to check for changes
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                val isValid = validateFields()
                binding.saveButton.isEnabled = isValid
            }
        }

        binding.firstName.addTextChangedListener(textWatcher)
        binding.lastName.addTextChangedListener(textWatcher)
        binding.jobTitle.addTextChangedListener(textWatcher)
        binding.location.addTextChangedListener(textWatcher)
        binding.description.addTextChangedListener(textWatcher)

        // Set the click listener for the profile picture change
        val clickListener = View.OnClickListener {
            val photoPickerIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(photoPickerIntent, PICK_IMAGE)
        }

        binding.profilePicture.setOnClickListener(clickListener)
        binding.modify.setOnClickListener(clickListener)

        // Set the click listener for the delete profile button
        binding.deleteButton.setOnClickListener {
            Dialogs.showAccountDeletionDialog(this) {
                viewModel.deleteAccount()
            }

            // sign out of Azure AD after deleting account
            mSingleAccountApp!!.signOut(object : ISingleAccountPublicClientApplication.SignOutCallback {
                override fun onSignOut() {
                    Log.i("ProfileEditActivity" , "Successfully signed out of Azure AD")
                }

                override fun onError(exception: MsalException) {
                    Log.i("ProfileEditActivity" , exception.message.toString())
                }
            })
        }

        // Set the click listener for the save button
        binding.saveButton.setOnClickListener {
            val isValid = validateFields()

            if (isValid) {
                var filePart: MultipartBody.Part? = null

                if(binding.profilePicture.drawable is BitmapDrawable) {
                    selectedImage?.let {
                        val bitmap = (binding.profilePicture.drawable as BitmapDrawable).bitmap
                        val file = ImageUploadUtil.convertBitmapToFile(this,"post_image.png", bitmap)
                        val reqFile = RequestBody.create(MediaType.parse("image/jpeg"), file)

                        filePart = MultipartBody.Part.createFormData("file", file.name, reqFile)
                    }
                }

                val interests = viewModel.selectedInterests.value?.joinToString(separator = ",") { it.interestId }

                Dialogs.showGeneralLoadingDialog(this)
                viewModel.updateUserProfile(binding.firstName.text.toString(), binding.lastName.text.toString(), binding.jobTitle.text.toString(), binding.location.text.toString(), binding.description.text.toString(), interests, filePart = filePart)
            } else {
                Toast.makeText(this, getString(R.string.login_error_empty_fields), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initUser() {
        viewModel.getLoggedInUser()
        viewModel.currentUser.observe(this) { user ->
            user.profilePicture?.let {
                Glide.with(this)
                    .load(user.profilePicture)
                    .placeholder(R.drawable.person_placeholder)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(binding.profilePicture)
            }

            binding.firstName.setText(user.firstName)
            binding.lastName.setText(user.lastName)
            binding.jobTitle.setText(user.jobTitle)
            binding.location.setText(user.location)
            binding.description.setText(user.description)
        }
    }

    private fun initInterests(){
        viewModel.getInterests()

        binding.interests.setOnClickListener {
            if(!hasOpenedInterestDialog) {
                hasOpenedInterestDialog = true

                SearchableMultiSelectSpinner.show(
                    this,
                    getString(R.string.profile_edit_interest_title),
                    getString(R.string.profile_edit_interest_button), searchableInterests, object :
                        SelectionCompleteListener {
                        override fun onCompleteSelection(selectedItems: ArrayList<SearchableItem>) {
                            val selectedInterests = selectedItems.map { it.code }
                            viewModel.setSelectedInterests(selectedInterests)

                            hasOpenedInterestDialog = false
                        }

                    })
            }
        }

        viewModel.interests.observe(this) { interests ->
            interests.forEach {
                val searchableItem = SearchableItem(it.name, it.interestId)

                if(viewModel.selectedInterests.value != null) {
                    viewModel.selectedInterests.value?.let { selectedInterests ->
                        if(selectedInterests.contains(it)) searchableItem.isSelected = true
                    }
                }

                searchableInterests.add(searchableItem)
            }
        }

        viewModel.selectedInterests.observe(this) { interests ->
            binding.interests.setText(interests.joinToString { it.name })
        }
    }

    private fun validateFields(): Boolean{
        return binding.firstName.text.isNotEmpty() &&
                binding.lastName.text.isNotEmpty() &&
                binding.jobTitle.text.isNotEmpty() &&
                binding.location.text.isNotEmpty() &&
                binding.description.text.length >= DESCRIPTION_LENGTH
    }

    private fun initResponseHandler() {
        viewModel.apiResponse.observe(this) { response ->
            when (response.status) {
                ResponseStatus.API_ERROR -> Toast.makeText(
                    this,
                    getString(R.string.api_error),
                    Toast.LENGTH_LONG
                ).show()
                ResponseStatus.UPDATED_VALUE -> {
                    Dialogs.hideCurrentDialog()

                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                }
                ResponseStatus.DELETED -> {
                    finishAffinity()
                    startActivity(Intent(this, LoginActivity::class.java))
                }
                else -> {
                }
            }
        }
    }

    companion object {
        const val DESCRIPTION_LENGTH = 10
        const val PICK_IMAGE = 1000
    }
}
