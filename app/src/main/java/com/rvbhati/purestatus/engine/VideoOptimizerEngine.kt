package com.rvbhati.purestatus.engine

import android.content.Context
import android.net.Uri
import androidx.media3.common.Effect
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Contrast
import androidx.media3.effect.Presentation
import androidx.media3.transformer.Composition
import androidx.media3.transformer.DefaultEncoderFactory
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.TransformationRequest
import androidx.media3.transformer.Transformer
import androidx.media3.transformer.VideoEncoderSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

enum class QualityProfile(
    val title: String,
    val subtitle: String,
    val targetLandscapeHeight: Int,
    val targetPortraitHeight: Int,
    val targetBitrateKbps: Int
) {
    ULTRA_1080P(
        title = "Ultra Full HD (1080p)",
        subtitle = "1080x1920 @ 6 Mbps • WhatsApp HD चैट ट्रिक (100% फुल क्लैरिटी)",
        targetLandscapeHeight = 1080,
        targetPortraitHeight = 1920,
        targetBitrateKbps = 6000
    ),
    WHATSAPP_HD(
        title = "WhatsApp Standard HD (720p)",
        subtitle = "720x1280 @ 2.4 Mbps • डायरेक्ट स्टेटस अपलोड के लिए",
        targetLandscapeHeight = 720,
        targetPortraitHeight = 1280,
        targetBitrateKbps = 2400
    ),
    COMPACT_540P(
        title = "कम डेटा सेवर (540p)",
        subtitle = "540x960 @ 1.2 Mbps • धीमे इंटरनेट के लिए सुपर-फ़ास्ट",
        targetLandscapeHeight = 540,
        targetPortraitHeight = 960,
        targetBitrateKbps = 1200
    )
}

data class ProcessedClip(
    val file: File,
    val partIndex: Int,
    val totalParts: Int,
    val durationMs: Long,
    val sizeBytes: Long
) {
    val sizeFormatted: String
        get() {
            val mb = sizeBytes.toDouble() / (1024 * 1024)
            return if (mb >= 1.0) String.format("%.1f MB", mb) else String.format("%.1f KB", sizeBytes.toDouble() / 1024)
        }

    val partTitle: String
        get() = if (totalParts > 1) "भाग $partIndex/$totalParts" else "HD स्टेटस वीडियो"
}

data class OptimizationResult(
    val clips: List<ProcessedClip>,
    val originalSizeBytes: Long,
    val totalCompressedSizeBytes: Long,
    val totalDurationMs: Long,
    val profile: QualityProfile
) {
    val savingsPercent: Int
        get() = if (originalSizeBytes > 0 && totalCompressedSizeBytes < originalSizeBytes) {
            (((originalSizeBytes - totalCompressedSizeBytes).toDouble() / originalSizeBytes) * 100).toInt()
        } else 0

    val originalSizeFormatted: String
        get() {
            val mb = originalSizeBytes.toDouble() / (1024 * 1024)
            return if (mb >= 1.0) String.format("%.1f MB", mb) else String.format("%.1f KB", originalSizeBytes.toDouble() / 1024)
        }

    val compressedSizeFormatted: String
        get() {
            val mb = totalCompressedSizeBytes.toDouble() / (1024 * 1024)
            return if (mb >= 1.0) String.format("%.1f MB", mb) else String.format("%.1f KB", totalCompressedSizeBytes.toDouble() / 1024)
        }
}

@UnstableApi
object VideoOptimizerEngine {

    suspend fun optimize(
        context: Context,
        videoInfo: VideoInfo,
        profile: QualityProfile = QualityProfile.WHATSAPP_HD,
        autoSplit30s: Boolean = true,
        splitDurationMs: Long = 30_000L,
        onProgress: (overallProgress: Float, currentPart: Int, totalParts: Int, statusText: String) -> Unit
    ): OptimizationResult = withContext(Dispatchers.Main) {
        val totalDuration = videoInfo.durationMs
        val outputDir = File(context.cacheDir, "optimized_status").apply { mkdirs() }

        // Clean previous runs
        outputDir.listFiles()?.forEach { it.delete() }

        val parts: List<Pair<Long, Long>> = if (autoSplit30s && totalDuration > splitDurationMs + 2000L) {
            val segments = mutableListOf<Pair<Long, Long>>()
            var start = 0L
            while (start < totalDuration) {
                val end = minOf(start + splitDurationMs, totalDuration)
                segments.add(start to end)
                start = end
            }
            segments
        } else {
            listOf(0L to totalDuration)
        }

        val totalParts = parts.size
        val processedClips = mutableListOf<ProcessedClip>()

        val targetHeight = if (videoInfo.isPortrait) {
            profile.targetPortraitHeight
        } else {
            profile.targetLandscapeHeight
        }

        for ((index, segment) in parts.withIndex()) {
            val partIndex = index + 1
            val (startMs, endMs) = segment
            val segmentDuration = endMs - startMs

            val outputFile = File(outputDir, "PureStatus_${System.currentTimeMillis()}_part${partIndex}.mp4")

            val baseProgress = index.toFloat() / totalParts.toFloat()
            val partWeight = 1f / totalParts.toFloat()

            transcodeSegment(
                context = context,
                sourceUri = videoInfo.uri,
                startMs = startMs,
                endMs = endMs,
                targetHeight = targetHeight,
                targetBitrateKbps = profile.targetBitrateKbps,
                outputFile = outputFile,
                onPartProgress = { partProg ->
                    val totalProg = baseProgress + (partProg * partWeight)
                    val statusText = if (totalParts > 1) {
                        "भाग $partIndex/$totalParts प्रोसेस हो रहा है (${(partProg * 100).toInt()}%)"
                    } else {
                        "HD वीडियो ऑप्टिमाइज़ हो रहा है (${(partProg * 100).toInt()}%)"
                    }
                    onProgress(totalProg.coerceIn(0f, 0.99f), partIndex, totalParts, statusText)
                }
            )

            val clip = ProcessedClip(
                file = outputFile,
                partIndex = partIndex,
                totalParts = totalParts,
                durationMs = segmentDuration,
                sizeBytes = outputFile.length()
            )
            processedClips.add(clip)
        }

        val totalCompressedSize = processedClips.sumOf { it.sizeBytes }

        onProgress(1.0f, totalParts, totalParts, "ऑप्टिमाइज़ेशन पूर्ण! WhatsApp HD तैयार है")

        OptimizationResult(
            clips = processedClips,
            originalSizeBytes = videoInfo.sizeBytes,
            totalCompressedSizeBytes = totalCompressedSize,
            totalDurationMs = totalDuration,
            profile = profile
        )
    }

    private suspend fun transcodeSegment(
        context: Context,
        sourceUri: Uri,
        startMs: Long,
        endMs: Long,
        targetHeight: Int,
        targetBitrateKbps: Int,
        outputFile: File,
        onPartProgress: (Float) -> Unit
    ) = suspendCancellableCoroutine<Unit> { continuation ->
        try {
            val clippingConfig = MediaItem.ClippingConfiguration.Builder()
                .setStartPositionMs(startMs)
                .apply {
                    if (endMs > 0) {
                        setEndPositionMs(endMs)
                    }
                }
                .build()

            val mediaItem = MediaItem.Builder()
                .setUri(sourceUri)
                .setClippingConfiguration(clippingConfig)
                .build()

            // Scale video height while preserving aspect ratio perfectly
            val presentation = Presentation.createForHeight(targetHeight)
            val effectList = mutableListOf<Effect>()
            effectList.add(presentation)

            // Pure Status Edge Clarity / Contrast Pre-Enhancement (+10% contrast)
            try {
                effectList.add(Contrast(0.10f))
            } catch (_: Throwable) {}

            val effects = Effects(emptyList(), effectList)

            val editedMediaItem = EditedMediaItem.Builder(mediaItem)
                .setEffects(effects)
                .build()

            val transformationRequest = TransformationRequest.Builder()
                .setVideoMimeType(MimeTypes.VIDEO_H264)
                .setAudioMimeType(MimeTypes.AUDIO_AAC)
                .build()

            // Configure precise video encoder bitrate
            val encoderSettings = VideoEncoderSettings.Builder()
                .setBitrate(targetBitrateKbps * 1000)
                .build()

            val encoderFactory = DefaultEncoderFactory.Builder(context)
                .setRequestedVideoEncoderSettings(encoderSettings)
                .setEnableFallback(true)
                .build()

            var transformerInstance: Transformer? = null

            val listener = object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    onPartProgress(1.0f)
                    if (continuation.isActive) {
                        continuation.resume(Unit)
                    }
                }

                override fun onError(
                    composition: Composition,
                    exportResult: ExportResult,
                    exportException: ExportException
                ) {
                    if (continuation.isActive) {
                        continuation.resumeWithException(exportException)
                    }
                }
            }

            val transformer = Transformer.Builder(context)
                .setEncoderFactory(encoderFactory)
                .setTransformationRequest(transformationRequest)
                .addListener(listener)
                .build()

            transformerInstance = transformer

            transformer.start(editedMediaItem, outputFile.absolutePath)

            // Progress polling coroutine
            val progressHolder = ProgressHolder()
            val pollingScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
            val progressJob = pollingScope.launch {
                while (continuation.isActive) {
                    delay(120)
                    if (!continuation.isActive) break
                    val state = transformerInstance.getProgress(progressHolder)
                    if (state == Transformer.PROGRESS_STATE_AVAILABLE) {
                        val p = progressHolder.progress / 100f
                        onPartProgress(p.coerceIn(0f, 0.99f))
                    }
                }
            }

            continuation.invokeOnCancellation {
                progressJob.cancel()
                try {
                    transformerInstance.cancel()
                } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            if (continuation.isActive) {
                continuation.resumeWithException(e)
            }
        }
    }
}
