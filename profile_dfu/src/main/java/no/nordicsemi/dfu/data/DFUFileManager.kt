package no.nordicsemi.dfu.data

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toFile
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DFUFileManager @Inject constructor(
    @ApplicationContext
    private val context: Context
) {

    private val TAG = "DFU_FILE_MANAGER"

    fun createFile(uri: Uri): ZipFile? {
        return try {
            createFromFile(uri)
        } catch (e: Exception) {
            Log.e(TAG, "Error during creation file from uri.", e)
            try {
                createFromContentResolver(uri)
            } catch (e: Exception) {
                Log.e(TAG, "Error during loading file from content resolver.", e)
                null
            }
        }
    }

    private fun createFromFile(uri: Uri): ZipFile {
        val file = uri.toFile()
        return ZipFile(uri, file.name, file.path, file.length())
    }

    private fun createFromContentResolver(uri: Uri): ZipFile? {
        val data = context.contentResolver.query(uri, null, null, null, null)

        return if (data != null && data.moveToNext()) {

            val displayNameIndex = data.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
            val fileSizeIndex = data.getColumnIndex(MediaStore.MediaColumns.SIZE)
            val dataIndex = data.getColumnIndex(MediaStore.MediaColumns.DATA)

            val fileName = data.getString(displayNameIndex)
            val fileSize = data.getInt(fileSizeIndex)
            val filePath = if (dataIndex != -1) {
                data.getString(dataIndex)
            } else {
                null
            }

            data.close()

            ZipFile(uri, fileName, filePath, fileSize.toLong())
        } else {
            Log.d(TAG, "Data loaded from ContentResolver is empty.")
            null
        }
    }
}
