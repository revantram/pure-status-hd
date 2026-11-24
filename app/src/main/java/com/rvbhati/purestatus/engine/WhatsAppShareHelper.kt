package com.rvbhati.purestatus.engine

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object WhatsAppShareHelper {

    private const val WHATSAPP_PKG = "com.whatsapp"
    private const val WHATSAPP_BIZ_PKG = "com.whatsapp.w4b"

    fun sendVideoToSpecificNumber(context: Context, file: File, rawPhoneNumber: String) {
        try {
            val cleanNumber = rawPhoneNumber.replace("+", "").replace(" ", "").replace("-", "").trim()
            val formattedNumber = if (cleanNumber.length == 10) "91$cleanNumber" else cleanNumber

            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)

            val jid = "$formattedNumber@s.whatsapp.net"
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "video/mp4"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra("jid", jid)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val pm = context.packageManager
            if (isPackageInstalled(pm, WHATSAPP_PKG)) {
                intent.setPackage(WHATSAPP_PKG)
                context.startActivity(intent)
            } else if (isPackageInstalled(pm, WHATSAPP_BIZ_PKG)) {
                intent.setPackage(WHATSAPP_BIZ_PKG)
                context.startActivity(intent)
            } else {
                context.startActivity(Intent.createChooser(intent, "WhatsApp पर भेजें"))
            }
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp खोलने में त्रुटि: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareToWhatsApp(context: Context, file: File, preferStatus: Boolean = true) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "video/mp4"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val pm = context.packageManager
            val waInstalled = isPackageInstalled(pm, WHATSAPP_PKG)
            val waBizInstalled = isPackageInstalled(pm, WHATSAPP_BIZ_PKG)

            if (waInstalled) {
                intent.setPackage(WHATSAPP_PKG)
                context.startActivity(intent)
            } else if (waBizInstalled) {
                intent.setPackage(WHATSAPP_BIZ_PKG)
                context.startActivity(intent)
            } else {
                context.startActivity(Intent.createChooser(intent, "WhatsApp स्टेटस पर शेयर करें"))
            }
        } catch (e: Exception) {
            Toast.makeText(context, "शेयर करने में त्रुटि: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareMultipleToWhatsApp(context: Context, files: List<File>, rawPhoneNumber: String? = null) {
        if (files.isEmpty()) return
        if (files.size == 1) {
            if (!rawPhoneNumber.isNullOrBlank()) {
                sendVideoToSpecificNumber(context, files.first(), rawPhoneNumber)
            } else {
                shareToWhatsApp(context, files.first())
            }
            return
        }

        try {
            val authority = "${context.packageName}.fileprovider"
            val uris = ArrayList<Uri>()
            for (file in files) {
                uris.add(FileProvider.getUriForFile(context, authority, file))
            }

            val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "video/mp4"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (!rawPhoneNumber.isNullOrBlank()) {
                val cleanNumber = rawPhoneNumber.replace("+", "").replace(" ", "").replace("-", "").trim()
                val formattedNumber = if (cleanNumber.length == 10) "91$cleanNumber" else cleanNumber
                intent.putExtra("jid", "$formattedNumber@s.whatsapp.net")
            }

            val pm = context.packageManager
            val waInstalled = isPackageInstalled(pm, WHATSAPP_PKG)
            val waBizInstalled = isPackageInstalled(pm, WHATSAPP_BIZ_PKG)

            if (waInstalled) {
                intent.setPackage(WHATSAPP_PKG)
                context.startActivity(intent)
            } else if (waBizInstalled) {
                intent.setPackage(WHATSAPP_BIZ_PKG)
                context.startActivity(intent)
            } else {
                context.startActivity(Intent.createChooser(intent, "सभी भाग WhatsApp पर भेजें"))
            }
        } catch (e: Exception) {
            Toast.makeText(context, "शेयर त्रुटि: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareGeneric(context: Context, file: File) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "video/mp4"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, "PureStatus HD द्वारा ऑप्टिमाइज़ किया गया वीडियो")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "वीडियो शेयर करें"))
        } catch (e: Exception) {
            Toast.makeText(context, "त्रुटि: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun saveToGallery(context: Context, file: File): Boolean = withContext(Dispatchers.IO) {
        try {
            val fileName = "PureStatus_${System.currentTimeMillis()}.mp4"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                    put(MediaStore.Video.Media.RELATIVE_PATH, "${Environment.DIRECTORY_MOVIES}/PureStatus")
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                }

                val collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val itemUri = context.contentResolver.insert(collection, values) ?: return@withContext false

                context.contentResolver.openOutputStream(itemUri)?.use { outStream ->
                    FileInputStream(file).use { inStream ->
                        inStream.copyTo(outStream)
                    }
                }

                values.clear()
                values.put(MediaStore.Video.Media.IS_PENDING, 0)
                context.contentResolver.update(itemUri, values, null, null)
                true
            } else {
                val moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
                val targetDir = File(moviesDir, "PureStatus").apply { mkdirs() }
                val targetFile = File(targetDir, fileName)

                FileInputStream(file).use { inStream ->
                    FileOutputStream(targetFile).use { outStream ->
                        inStream.copyTo(outStream)
                    }
                }

                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(targetFile.absolutePath),
                    arrayOf("video/mp4"),
                    null
                )
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun isPackageInstalled(pm: android.content.pm.PackageManager, packageName: String): Boolean {
        return try {
            pm.getPackageInfo(packageName, 0)
            true
        } catch (_: Exception) {
            false
        }
    }
}
