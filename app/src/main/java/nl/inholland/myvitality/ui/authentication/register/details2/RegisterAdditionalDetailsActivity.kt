package nl.inholland.myvitality.ui.authentication.register.details2

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.widget.*
import butterknife.BindView
import butterknife.OnClick
import butterknife.OnTextChanged
import nl.gunther.bryan.newsreader.utils.SharedPreferenceHelper
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.architecture.base.BaseActivity
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.ui.MainActivity
import nl.inholland.myvitality.ui.widgets.dialog.Dialogs
import nl.inholland.myvitality.util.ImageUploadUtil
import nl.inholland.myvitality.util.RequestUtils
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject


class RegisterAdditionalDetailsActivity : BaseActivity(), Callback<Void> {
    @Inject
    lateinit var apiClient: ApiClient
    @Inject
    lateinit var sharedPrefs: SharedPreferenceHelper

    @BindView(R.id.register_details_error)
    lateinit var error: TextView
    @BindView(R.id.register_details_pick_image_button)
    lateinit var imageButton: ImageButton
    @BindView(R.id.register_details_edit_text_description)
    lateinit var description: EditText
    @BindView(R.id.register_details_2_button)
    lateinit var button: Button

    private val PICK_IMAGE = 1000
    private val DESCRIPTION_LENGTH = 10

    private var selectedImage: Uri? = null
    private var filePath: String? = null

    override fun layoutResourceId(): Int {
        return R.layout.activity_register_details_2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as VitalityApplication).appComponent.inject(this)

        imageButton.setOnClickListener {
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, PICK_IMAGE)
        }

        description.hint = getString(R.string.hint_profile_description, DESCRIPTION_LENGTH)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE) if (resultCode == RESULT_OK) {
            selectedImage = data?.data
            filePath = selectedImage?.path
            imageButton.setImageURI(selectedImage)
            imageButton.scaleType = ImageView.ScaleType.CENTER_CROP
        }
    }

    @OnTextChanged(R.id.register_details_edit_text_description)
    fun onDescriptionChanged(){
        val isValid = description.text.length >= DESCRIPTION_LENGTH
        button.isEnabled = isValid
    }

    @OnClick(R.id.register_details_2_button)
    fun onClickStart(){
        val isValid = description.text.length >= DESCRIPTION_LENGTH

        if(isValid){
            val description = RequestUtils.createPartFromString(description.text.toString())

            if(imageButton.drawable is BitmapDrawable) {
                selectedImage?.let {
                    val bitmap = (imageButton.drawable as BitmapDrawable).bitmap
                    val file = ImageUploadUtil.convertBitmapToFile(this,"profile_image.png", bitmap)
                    val reqFile = RequestBody.create(MediaType.parse("image/jpeg"), file)

                    val filePart = MultipartBody.Part.createFormData("file", file.name, reqFile)

                    sharedPrefs.accessToken?.let{
                        apiClient.updateUserProfile("Bearer $it", description = description, file = filePart).enqueue(this)
                        Dialogs.showGeneralLoadingDialog(this)
                    }

                    return
                }
            }

            sharedPrefs.accessToken?.let{
                apiClient.updateUserProfile("Bearer $it", description = description).enqueue(this)
                Dialogs.showGeneralLoadingDialog(this)
            }
        }
    }

    override fun onResponse(call: Call<Void>, response: Response<Void>) {
        if (response.isSuccessful) {
            startActivity(Intent(this, MainActivity::class.java))
            sharedPrefs.recentlyRegistered = false
        } else {
            Toast.makeText(this, getString(R.string.api_error), Toast.LENGTH_LONG).show()
        }

        // Hide the loading dialog
        Dialogs.hideCurrentDialog()
    }

    override fun onFailure(call: Call<Void>, t: Throwable) {
        Toast.makeText(this, t.message.toString(), Toast.LENGTH_LONG).show()

        // Hide the loading dialog
        Dialogs.hideCurrentDialog()
    }
}