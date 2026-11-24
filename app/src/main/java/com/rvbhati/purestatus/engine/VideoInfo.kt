package com.rvbhati.purestatus.engine

import android.net.Uri

data class VideoInfo(
    val uri: Uri,
    val fileName: String,
    val durationMs: Long,
    val width: Int,
    val height: Int,
    val rotation: Int,
    val sizeBytes: Long,
    val bitrate: Long = 0,
    val mimeType: String = "video/mp4"
) {
    val durationFormatted: String
        get() {
            val totalSeconds = (durationMs / 1000).toInt()
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }

    val sizeFormatted: String
        get() {
            val mb = sizeBytes.toDouble() / (1024 * 1024)
            return if (mb >= 1.0) {
                String.format("%.1f MB", mb)
            } else {
                val kb = sizeBytes.toDouble() / 1024
                String.format("%.1f KB", kb)
            }
        }

    val displayResolution: String
        get() {
            val isRotated = rotation == 90 || rotation == 270
            val effWidth = if (isRotated) height else width
            val effHeight = if (isRotated) width else height
            return "${effWidth}x${effHeight}"
        }

    val isPortrait: Boolean
        get() {
            val isRotated = rotation == 90 || rotation == 270
            val effWidth = if (isRotated) height else width
            val effHeight = if (isRotated) width else height
            return effHeight > effWidth
        }
}
