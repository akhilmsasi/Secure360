package com.cet.secure360

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cet.secure360.model.IncidentRecord
import com.cet.secure360.model.dummyIncidentList

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

// ─── Data Models ─────────────────────────────────────────────────────────────

enum class DashcamNavItem { Home, Car, Video, Settings, Cloud, Apps, Phone }

enum class ClipTab { All, Parked, Driving, Shared }

// ─── Main Screen ─────────────────────────────────────────────────────────────

@Composable
fun DashcamScreen() {
    var currentNavItem by remember { mutableStateOf(DashcamNavItem.Home) }
    var selectedTab by remember { mutableStateOf(ClipTab.All) }
    var isRecording by remember { mutableStateOf(false) }

    // Safety Settings States
    var faceDetectionEnabled by remember { mutableStateOf(false) }
    var honkEventEnabled by remember { mutableStateOf(false) }
    var hardBrakingEnabled by remember { mutableStateOf(false) }
    var alarmEnabled by remember { mutableStateOf(false) }

    val filteredIncidents = remember(selectedTab) {
        if (selectedTab == ClipTab.All) dummyIncidentList
        else dummyIncidentList.filter { incident ->
            when (selectedTab) {
                ClipTab.Parked -> incident.incidentType == 0
                ClipTab.Driving -> incident.incidentType != 0
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
                        onClick = { isRecording = !isRecording }
                    )
                }

                // ── Right Panel: Clip List OR Safety Settings ────────────────────────────────
                if (currentNavItem == DashcamNavItem.Car) {
                    SafetySettingsPanel(
                        modifier = Modifier.weight(1.1f),
                        faceDetection = faceDetectionEnabled,
                        onFaceDetectionChange = { faceDetectionEnabled = it },
                        honkEvent = honkEventEnabled,
                        onHonkEventChange = { honkEventEnabled = it },
                        hardBraking = hardBrakingEnabled,
                        onHardBrakingChange = { hardBrakingEnabled = it },
                        alarm = alarmEnabled,
                        onAlarmChange = { alarmEnabled = it }
                    )
                } else {
                    ClipListPanel(
                        modifier = Modifier.weight(1.1f),
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                        incidents = filteredIncidents
                    )
                }
            }
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
        // Navigation Icons
        NavIcon(
            icon = Icons.Default.Home,
            isSelected = selectedItem == DashcamNavItem.Home,
            onClick = { onItemClick(DashcamNavItem.Home) }
        )
        Spacer(Modifier.height(4.dp))

        NavIcon(icon = Icons.Default.DirectionsCar, isSelected = selectedItem == DashcamNavItem.Car, onClick = { onItemClick(DashcamNavItem.Car) })
        NavIcon(icon = Icons.Default.VideoLibrary, isSelected = selectedItem == DashcamNavItem.Video, onClick = { onItemClick(DashcamNavItem.Video) })
        NavIcon(icon = Icons.Default.VideoSettings, isSelected = selectedItem == DashcamNavItem.Settings, onClick = { onItemClick(DashcamNavItem.Settings) })
        NavIcon(icon = Icons.Default.CloudUpload, isSelected = selectedItem == DashcamNavItem.Cloud, onClick = { onItemClick(DashcamNavItem.Cloud) })
        NavIcon(icon = Icons.Default.Apps, isSelected = selectedItem == DashcamNavItem.Apps, onClick = { onItemClick(DashcamNavItem.Apps) })
        NavIcon(icon = Icons.Default.Phone, isSelected = selectedItem == DashcamNavItem.Phone, onClick = { onItemClick(DashcamNavItem.Phone) })

        Spacer(Modifier.weight(1f))

        // Bottom: AI icon
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E3A5F)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "AI",
                tint = AccentTeal,
                modifier = Modifier.size(18.dp)
            )
        }

        // Selected indicator
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(32.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(AccentTeal)
                .align(Alignment.Start)
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
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) SurfaceVariant else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
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
        modifier = modifier
            .fillMaxHeight()
            .padding(15.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
    ) {
        Text(
            text = "Safety & AI Settings",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        Divider(color = DividerColor, thickness = 1.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SafetyToggleItem("FACE DETECTION", faceDetection, onFaceDetectionChange)
            SafetyToggleItem("HONK EVENT", honkEvent, onHonkEventChange)
            SafetyToggleItem("HARD BRAKING", hardBraking, onHardBrakingChange)
            SafetyToggleItem("ALARM", alarm, onAlarmChange)
        }
    }
}

@Composable
fun SafetyToggleItem(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceVariant)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = AccentTeal,
                checkedTrackColor = AccentTeal.copy(alpha = 0.3f),
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = BackgroundDark
            )
        )
    }
}

// ─── Car Preview Panel ───────────────────────────────────────────────────────

@Composable
fun CarPreviewPanel(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(15.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark),
        contentAlignment = Alignment.Center
    ) {
        // Subtle gradient glow under car
        Box(
            modifier = Modifier
                .size(220.dp, 60.dp)
                .align(Alignment.BottomCenter)
                .offset(y = (-12).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            AccentTeal.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
                .blur(20.dp)
        )

        Image(
            painter = painterResource(id = R.drawable.car_image),
            contentDescription = "Car preview",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .padding(10.dp)
        )

        // Status dot
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(AccentTeal)
            )
            Text("LIVE", fontSize = 9.sp, color = AccentTeal, fontWeight = FontWeight.Bold)
        }
    }
}

// ─── Record Button ───────────────────────────────────────────────────────────

@Composable
fun RecordButton(isRecording: Boolean, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow
        Box(
            modifier = Modifier
                .size(220.dp, 70.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(AccentRed.copy(alpha = glowAlpha * 0.25f))
                .blur(16.dp)
        )

        Button(
            onClick = onClick,
            modifier = Modifier
                .height(56.dp)
                .fillMaxWidth(0.75f),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isRecording) "Stop" else "Record",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(if (isRecording) RoundedCornerShape(2.dp) else CircleShape)
                        .background(Color.White)
                )
            }
        }
    }
}

// ─── Clip List Panel ─────────────────────────────────────────────────────────

@Composable
fun ClipListPanel(
    modifier: Modifier = Modifier,
    selectedTab: ClipTab,
    onTabSelected: (ClipTab) -> Unit,
    incidents: List<IncidentRecord>
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(15.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
    ) {
        // Tab Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ClipTab.values().forEach { tab ->
                ClipTabItem(
                    label = tab.name,
                    isSelected = selectedTab == tab,
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Divider(color = DividerColor, thickness = 1.dp)

        // Clip List or Empty State
        if (incidents.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.VideoLibrary,
                        contentDescription = null,
                        tint = TextSecondary.copy(alpha = 0.3f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "No Recorded Video Error",
                        color = TextSecondary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 6.dp)
            ) {
                items(incidents) { incident ->
                    ClipListItem(incident = incident)
                    Divider(
                        color = DividerColor.copy(alpha = 0.5f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ClipTabItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) AccentTeal else TextSecondary
            )
            Spacer(Modifier.height(4.dp))
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .height(2.dp)
                        .fillMaxWidth(0.5f)
                        .clip(RoundedCornerShape(1.dp))
                        .background(AccentTeal)
                )
            }
        }
    }
}

@Composable
private fun ClipListItem(incident: IncidentRecord) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Thumbnail
        Box(
            modifier = Modifier
                .size(110.dp, 66.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF2C3340)),
            contentAlignment = Alignment.BottomEnd
        ) {
            // Fake road perspective lines
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val paint = androidx.compose.ui.graphics.Paint().apply {
                    color = Color.White.copy(alpha = 0.12f)
                    strokeWidth = 1.5f
                }
                drawIntoCanvas { canvas ->
                    // horizon line
                    canvas.drawLine(
                        androidx.compose.ui.geometry.Offset(0f, h * 0.45f),
                        androidx.compose.ui.geometry.Offset(w, h * 0.45f),
                        paint
                    )
                    // left lane
                    canvas.drawLine(
                        androidx.compose.ui.geometry.Offset(w * 0.35f, h * 0.45f),
                        androidx.compose.ui.geometry.Offset(w * 0.1f, h),
                        paint
                    )
                    // right lane
                    canvas.drawLine(
                        androidx.compose.ui.geometry.Offset(w * 0.65f, h * 0.45f),
                        androidx.compose.ui.geometry.Offset(w * 0.9f, h),
                        paint
                    )
                }
            }

            // Timestamp badge
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    text = incident.time,
                    fontSize = 10.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Label and Upload Status
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${incident.title} - ${incident.date}",
                fontSize = 14.sp,
                color = TextPrimary,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (incident.incidentType == 0) Color(0xFFFFA726) else AccentTeal)
                )
                Text(
                    text = "${incident.placeCityName}, ${incident.roadName}",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }

        // Upload Status Percentage
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${incident.fileUploadedStatus}%",
                fontSize = 12.sp,
                color = if (incident.fileUploadedStatus == 100) AccentTeal else Color(0xFFFFA726),
                fontWeight = FontWeight.Bold
            )
            LinearProgressIndicator(
                progress = { incident.fileUploadedStatus / 100f },
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = if (incident.fileUploadedStatus == 100) AccentTeal else Color(0xFFFFA726),
                trackColor = SurfaceVariant
            )
        }
    }
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(
    showBackground = true,
    widthDp = 900,
    heightDp = 520,
    backgroundColor = 0xFF1A1D22
)
@Composable
fun DashcamScreenPreview() {
    MaterialTheme {
        DashcamScreen()
    }
}
