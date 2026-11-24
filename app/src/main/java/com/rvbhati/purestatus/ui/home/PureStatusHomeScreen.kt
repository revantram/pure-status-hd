package com.rvbhati.purestatus.ui.home

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import com.rvbhati.purestatus.engine.OptimizationResult
import com.rvbhati.purestatus.engine.QualityProfile
import com.rvbhati.purestatus.engine.VideoInfo
import com.rvbhati.purestatus.engine.VideoMetadataExtractor
import com.rvbhati.purestatus.engine.VideoOptimizerEngine
import com.rvbhati.purestatus.engine.WhatsAppShareHelper
import com.rvbhati.purestatus.ui.components.LiquidGlassCard
import com.rvbhati.purestatus.ui.components.VideoPlayerPreview
import com.rvbhati.purestatus.ui.theme.EmeraldGreen
import com.rvbhati.purestatus.ui.theme.NeonCyan
import com.rvbhati.purestatus.ui.theme.WhatsAppGreen
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@Composable
fun PureStatusHomeScreen(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onOpenSettings: () -> Unit,
    initialSharedUri: Uri? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var selectedVideoInfo by remember { mutableStateOf<VideoInfo?>(null) }
    var isLoadingInfo by remember { mutableStateOf(false) }

    var selectedProfile by remember { mutableStateOf(QualityProfile.ULTRA_1080P) }
    var autoSplit30s by remember { mutableStateOf(true) }

    var isProcessing by remember { mutableStateOf(false) }
    var currentProgress by remember { mutableFloatStateOf(0f) }
    var progressStatusText by remember { mutableStateOf("") }
    var currentPartNumber by remember { mutableIntStateOf(1) }
    var totalPartsCount by remember { mutableIntStateOf(1) }

    var result by remember { mutableStateOf<OptimizationResult?>(null) }
    var selectedClipIndex by remember { mutableIntStateOf(0) }

    // Video picker launcher
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                isLoadingInfo = true
                result = null
                selectedVideoInfo = VideoMetadataExtractor.extract(context, uri)
                isLoadingInfo = false
            }
        }
    }

    // Process initial shared intent URI if any
    LaunchedEffect(initialSharedUri) {
        if (initialSharedUri != null && selectedVideoInfo == null) {
            isLoadingInfo = true
            selectedVideoInfo = VideoMetadataExtractor.extract(context, initialSharedUri)
            isLoadingInfo = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(18.dp))

        // Top App Bar (Title + 100% Ad-Free Pill + Theme Toggle + Settings)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "PureStatus",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "HD",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black
                        ),
                        color = EmeraldGreen
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(EmeraldGreen.copy(alpha = 0.15f))
                            .border(1.dp, EmeraldGreen.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "100% AD-FREE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = EmeraldGreen
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "WhatsApp स्टेटस HD ऑप्टिमाइज़र",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Top Right Action Buttons (Theme Toggle + Settings)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Theme Toggle
                Surface(
                    onClick = onToggleTheme,
                    shape = CircleShape,
                    color = if (isDarkTheme) Color(0x33FFFFFF) else Color(0x1A000000),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isDarkTheme) Color(0x33FFFFFF) else Color(0x1F000000)
                    ),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "थीम बदलें",
                            tint = if (isDarkTheme) Color(0xFFFFD54F) else Color(0xFF0F172A),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Settings Button
                Surface(
                    onClick = onOpenSettings,
                    shape = CircleShape,
                    color = if (isDarkTheme) Color(0x33FFFFFF) else Color(0x1A000000),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isDarkTheme) Color(0x33FFFFFF) else Color(0x1F000000)
                    ),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "सेटिंग्स",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Hero Video Selection Zone
        if (selectedVideoInfo == null) {
            LiquidGlassCard(
                borderGlowColor = if (isDarkTheme) EmeraldGreen.copy(alpha = 0.4f) else null,
                isDark = isDarkTheme,
                modifier = Modifier
                    .clickable { videoPickerLauncher.launch("video/*") }
                    .animateContentSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        EmeraldGreen.copy(alpha = 0.3f),
                                        EmeraldGreen.copy(alpha = 0.05f)
                                    )
                                )
                            )
                            .border(1.5.dp, EmeraldGreen.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "वीडियो चुनें या डालें",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "WhatsApp स्टेटस के लिए बिना क्वालिटी खोए साइज घटाएं\nफुल HD स्पष्टता और 30s ऑटो स्प्लिट",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = { videoPickerLauncher.launch("video/*") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldGreen,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "गैलरी से वीडियो चुनें",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            // Selected Video Details Card
            val video = selectedVideoInfo!!
            LiquidGlassCard(
                borderGlowColor = EmeraldGreen.copy(alpha = 0.5f),
                isDark = isDarkTheme
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(EmeraldGreen.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Videocam,
                                    contentDescription = null,
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = video.fileName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${video.displayResolution} • ${video.durationFormatted} • ${video.sizeFormatted}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = { videoPickerLauncher.launch("video/*") }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "बदलें",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Preset Selector
                    Text(
                        text = "क्वालिटी प्रोफाइल चुनें:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    QualityProfile.values().forEach { profile ->
                        val isSelected = selectedProfile == profile
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) {
                                        EmeraldGreen.copy(alpha = 0.15f)
                                    } else {
                                        if (isDarkTheme) Color(0x1AFFFFFF) else Color(0x0A000000)
                                    }
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.8.dp,
                                    color = if (isSelected) EmeraldGreen else Color(0x22FFFFFF),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedProfile = profile }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = profile.title,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                                        ),
                                        color = if (isSelected) EmeraldGreen else MaterialTheme.colorScheme.onBackground
                                    )
                                    if (profile == QualityProfile.ULTRA_1080P) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(EmeraldGreen)
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "BEST HD",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color.Black
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = profile.subtitle,
                                    style = MaterialTheme.typography.bodySmall ?: MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 30-Second Status Splitting Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDarkTheme) Color(0x1AFFFFFF) else Color(0x0A000000))
                            .border(0.8.dp, Color(0x22FFFFFF), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCut,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "30s स्टेटस स्प्लिटर (Auto-Split)",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = if (video.durationMs > 32000L) {
                                        "30-30 सेकंड के निर्बाध पार्ट में तैयार होगा"
                                    } else {
                                        "वीडियो 30s से छोटा है (स्प्लिट की जरूरत नहीं)"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = autoSplit30s,
                            onCheckedChange = { autoSplit30s = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = EmeraldGreen,
                                checkedTrackColor = EmeraldGreen.copy(alpha = 0.3f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Main Action Button (Optimize Video)
                    Button(
                        onClick = {
                            scope.launch {
                                isProcessing = true
                                currentProgress = 0f
                                progressStatusText = "हार्डवेयर मीडिया एनकोडर शुरू हो रहा है..."
                                try {
                                    val optResult = VideoOptimizerEngine.optimize(
                                        context = context,
                                        videoInfo = video,
                                        profile = selectedProfile,
                                        autoSplit30s = autoSplit30s,
                                        onProgress = { prog, currentPart, totalParts, status ->
                                            currentProgress = prog
                                            currentPartNumber = currentPart
                                            totalPartsCount = totalParts
                                            progressStatusText = status
                                        }
                                    )
                                    result = optResult
                                    selectedClipIndex = 0
                                    Toast.makeText(context, "✅ HD वीडियो तैयार और गैलरी में सेव हो गया!", Toast.LENGTH_LONG).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "त्रुटि: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                } finally {
                                    isProcessing = false
                                }
                            }
                        },
                        enabled = !isProcessing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldGreen,
                            contentColor = Color.Black
                        )
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.Black,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "प्रोसेस हो रहा है...",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "HD ऑप्टिमाइज़ करें",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // Processing Progress Indicator
        AnimatedVisibility(
            visible = isProcessing,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                LiquidGlassCard(
                    borderGlowColor = EmeraldGreen,
                    isDark = isDarkTheme
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = progressStatusText,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "${(currentProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = EmeraldGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { currentProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = EmeraldGreen,
                            trackColor = if (isDarkTheme) Color(0x33FFFFFF) else Color(0x1F000000)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "⚡ Hardware MediaCodec द्वारा बिजली जैसी रफ्तार",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Optimization Result Card
        if (result != null && !isProcessing) {
            val optResult = result!!
            Spacer(modifier = Modifier.height(20.dp))

            LiquidGlassCard(
                borderGlowColor = EmeraldGreen,
                isDark = isDarkTheme
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Success Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = EmeraldGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "HD वीडियो तैयार!",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        if (optResult.savingsPercent > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(EmeraldGreen)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "-${optResult.savingsPercent}% बचत",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    color = Color.Black
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Size Comparison Pill
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDarkTheme) Color(0x2600E676) else Color(0x1400C853))
                            .border(1.dp, EmeraldGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "मूल साइज",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = optResult.originalSizeFormatted,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Text(
                            text = "➔",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = EmeraldGreen
                        )

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "ऑप्टिमाइज़्ड HD",
                                fontSize = 11.sp,
                                color = EmeraldGreen
                            )
                            Text(
                                text = optResult.compressedSizeFormatted,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = EmeraldGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Auto-Saved to Gallery Confirmation Pill
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDarkTheme) Color(0x2600E676) else Color(0x1A00E676))
                            .border(1.dp, EmeraldGreen.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "✅ वीडियो आपकी फोन गैलरी (DCIM/PureStatus) में सुरक्षित सेव हो गया है!",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // If multiple split parts, show parts selector tabs
                    if (optResult.clips.size > 1) {
                        Text(
                            text = "कुल ${optResult.clips.size} भाग (30s स्प्लिट):",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            itemsIndexed(optResult.clips) { index, clip ->
                                val isSelected = selectedClipIndex == index
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isSelected) EmeraldGreen else if (isDarkTheme) Color(0x22FFFFFF) else Color(0x11000000)
                                        )
                                        .clickable { selectedClipIndex = index }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "${clip.partTitle} (${clip.sizeFormatted})",
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Video Preview Player of selected clip
                    val activeClip = optResult.clips.getOrNull(selectedClipIndex) ?: optResult.clips.first()
                    VideoPlayerPreview(
                        videoFile = activeClip.file,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons (WhatsApp Direct Status, Gallery, Share)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // 1. Direct Share to WhatsApp Status
                        Button(
                            onClick = {
                                WhatsAppShareHelper.shareToWhatsApp(context, activeClip.file, preferStatus = true)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WhatsAppGreen,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (optResult.clips.size > 1) {
                                    "${activeClip.partTitle} को WhatsApp स्टेटस पर लगाएं"
                                } else {
                                    "WhatsApp स्टेटस पर लगाएं (Direct Share)"
                                },
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                        }

                        // If multiple parts, offer "Share All Parts to WhatsApp" in 1 click!
                        if (optResult.clips.size > 1) {
                            OutlinedButton(
                                onClick = {
                                    WhatsAppShareHelper.shareMultipleToWhatsApp(
                                        context,
                                        optResult.clips.map { it.file }
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, EmeraldGreen)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCut,
                                    contentDescription = null,
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "सभी ${optResult.clips.size} भाग एक साथ WhatsApp पर भेजें",
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreen
                                )
                            }
                        }

                        // Open Gallery & Generic Share
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(
                                                activeClip.galleryUri ?: Uri.fromFile(activeClip.file),
                                                "video/*"
                                            )
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        Toast.makeText(context, "गैलरी (DCIM/PureStatus) में उपलब्ध है", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "गैलरी में देखें",
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    WhatsAppShareHelper.shareGeneric(context, activeClip.file)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "अन्य ऐप्स",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
