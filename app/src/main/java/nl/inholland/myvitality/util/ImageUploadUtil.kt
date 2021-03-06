package nl.inholland.myvitality.util

import android.content.Context
import android.graphics.Bitmap
import java.io.*

object ImageUploadUtil {

    /**
     * Convert a bitmap into a file
     * @param context the context of the class it's called from
     * @param fileName  the name of the original file
     * @param bitmap the bitmap to convert
     *
     * @return The converte file
     */
    fun convertBitmapToFile(context: Context, fileName: String, bitmap: Bitmap): File {
        //create a file to write bitmap data
        val file = File(context.cacheDir, fileName)
        file.createNewFile()

        //Convert bitmap to byte array
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100 /*ignored for PNG*/, bos)
        val bitMapData = bos.toByteArray()

        //write the bytes in file
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(file)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        try {
            fos?.write(bitMapData)
            fos?.flush()
            fos?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file
    }
}