package com.rvbhati.purestatus.engine

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val hasUpdate: Boolean,
    val latestVersion: String,
    val downloadUrl: String?,
    val releaseNotes: String?
)

object InAppUpdateEngine {

    const val CURRENT_VERSION = "v1.0.0"
    private const val GITHUB_RELEASE_API = "https://api.github.com/repos/revantram/pure-status-hd/releases/latest"

    suspend fun checkLatestRelease(): UpdateInfo = withContext(Dispatchers.IO) {
        try {
            val url = URL(GITHUB_RELEASE_API)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "PureStatusHDApp")
            }

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                val tagName = json.optString("tag_name", "v1.0.0")
                val notes = json.optString("body", "बग फिक्स व नए स्टेटस ऑप्टिमाइज़ेशन फीचर्स")

                var apkDownloadUrl: String? = null
                val assets = json.optJSONArray("assets")
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val name = asset.optString("name", "")
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            apkDownloadUrl = asset.optString("browser_download_url")
                            break
                        }
                    }
                }

                if (apkDownloadUrl == null) {
                    apkDownloadUrl = "https://github.com/revantram/pure-status-hd/releases/latest/download/PureStatusHD-v1.0.apk"
                }

                UpdateInfo(
                    hasUpdate = true,
                    latestVersion = tagName,
                    downloadUrl = apkDownloadUrl,
                    releaseNotes = notes
                )
            } else {
                UpdateInfo(
                    hasUpdate = true,
                    latestVersion = CURRENT_VERSION,
                    downloadUrl = "https://github.com/revantram/pure-status-hd/releases/latest/download/PureStatusHD-v1.0.apk",
                    releaseNotes = "नवीनतम रिलीज़ उपलब्ध है"
                )
            }
        } catch (e: Exception) {
            UpdateInfo(
                hasUpdate = true,
                latestVersion = CURRENT_VERSION,
                downloadUrl = "https://github.com/revantram/pure-status-hd/releases/latest/download/PureStatusHD-v1.0.apk",
                releaseNotes = "ऑफलाइन/नेटवर्क एरर - डायरेक्ट डाउनलोड उपलब्ध"
            )
        }
    }

    suspend fun downloadApkFile(
        context: Context,
        downloadUrl: String,
        onProgress: (Float) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        try {
            val updateDir = File(context.cacheDir, "apk_updates").apply { mkdirs() }
            val apkFile = File(updateDir, "PureStatusHD_Update.apk")
            if (apkFile.exists()) {
                apkFile.delete()
            }

            var currentUrl = downloadUrl
            var connection: HttpURLConnection
            var redirectCount = 0

            while (true) {
                val url = URL(currentUrl)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    instanceFollowRedirects = false
                    connectTimeout = 15000
                    readTimeout = 15000
                    setRequestProperty("User-Agent", "PureStatusHDApp")
                }

                val status = connection.responseCode
                if (status == HttpURLConnection.HTTP_MOVED_TEMP ||
                    status == HttpURLConnection.HTTP_MOVED_PERM ||
                    status == 307 || status == 308
                ) {
                    currentUrl = connection.getHeaderField("Location")
                    redirectCount++
                    if (redirectCount > 5) break
                } else {
                    break
                }
            }

            val fileLength = connection.contentLength
            val input: InputStream = connection.inputStream
            val output = FileOutputStream(apkFile)

            val data = ByteArray(8192)
            var total: Long = 0
            var count: Int

            while (input.read(data).also { count = it } != -1) {
                total += count
                if (fileLength > 0) {
                    onProgress(total.toFloat() / fileLength.toFloat())
                }
                output.write(data, 0, count)
            }

            output.flush()
            output.close()
            input.close()

            if (apkFile.exists() && apkFile.length() > 0) {
                apkFile
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun installApk(context: Context, apkFile: File) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val apkUri = FileProvider.getUriForFile(context, authority, apkFile)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "इंस्टॉल करने में त्रुटि: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    fun shareAppApk(context: Context) {
        try {
            val appInfo = context.applicationInfo
            val originalApk = File(appInfo.sourceDir)
            val shareDir = File(context.cacheDir, "shared_apk").apply { mkdirs() }
            val shareApk = File(shareDir, "PureStatusHD_v1.0.apk")

            if (!shareApk.exists() || shareApk.length() != originalApk.length()) {
                originalApk.copyTo(shareApk, overwrite = true)
            }

            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, shareApk)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.android.package-archive"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(
                    Intent.EXTRA_TEXT,
                    "PureStatus HD — WhatsApp Status HD Video Compressor & Splitter (100% Ad-Free)\n" +
                            "डेवलपर: रेवंतराम भाटी (+91 8696663615)\n" +
                            "डाउनलोड करें: https://github.com/revantram/pure-status-hd/releases/latest"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "PureStatus HD ऐप शेयर करें"))
        } catch (e: Exception) {
            Toast.makeText(context, "APK शेयर करने में त्रुटि: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
