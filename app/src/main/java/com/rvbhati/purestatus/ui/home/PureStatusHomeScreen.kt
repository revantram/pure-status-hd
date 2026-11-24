package com.rvbhati.purestatus.ui.home

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import com.rvbhati.purestatus.engine.OptimizationResult
import com.rvbhati.purestatus.engine.QualityProfile
import com.rvbhati.purestatus.engine.UserPreferences
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

    var userPhone by remember { mutableStateOf(UserPreferences.getWhatsAppNumber(context)) }
    var showPhoneDialog by remember { mutableStateOf(false) }
    var tempPhoneInput by remember { mutableStateOf(userPhone) }

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

    // Phone Number Dialog
    if (showPhoneDialog) {
        AlertDialog(
            onDismissRequest = { showPhoneDialog = false },
            title = {
                Text(
                    text = "WhatsApp नंबर दर्ज करें",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Column {
                    Text(
                        text = "वीडियो को बिना क्वालिटी खोए सीधे आपके WhatsApp नंबर की चैट में भेजा जाएगा, जहाँ 'HD' बटन चालू करके आप स्टेटस पर 100% फुल क्लैरिटी में फॉरवर्ड कर सकेंगे।",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = tempPhoneInput,
                        onValueChange = { tempPhoneInput = it },
                        label = { Text("10 अंकों का मोबाइल नंबर") },
                        placeholder = { Text("उदा. 9876543210") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            focusedLabelColor = EmeraldGreen
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempPhoneInput.isNotBlank()) {
                            UserPreferences.setWhatsAppNumber(context, tempPhoneInput)
                            userPhone = UserPreferences.getWhatsAppNumber(context)
                            showPhoneDialog = false
                            Toast.makeText(context, "नंबर सेव हो गया!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "कृपया मान्य नंबर दर्ज करें", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = Color.Black)
                ) {
                    Text("सेव करें", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showPhoneDialog = false }) {
                    Text("रद्द करें")
                }
            }
        )
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
                        text = "WhatsApp स्टेटस ऑप्टिमाइज़र",
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

        Spacer(modifier = Modifier.height(14.dp))

        // WhatsApp Number Fast-Setup Pill / Banner
        LiquidGlassCard(
            cornerRadius = 14.dp,
            borderGlowColor = if (userPhone.isBlank()) NeonCyan else null,
            contentPadding = 12.dp,
            isDark = isDarkTheme,
            modifier = Modifier.clickable {
                tempPhoneInput = userPhone
                showPhoneDialog = true
            }
        ) {
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(WhatsAppGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = WhatsAppGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (userPhone.isNotBlank()) "आपका WhatsApp: +$userPhone" else "अपना WhatsApp नंबर जोड़ें",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (userPhone.isNotBlank()) "1-क्लिक में HD वीडियो चैट में भेजने के लिए तैयार" else "टैप करें और नंबर दर्ज करें (HD स्टेटस चैट ट्रिक के लिए)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "बदलें",
                    tint = EmeraldGreen,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

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
                                    Toast.makeText(context, "✅ वीडियो सफलतापूर्वक तैयार!", Toast.LENGTH_SHORT).show()
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

                    // Step-by-Step HD Trick Instructions Banner
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isDarkTheme) Color(0x2200E5FF) else Color(0x1400E5FF))
                            .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "100% फुल HD स्टेटस कैसे लगाएं:",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "1️⃣ नीचे दिए गए हरे बटन 'मेरे WhatsApp पर HD में भेजें' को दबाएं।\n" +
                                    "2️⃣ WhatsApp चैट में स्क्रीन के सबसे ऊपर 'HD' बटन दबाकर 'HD Quality' चुनें और सेंड करें।\n" +
                                    "3️⃣ चैट में सेंड होने के बाद वीडियो पर लॉन्ग प्रेस करके 'Forward ➔ My Status' कर दें। बिना किसी ब्लर के 100% ओरिजिनल HD स्टेटस लग जाएगा!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            lineHeight = 19.sp,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Buttons (WhatsApp Chat 1-Click Send, Status Direct, Save, Share)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // PRIMARY BUTTON: 1-Click Send to User's WhatsApp Number
                        Button(
                            onClick = {
                                if (userPhone.isBlank()) {
                                    tempPhoneInput = ""
                                    showPhoneDialog = true
                                } else {
                                    WhatsAppShareHelper.sendVideoToSpecificNumber(
                                        context = context,
                                        file = activeClip.file,
                                        rawPhoneNumber = userPhone
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldGreen,
                                contentColor = Color.Black
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (userPhone.isNotBlank()) "मेरे WhatsApp पर HD में भेजें" else "WhatsApp नंबर डालें और भेजें",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                        }

                        // If multiple parts, send all parts to user's chat in 1 click!
                        if (optResult.clips.size > 1 && userPhone.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    WhatsAppShareHelper.shareMultipleToWhatsApp(
                                        context = context,
                                        files = optResult.clips.map { it.file },
                                        rawPhoneNumber = userPhone
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
                                    text = "सभी ${optResult.clips.size} भाग मेरे WhatsApp पर भेजें",
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreen
                                )
                            }
                        }

                        // Alternative Direct Share to Status
                        OutlinedButton(
                            onClick = {
                                WhatsAppShareHelper.shareToWhatsApp(context, activeClip.file, preferStatus = true)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "सीधे WhatsApp स्टेटस पर शेयर करें (Direct Share)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        // Save to Gallery & Generic Share
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        val saved = WhatsAppShareHelper.saveToGallery(context, activeClip.file)
                                        if (saved) {
                                            Toast.makeText(context, "गैलरी (Movies/PureStatus) में सेव हो गया!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "सेव करने में विफल", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "गैलरी में सेव",
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
