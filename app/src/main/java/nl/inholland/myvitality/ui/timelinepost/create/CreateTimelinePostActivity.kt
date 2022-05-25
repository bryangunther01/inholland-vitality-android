package nl.inholland.myvitality.ui.timelinepost.create

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import butterknife.OnClick
import butterknife.OnTextChanged
import com.bumptech.glide.Glide
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.architecture.ChosenFragment
import nl.inholland.myvitality.architecture.base.BaseActivity
import nl.inholland.myvitality.data.entities.ResponseStatus
import nl.inholland.myvitality.databinding.ActivityCreatePostBinding
import nl.inholland.myvitality.ui.MainActivity
import nl.inholland.myvitality.ui.timelinepost.view.TimelinePostActivity
import nl.inholland.myvitality.ui.widgets.dialog.Dialogs
import nl.inholland.myvitality.util.ImageUploadUtil
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject


class CreateTimelinePostActivity : BaseActivity<ActivityCreatePostBinding>(), Callback<Void> {

    override val bindingInflater: (LayoutInflater) -> ActivityCreatePostBinding
            = ActivityCreatePostBinding::inflate

    @Inject
    lateinit var factory: CreateTimelinePostViewModelFactory
    lateinit var viewModel: CreateTimelinePostViewModel

    private val PICK_IMAGE = 1000
    private val REQUEST_PERMISSION = 2000

    private var selectedImage: Uri? = null
    private var filePath: String? = null

    var postId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.navigation_post)

        (application as VitalityApplication).appComponent.inject(this)
        viewModel = ViewModelProviders.of(this, factory).get(CreateTimelinePostViewModel::class.java)

        postId = intent.getStringExtra("POST_ID")
        if(postId != null){
            binding.hrBottom.visibility = View.INVISIBLE
            binding.imageUploadButton.visibility = View.INVISIBLE
        }

        binding.imageUploadButton.setOnClickListener {
            requestImage()
        }

        initResponseHandler()
        initUser()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE) if (resultCode == RESULT_OK) {
            selectedImage = data?.data
            filePath = selectedImage?.path

            binding.imagePreview.setImageURI(selectedImage)
            binding.imagePreview.visibility = View.VISIBLE
            binding.imagePreviewRemove.visibility = View.VISIBLE
        }
    }

    @OnClick(R.id.image_preview_remove)
    fun onClickRemoveImage(){
        binding.imagePreview.setImageDrawable(null)
        binding.imagePreview.visibility = View.GONE
        binding.imagePreviewRemove.visibility = View.GONE
    }

    @OnClick(R.id.button)
    fun onClickPostButton() {
        if(postId != null){
            viewModel.createComment(postId!!, binding.message.text.toString())
        } else {
            if(binding.imagePreview.drawable is BitmapDrawable) {
                selectedImage?.let {
                    val bitmap = (binding.imagePreview.drawable as BitmapDrawable).bitmap
                    val file = ImageUploadUtil.convertBitmapToFile(this,"post_image.png", bitmap)
                    val reqFile = RequestBody.create(MediaType.parse("image/jpeg"), file)

                    val filePart = MultipartBody.Part.createFormData("file", file.name, reqFile)

                    viewModel.createTimelinePost(binding.message.text.toString(), filePart)
                    Dialogs.showGeneralLoadingDialog(this)

                    return
                }
            }

            viewModel.createTimelinePost(binding.message.text.toString())
            Dialogs.showGeneralLoadingDialog(this)
        }
    }

    @OnTextChanged(R.id.message)
    fun onTextChanged() {
        binding.button.isEnabled = binding.message.text.isNotEmpty()
    }

    private fun requestImage(){
        val photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, PICK_IMAGE)
    }

    private fun initUser() {
        viewModel.getLoggedInUser()

        viewModel.currentUser.observe(this) { user ->
            Glide.with(this)
                .load(user.profilePicture)
                .into(binding.profileImage)
            binding.userName.text = user.fullName
        }
    }

    override fun onResponse(call: Call<Void>, response: Response<Void>) {
        if (response.isSuccessful) {
            if(postId != null){
                startActivity(
                    Intent(this, TimelinePostActivity::class.java)
                        .putExtra("POST_ID", postId))
            } else {
                startActivity(
                    Intent(this, MainActivity::class.java)
                        .putExtra("FRAGMENT_TO_LOAD", ChosenFragment.FRAGMENT_TIMELINE.ordinal))
            }
        } else {
            Toast.makeText(this, getString(R.string.api_error), Toast.LENGTH_LONG)
                .show()
        }
    }

    override fun onFailure(call: Call<Void>, t: Throwable) {
        Toast.makeText(this, getString(R.string.api_error), Toast.LENGTH_LONG).show()
    }

    private fun initResponseHandler() {
        viewModel.apiResponse.observe(this) { response ->
            when (response.status) {
                ResponseStatus.API_ERROR -> {
                    Toast.makeText(
                        this,
                        getString(R.string.api_error),
                        Toast.LENGTH_LONG
                    ).show()

                    Dialogs.hideCurrentDialog()
                }
                ResponseStatus.CREATED -> {
                    if (postId != null) {
                        startActivity(
                            Intent(this, TimelinePostActivity::class.java)
                                .putExtra("POST_ID", postId)
                        )
                        finish()
                    } else {
                        startActivity(
                            Intent(this, MainActivity::class.java)
                                .putExtra(
                                    "FRAGMENT_TO_LOAD",
                                    ChosenFragment.FRAGMENT_TIMELINE.ordinal
                                )
                        )
                        finish()
                    }
                }
                else -> {
                }
            }
        }
    }
}