package com.cet.secure360

import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cet.secure360.localdatabase.RetrofitClient
import com.cet.secure360.model.IncidentRecord
import com.cet.secure360.viewmodel.RecordingViewModel
import kotlinx.coroutines.delay

// ─── Color Palette ───────────────────────────────────────────────────────────

private val BackgroundDark   = Color(0xFF1A1D22)
private val SurfaceDark      = Color(0xFF22262E)
private val SurfaceVariant   = Color(0xFF2A2F39)
private val AccentTeal       = Color(0xFF00C9A7)
private val AccentRed        = Color(0xFFE53935)
private val TextPrimary      = Color(0xFFECEFF4)
private val TextSecondary    = Color(0xFF8D93A1)
private val SidebarBg        = Color(0xFF13161B)
private val DividerColor     = Color(0xFF2E333D)

// ─── Constants for Event Types ───────────────────────────────────────────────

private const val FACE_DETECTION = 2
private const val HONK_EVENT = 3
private const val HARD_BRAKING = 4
private const val ALARM = 5

// ─── Data Models ─────────────────────────────────────────────────────────────

enum class DashcamNavItem { Home, Car, Video, Cloud, Profile }

enum class ClipTab { All, Parked, Driving, Shared }

// ─── Main Screen ─────────────────────────────────────────────────────────────

@Composable
fun DashcamScreen(
    onPlayVideo: (String) -> Unit = {}
) {
    val viewModel: RecordingViewModel = viewModel(
        factory = RecordingViewModel.Factory(RetrofitClient.apiService)
    )

    var currentNavItem by remember { mutableStateOf(DashcamNavItem.Home) }
    var selectedTab by remember { mutableStateOf(ClipTab.All) }
    val currentStatus by viewModel.recordingStatus.collectAsState()
    val allIncidents by viewModel.incidents.collectAsState()
    val eventStatuses by viewModel.eventStatuses.collectAsState()
    
    val isRecording = currentStatus == 1
    val context = LocalContext.current

    // Hoisted timer state
    var recordingSeconds by remember { mutableStateOf(30) }
    val maxSeconds = 60

    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (recordingSeconds < maxSeconds) {
                delay(1000)
                recordingSeconds++
            }
        } else {
            recordingSeconds = 30
        }
    }

    // Selected incident ID for Video library view
    var selectedIncidentId by remember { mutableStateOf<String?>(null) }

    // Auto-select first incident when list is loaded if none selected
    LaunchedEffect(allIncidents) {
        if (selectedIncidentId == null && allIncidents.isNotEmpty()) {
            selectedIncidentId = allIncidents.firstOrNull()?.id
        }
    }

    // Always get the latest incident data from the list based on the ID
    val selectedIncident = remember(selectedIncidentId, allIncidents) {
        allIncidents.find { it.id == selectedIncidentId }
    }

    // Safety Settings States from Database
    val faceDetectionEnabled = eventStatuses[FACE_DETECTION] == 1
    val honkEventEnabled = eventStatuses[HONK_EVENT] == 1
    val hardBrakingEnabled = eventStatuses[HARD_BRAKING] == 1
    val alarmEnabled = eventStatuses[ALARM] == 1

    val filteredIncidents = remember(selectedTab, allIncidents) {
        if (selectedTab == ClipTab.All) allIncidents
        else allIncidents.filter { incident ->
            when (selectedTab) {
                ClipTab.Parked -> incident.gear == 0
                ClipTab.Driving -> incident.gear == 1
                ClipTab.Shared -> false
                else -> true
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // ── Left Sidebar ──────────────────────────────────────────────────
        SideNavigationBar(
            selectedItem = currentNavItem,
            onItemClick = { currentNavItem = it }
        )

        // ── Main Content ──────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (currentNavItem == DashcamNavItem.Video) {
                    // ── Left Panel: Video List ─────────────────────
                    ClipListPanel(
                        modifier = Modifier.weight(1f),
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                        incidents = filteredIncidents,
                        selectedIncident = selectedIncident,
                        onIncidentClick = { selectedIncidentId = it.id }
                    )

                    // ── Right Panel: Event Details ────────────────────────────────
                    Box(
                        modifier = Modifier
                            .weight(1.5f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceDark)
                    ) {
                        selectedIncident?.let {
                            EventDetailsMainScreen(
                                incident = it,
                                onPlayFootageClick = { onPlayVideo(it) }
                            )
                        } ?: Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Select a video to view details", color = TextSecondary)
                        }
                    }
                } else {
                    // ── Left Panel: Car Preview + Record ─────────────────────
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CarPreviewPanel(modifier = Modifier.weight(1f))
                        RecordButton(
                            isRecording = isRecording,
                            seconds = recordingSeconds,
                            onClick = { viewModel.setRecordingStatus(if (isRecording) 0 else 1) }
                        )
                    }

                    // ── Right Panel Content Switcher ────────────────────────────────
                    val rightPanelModifier = Modifier.weight(1.1f)
                    when (currentNavItem) {
                        DashcamNavItem.Car -> {
                            SafetySettingsPanel(
                                modifier = rightPanelModifier,
                                faceDetection = faceDetectionEnabled,
                                onFaceDetectionChange = { viewModel.updateEventStatus(FACE_DETECTION, if (it) 1 else 0) },
                                honkEvent = honkEventEnabled,
                                onHonkEventChange = { viewModel.updateEventStatus(HONK_EVENT, if (it) 1 else 0) },
                                hardBraking = hardBrakingEnabled,
                                onHardBrakingChange = { viewModel.updateEventStatus(HARD_BRAKING, if (it) 1 else 0) },
                                alarm = alarmEnabled,
                                onAlarmChange = { viewModel.updateEventStatus(ALARM, if (it) 1 else 0) }
                            )
                        }
                        DashcamNavItem.Cloud -> {
                            CloudUploadPanel(
                                modifier = rightPanelModifier,
                                incidents = allIncidents
                            )
                        }
                        DashcamNavItem.Profile -> {
                            ProfilePanel(
                                modifier = rightPanelModifier,
                                onLogout = {
                                    val sharedPref = context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
                                    sharedPref.edit().clear().apply()
                                    context.startActivity(Intent(context, MainActivity::class.java).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    })
                                }
                            )
                        }
                        else -> {
                            // Home
                            ClipListPanel(
                                modifier = rightPanelModifier,
                                selectedTab = selectedTab,
                                onTabSelected = { selectedTab = it },
                                incidents = filteredIncidents,
                                selectedIncident = null, // No selection bar on Home
                                onIncidentClick = { incident ->
                                    selectedIncidentId = incident.id
                                    currentNavItem = DashcamNavItem.Video
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Profile Panel ───────────────────────────────────────────────────────────

@Composable
fun ProfilePanel(modifier: Modifier = Modifier, onLogout: () -> Unit) {
    val context = LocalContext.current
    val sharedPref = remember { context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE) }
    val name = sharedPref.getString("name", "User") ?: "User"
    val email = sharedPref.getString("email", "user@example.com") ?: "user@example.com"
    val vehicle = sharedPref.getString("vehicleNumber", "KL-01-AX-1234") ?: "KL-01-AX-1234"

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(SurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(50.dp))
        }
        
        Spacer(Modifier.height(16.dp))
        
        Text(text = name, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(text = email, color = TextSecondary, fontSize = 14.sp)
        
        Spacer(Modifier.height(32.dp))
        
        HorizontalDivider(color = DividerColor)
        
        Spacer(Modifier.height(24.dp))
        
        ProfileDetailRow(icon = Icons.Default.DirectionsCar, label = "Vehicle Number", value = vehicle)
        ProfileDetailRow(icon = Icons.Default.VerifiedUser, label = "Account Status", value = "Verified Pro")
        ProfileDetailRow(icon = Icons.Default.CloudQueue, label = "Cloud Storage", value = "12.4 GB / 50 GB")

        Spacer(Modifier.weight(1f))
        
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(12.dp))
            Text("Logout Session", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ProfileDetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Column {
            Text(text = label, color = TextSecondary, fontSize = 11.sp)
            Text(text = value, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ─── Side Navigation Bar ─────────────────────────────────────────────────────

@Composable
fun SideNavigationBar(
    selectedItem: DashcamNavItem,
    onItemClick: (DashcamNavItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(56.dp)
            .background(SidebarBg)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        NavIcon(icon = Icons.Default.Home, isSelected = selectedItem == DashcamNavItem.Home, onClick = { onItemClick(DashcamNavItem.Home) })
        Spacer(Modifier.height(4.dp))
        NavIcon(icon = Icons.Default.DirectionsCar, isSelected = selectedItem == DashcamNavItem.Car, onClick = { onItemClick(DashcamNavItem.Car) })
        NavIcon(icon = Icons.Default.VideoLibrary, isSelected = selectedItem == DashcamNavItem.Video, onClick = { onItemClick(DashcamNavItem.Video) })
        NavIcon(icon = Icons.Default.CloudUpload, isSelected = selectedItem == DashcamNavItem.Cloud, onClick = { onItemClick(DashcamNavItem.Cloud) })
        
        Spacer(Modifier.weight(1f))
        
        NavIcon(icon = Icons.Default.AccountCircle, isSelected = selectedItem == DashcamNavItem.Profile, onClick = { onItemClick(DashcamNavItem.Profile) })

        Box(
            modifier = Modifier.width(3.dp).height(32.dp).clip(RoundedCornerShape(2.dp))
                .background(AccentTeal).align(Alignment.Start)
        )
    }
}

@Composable
private fun NavIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    tint: Color = if (isSelected) AccentTeal else TextSecondary
) {
    Box(
        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) SurfaceVariant else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
    }
}

// ─── Safety Settings Panel ──────────────────────────────────────────────────

@Composable
fun SafetySettingsPanel(
    modifier: Modifier = Modifier,
    faceDetection: Boolean,
    onFaceDetectionChange: (Boolean) -> Unit,
    honkEvent: Boolean,
    onHonkEventChange: (Boolean) -> Unit,
    hardBraking: Boolean,
    onHardBrakingChange: (Boolean) -> Unit,
    alarm: Boolean,
    onAlarmChange: (Boolean) -> Unit
) {
    Column(
        modifier = modifier.fillMaxHeight().clip(RoundedCornerShape(16.dp)).background(SurfaceDark)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AdminPanelSettings, null, tint = AccentTeal, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Safety & AI Intelligence", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Configure automated incident detection", color = TextSecondary, fontSize = 12.sp)
            }
        }
        HorizontalDivider(color = DividerColor, thickness = 1.dp)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            
            SafetyToggleCard("NORMAL RECORDING", "Continuous loop recording while driving", Icons.Outlined.Videocam, true, {}, isLocked = true)
            SafetyToggleCard("CRASH RECORDING", "High-priority emergency event capture", Icons.Outlined.Report, true, {}, isLocked = true)
            SafetyToggleCard("FACE DETECTION", "Detect and alert driver fatigue or distraction", Icons.Outlined.Face, faceDetection, onFaceDetectionChange)
            SafetyToggleCard("HONK EVENT", "Automatically record when unusual honking occurs", Icons.Outlined.VolumeUp, honkEvent, onHonkEventChange)
            SafetyToggleCard("HARD BRAKING", "Trigger emergency recording on sudden deceleration", Icons.Outlined.ReportProblem, hardBraking, onHardBrakingChange)
            SafetyToggleCard("ALARM SYSTEM", "Enable audible alerts for critical safety events", Icons.Outlined.NotificationsActive, alarm, onAlarmChange)
            
            Spacer(Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(AccentTeal.copy(alpha = 0.05f)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, null, tint = AccentTeal, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Some features may require active internet connection for real-time cloud sync.", color = AccentTeal.copy(alpha = 0.8f), fontSize = 11.sp)
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SafetyToggleCard(
    title: String, 
    description: String, 
    icon: ImageVector, 
    checked: Boolean, 
    onCheckedChange: (Boolean) -> Unit,
    isLocked: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(if (checked) SurfaceVariant else SurfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, if (checked) AccentTeal.copy(alpha = 0.3f) else Color.Transparent, RoundedCornerShape(12.dp))
            .then(if (!isLocked) Modifier.clickable { onCheckedChange(!checked) } else Modifier)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically, 
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(if (checked) AccentTeal.copy(alpha = 0.1f) else BackgroundDark.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = if (checked) AccentTeal else TextSecondary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, color = if (checked) AccentTeal else TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    if (isLocked) {
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.Lock, null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(12.dp))
                    }
                }
                Text(description, color = TextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        if (isLocked) {
            Text("ALWAYS ON", color = AccentTeal, fontSize = 10.sp, fontWeight = FontWeight.Black)
        } else {
            Switch(
                checked = checked, 
                onCheckedChange = onCheckedChange, 
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White, 
                    checkedTrackColor = AccentTeal, 
                    uncheckedThumbColor = TextSecondary, 
                    uncheckedTrackColor = BackgroundDark, 
                    uncheckedBorderColor = DividerColor
                )
            )
        }
    }
}

// ─── Cloud Upload Panel ──────────────────────────────────────────────────

@Composable
fun CloudUploadPanel(modifier: Modifier = Modifier, incidents: List<IncidentRecord>) {
    val uploadingIncidents = remember(incidents) { incidents.filter { it.fileUploadedStatus < 100 } }
    Column(modifier = modifier.fillMaxHeight().padding(15.dp).clip(RoundedCornerShape(16.dp)).background(SurfaceDark)) {
        Text("Cloud Upload Progress", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
        HorizontalDivider(color = DividerColor, thickness = 1.dp)
        if (uploadingIncidents.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CloudDone, null, tint = AccentTeal.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("All Files are Uploaded", color = AccentTeal, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 6.dp)) {
                items(uploadingIncidents) { incident ->
                    ClipListItem(incident)
                    Divider(color = DividerColor.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 12.dp))
                }
            }
        }
    }
}

// ─── Car Preview Panel ───────────────────────────────────────────────────────

@Composable
fun CarPreviewPanel(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().padding(15.dp).clip(RoundedCornerShape(16.dp)).background(SurfaceDark).border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.size(260.dp, 80.dp).align(Alignment.BottomCenter).offset(y = (-20).dp).background(Brush.radialGradient(colors = listOf(AccentTeal.copy(alpha = 0.2f), Color.Transparent))).blur(30.dp))

        Box(
            modifier = Modifier
                .size(500.dp)
                .background(Brush.radialGradient(listOf(AccentTeal.copy(alpha = 0.15f), Color.Transparent)))
                .blur(60.dp)
        )

        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Image(painter = painterResource(R.drawable.car_image), null, contentScale = ContentScale.FillWidth, modifier = Modifier.padding(bottom = 10.dp))
        }
        Column(modifier = Modifier.align(Alignment.TopStart).padding(16.dp)) {
            Text("Secure360 Pro", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("Vehicle Connected", color = AccentTeal, fontSize = 10.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.5.sp)
        }
        Row(modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).clip(RoundedCornerShape(20.dp)).background(Color.Black.copy(alpha = 0.3f)).padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(AccentTeal))
            Text("LIVE", fontSize = 10.sp, color = AccentTeal, fontWeight = FontWeight.Black)
        }
        Row(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CarStatChip(Modifier.weight(1f), Icons.Default.BatteryChargingFull, "84%", "Battery", AccentTeal)
            CarStatChip(Modifier.weight(1f), Icons.Default.Speed, "12,450 km", "Total Dist", TextPrimary)
            CarStatChip(Modifier.weight(1f), Icons.Default.Security, "ACTIVE", "Sentry", AccentRed)
        }
    }
}

@Composable
private fun CarStatChip(modifier: Modifier = Modifier, icon: ImageVector, value: String, label: String, color: Color) {
    Box(modifier = modifier.clip(RoundedCornerShape(12.dp)).background(SurfaceVariant.copy(alpha = 0.6f)).padding(vertical = 10.dp, horizontal = 4.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(Modifier.height(6.dp))
            Text(value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(label, color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Medium)
        }
    }
}

// ─── Record Button ───────────────────────────────────────────────────────────

@Composable
fun RecordButton(isRecording: Boolean, seconds: Int, onClick: () -> Unit) {
    val animatedProgress by animateFloatAsState(targetValue = seconds.toFloat() / 60, animationSpec = tween(1000, easing = LinearEasing), label = "progress")
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infiniteTransition.animateFloat(0.3f, 0.7f, infiniteRepeatable(tween(900, easing = EaseInOutSine), RepeatMode.Reverse), label = "glow")
    Box(modifier = Modifier.fillMaxWidth().height(70.dp), contentAlignment = Alignment.Center) {
        if (!isRecording) {
            Box(modifier = Modifier.size(220.dp, 70.dp).clip(RoundedCornerShape(10.dp)).background(AccentRed.copy(alpha = glowAlpha * 0.25f)).blur(16.dp))
            Button(onClick = onClick, modifier = Modifier.height(56.dp).fillMaxWidth(0.75f), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(AccentRed)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Record", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color.White))
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth(0.9f).height(56.dp).clip(RoundedCornerShape(12.dp)).background(SurfaceVariant).border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))) {
                Box(modifier = Modifier.fillMaxWidth(animatedProgress).fillMaxHeight().background(Brush.horizontalGradient(listOf(AccentRed.copy(alpha = 0.6f), AccentRed))))
                Row(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val dotAlpha by infiniteTransition.animateFloat(0.4f, 1f, infiniteRepeatable(tween(500), RepeatMode.Reverse), label = "dot")
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.White.copy(alpha = dotAlpha)))
                        Spacer(Modifier.width(12.dp))
                        Text("Recording Progress $seconds:60", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = 0.2f)).clickable { onClick() }.padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(Color.White))
                            Text("STOP", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

// ─── Clip List Panel ─────────────────────────────────────────────────────────

@Composable
fun ClipListPanel(modifier: Modifier = Modifier, selectedTab: ClipTab, onTabSelected: (ClipTab) -> Unit, incidents: List<IncidentRecord>, selectedIncident: IncidentRecord? = null, onIncidentClick: (IncidentRecord) -> Unit = {}) {
    Column(modifier = modifier.fillMaxHeight().padding(15.dp).clip(RoundedCornerShape(16.dp)).background(SurfaceDark)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ClipTab.values().forEach { tab -> ClipTabItem(tab.name, selectedTab == tab, { onTabSelected(tab) }, Modifier.weight(1f)) }
        }
        HorizontalDivider(color = DividerColor, thickness = 1.dp)
        if (incidents.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.VideoLibrary, null, tint = TextSecondary.copy(alpha = 0.3f), modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No Recorded Video Error", color = TextSecondary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 6.dp)) {
                items(incidents) { incident ->
                    ClipListItem(incident, incident.id == selectedIncident?.id, { onIncidentClick(incident) })
                    Divider(color = DividerColor.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 12.dp))
                }
            }
        }
    }
}

@Composable
private fun ClipTabItem(label: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.clip(RoundedCornerShape(6.dp)).clickable { onClick() }.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal, color = if (isSelected) AccentTeal else TextSecondary)
            Spacer(Modifier.height(4.dp))
            if (isSelected) Box(modifier = Modifier.height(2.dp).fillMaxWidth(0.5f).clip(RoundedCornerShape(1.dp)).background(AccentTeal))
        }
    }
}

@Composable
private fun ClipListItem(incident: IncidentRecord, isSelected: Boolean = false, onClick: () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth().background(if (isSelected) SurfaceVariant else Color.Transparent).clickable { onClick() }.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier.size(110.dp, 66.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF2C3340)), contentAlignment = Alignment.BottomEnd) {
            Canvas(Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val paint = androidx.compose.ui.graphics.Paint().apply { color = Color.White.copy(alpha = 0.12f); strokeWidth = 1.5f }
                drawIntoCanvas { canvas ->
                    canvas.drawLine(androidx.compose.ui.geometry.Offset(0f, h * 0.45f), androidx.compose.ui.geometry.Offset(w, h * 0.45f), paint)
                    canvas.drawLine(androidx.compose.ui.geometry.Offset(w * 0.35f, h * 0.45f), androidx.compose.ui.geometry.Offset(w * 0.1f, h), paint)
                    canvas.drawLine(androidx.compose.ui.geometry.Offset(w * 0.65f, h * 0.45f), androidx.compose.ui.geometry.Offset(w * 0.9f, h), paint)
                }
            }
            Box(modifier = Modifier.padding(4.dp).clip(RoundedCornerShape(4.dp)).background(Color.Black.copy(alpha = 0.65f)).padding(horizontal = 5.dp, vertical = 2.dp)) {
                Text(incident.time, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Medium)
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text("${incident.title} - ${incident.date}", fontSize = 14.sp, color = if (isSelected) AccentTeal else TextPrimary, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(if (incident.gear == 0) Color(0xFFFFA726) else AccentTeal))
                Text("${incident.placeCityName}, ${incident.roadName}", fontSize = 11.sp, color = if (isSelected) TextPrimary.copy(alpha = 0.8f) else TextSecondary)
            }
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Center) {
            Text("${incident.fileUploadedStatus}%", fontSize = 12.sp, color = if (incident.fileUploadedStatus == 100) AccentTeal else Color(0xFFFFA726), fontWeight = FontWeight.Bold)
            LinearProgressIndicator(progress = { incident.fileUploadedStatus / 100f }, modifier = Modifier.width(40.dp).height(4.dp).clip(RoundedCornerShape(2.dp)), color = if (incident.fileUploadedStatus == 100) AccentTeal else Color(0xFFFFA726), trackColor = if (isSelected) SurfaceDark else SurfaceVariant)
        }
    }
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 900, heightDp = 520, backgroundColor = 0xFF1A1D22)
@Composable
fun DashcamScreenPreview() { MaterialTheme { DashcamScreen() } }
