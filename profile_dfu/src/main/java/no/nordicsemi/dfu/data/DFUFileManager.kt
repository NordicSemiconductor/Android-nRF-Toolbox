package no.nordicsemi.dfu.data

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.core.net.toFile
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DFUFileManager @Inject constructor(
    @ApplicationContext
    private val context: Context
) {

    fun createFile(uri: Uri): FileData? {
        return try {
            createFromFile(uri)
        } catch (e: Exception) {
            try {
                createFromContentResolver(uri)
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun createFromFile(uri: Uri): FileData {
        val file = uri.toFile()
        return FileData(uri, file.name, file.path, file.length())
    }

    private fun createFromContentResolver(uri: Uri): FileData? {
        return try {
            val data = context.contentResolver.query(uri, null, null, null, null)

            if (data != null && data.moveToNext()) {

                val displayNameIndex = data.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                val fileSizeIndex = data.getColumnIndex(MediaStore.MediaColumns.SIZE)
                val dataIndex = data.getColumnIndex(MediaStore.MediaColumns.DATA)

                val fileName = data.getString(displayNameIndex)
                val fileSize = data.getInt(fileSizeIndex)
                val filePath = data.getString(dataIndex)

                data.close()

                FileData(uri, fileName, filePath, fileSize.toLong())
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
