package nl.inholland.myvitality.ui.authentication.register

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import butterknife.BindView
import butterknife.ButterKnife
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.data.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import android.content.Intent

import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import nl.gunther.bryan.newsreader.utils.SharedPreferenceHelper

import android.widget.Toast

import android.graphics.Bitmap

import android.net.Uri

import android.provider.MediaStore.MediaColumns

import android.database.Cursor
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.os.FileUtils
import butterknife.OnClick
import butterknife.OnTextChanged
import java.io.*


class RegisterDetails2Activity : AppCompatActivity(), Callback<Void> {
    @Inject
    lateinit var apiClient: ApiClient

    @BindView(R.id.register_details_error)
    lateinit var error: TextView

    @BindView(R.id.register_details_pick_image_button)
    lateinit var imageButton: ImageButton

    @BindView(R.id.register_details_edit_text_description)
    lateinit var description: EditText

    @BindView(R.id.register_details_2_button)
    lateinit var button: Button

    val PICK_IMAGE = 1


    private var selectedImage: Uri? = null
    private var filePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_details_2)
        ButterKnife.bind(this)

        (application as VitalityApplication).appComponent.inject(this)

        imageButton.setOnClickListener {
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, PICK_IMAGE)
        }
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
        val isValid = description.text.length > 10
        button.isEnabled = isValid
    }

    @OnClick(R.id.register_details_2_button)
    fun onClickStart(){
        val isValid = description.text.length > 10

        if(isValid){
            uploadImage()
        }
    }

    fun uploadImage(){
        if(imageButton.drawable !is BitmapDrawable) return

        selectedImage?.let {
            val file = File(filePath!!)
            val reqFile = RequestBody.create(MediaType.parse(contentResolver.getType(it)!!), file)
            val body = MultipartBody.Part.createFormData("profile_image", file.name, reqFile)

            val sharedPrefs = SharedPreferenceHelper(this)

            if(sharedPrefs.isLoggedIn()) {
                apiClient.updateProfileImage("Bearer " + sharedPrefs.accessToken!!, body).enqueue(object : Callback<Void> {
                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        println("failed" + t.message)
                    }

                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if(response.isSuccessful){
                            println("UPLOADED IMAGE")
                        }
                    }
                })
            }
        }
    }

    override fun onResponse(call: Call<Void>, response: Response<Void>) {
        if (response.isSuccessful) {

        } else {
            Toast.makeText(this, getString(R.string.api_error), Toast.LENGTH_LONG).show()
        }
    }

    override fun onFailure(call: Call<Void>, t: Throwable) {
        Toast.makeText(this, getString(R.string.api_error), Toast.LENGTH_LONG).show()
    }
}