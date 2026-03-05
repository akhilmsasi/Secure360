package com.cet.secure360

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cet.secure360.model.IncidentRecord

// ─── Shared Color Palette (Matching DashcamScreen) ───────────────────────────

private val SurfaceDark      = Color(0xFF22262E)
private val SurfaceVariant   = Color(0xFF2A2F39)
private val AccentTeal       = Color(0xFF00C9A7)
private val AccentRed        = Color(0xFFE53935)
private val TextPrimary      = Color(0xFFECEFF4)
private val TextSecondary    = Color(0xFF8D93A1)
private val DividerColor     = Color(0xFF2E333D)

private fun getIncidentTypeText(incidentType: Int): String {
    return when (incidentType) {
        0 -> "Manual Sentry"
        1 -> "Harsh Braking"
        2 -> "Lane Departure"
        3 -> "Over Speeding"
        4 -> "Forward Collision"
        5 -> "Harsh Acceleration"
        6 -> "Proximity Alert"
        else -> "Unknown"
    }
}

@Composable
fun EventDetailsMainScreen(
    incident: IncidentRecord,
    modifier: Modifier = Modifier,
    onPlayFootageClick: (String) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Event Analysis",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (incident.incidentType == 0) Color(0xFFFFA726).copy(alpha = 0.2f) else AccentTeal.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = getIncidentTypeText(incident.incidentType).uppercase(),
                    color = if (incident.incidentType == 0) Color(0xFFFFA726) else AccentTeal,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        HorizontalDivider(color = DividerColor, thickness = 1.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Upper Section: Map and Info Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.3f),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Interactive Map Placeholder
                Box(
                    modifier = Modifier
                        .weight(1.6f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceVariant)
                ) {
                    Image(
                        painter = painterResource(R.drawable.map_full_view),
                        contentDescription = "Map View",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // Bottom shadow for text visibility
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                                    startY = 200f
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = incident.roadName,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${incident.placeCityName} • ${incident.date}",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }
                }

                // Vital Stats Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        label = "Velocity",
                        value = "${incident.vehicleSpeed.toInt()} km/h",
                        icon = Icons.Default.Speed,
                        color = if (incident.vehicleSpeed > 80) AccentRed else AccentTeal
                    )
                    StatCard(
                        label = "Timeline",
                        value = incident.time,
                        icon = Icons.Default.Schedule,
                        color = TextSecondary
                    )
                    StatCard(
                        label = "Transmission",
                        value = "Gear D${incident.gear}",
                        icon = Icons.Default.Settings,
                        color = TextSecondary
                    )
                }
            }

            // Lower Section: Footage and Logs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Video Playback Action
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceVariant)
                        .clickable { onPlayFootageClick(incident.filepath) },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.thumbnail),
                        contentDescription = "Footage Preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // Play Button Overlay
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    Text(
                        text = "REVIEW FOOTAGE",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                    )
                }

                // Data Logs
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Telemetry Data",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    DataRow("Latitude", incident.locationLat.toString())
                    DataRow("Longitude", incident.locationLong.toString())
                    
                    // Progress Indicator for Upload
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Upload status", color = TextSecondary, fontSize = 11.sp)
                            Text("${incident.fileUploadedStatus}%", color = if (incident.fileUploadedStatus == 100) AccentTeal else Color(0xFFFFA726), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                        LinearProgressIndicator(
                            progress = { incident.fileUploadedStatus / 100f },
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                            color = if (incident.fileUploadedStatus == 100) AccentTeal else Color(0xFFFFA726),
                            trackColor = SurfaceDark
                        )
                    }
                    
                    Spacer(Modifier.weight(1f))
                    
                    Button(
                        onClick = { /* Share Evidence */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariant),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Export Log", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, icon: ImageVector, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceVariant)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        }
        Column {
            Text(label, color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Medium)
            Text(value, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun DataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary, fontSize = 11.sp)
        Text(value, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}
