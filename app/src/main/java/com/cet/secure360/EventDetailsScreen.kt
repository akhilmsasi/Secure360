package com.cet.secure.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CarCrash
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cet.secure360.R
import com.cet.secure360.model.IncidentRecord

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
    recentEvents: List<IncidentRecord>,
    onItemClick: () -> Unit,
    onPlayFootageClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
    ) {

        Image(
            painter = painterResource(R.drawable.map_full_view),
            contentDescription = "Map View",
            contentScale = ContentScale.Crop, // Ensures the map fills the dimensions properly
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp) // Set your desired height
                .drawWithCache {
                    // 1. Define the vertical gradient brush
                    val gradient = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black),
                        startY = 0f, // Starts at the very top
                        endY = size.height // Ends at the very bottom
                    )

                    onDrawWithContent {
                        // 2. Draw the underlying image first
                        drawContent()

                        // 3. Draw the gradient rectangle over the image
                        drawRect(brush = gradient)
                    }
                }
        )



        Box(
            modifier = Modifier
                .padding(top = 200.dp, start = 20.dp, end = 20.dp)
                .fillMaxWidth()
        ) {
            SelectedVideoThumbnail(
                Modifier
                    .align(Alignment.TopEnd)
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
            ) {

                Text(
                    incident.placeCityName,
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )
                Text(
                    incident.time,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Serif,
                    modifier = Modifier
                        .padding(top = 10.dp)
                )
            }

        }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 350.dp, start = 20.dp, end = 20.dp)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "",
                    tint = Color.Red,
                    modifier = Modifier
                        .size(40.dp)
                )

                Text(
                    incident.roadName,
                    color = Color.Gray,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Serif,
                    modifier = Modifier
                        .fillMaxWidth()
                )

            }

            Row(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .fillMaxWidth(),
            ) {

                IconText(
                    getIncidentTypeText(incident.incidentType),
                    Icons.Default.CarCrash,
                    Modifier.weight(1f)
                )
                IconText(
                    "${incident.vehicleSpeed.toInt()} KM/H",
                    Icons.Default.Speed,
                    Modifier.weight(1f)
                )

            }

            Row(
                modifier = Modifier
                    .padding(top = 40.dp)
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE91E63))
                    .clickable { onPlayFootageClick(incident.filepath) },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = Icons.Default.VideoLibrary,
                    contentDescription = "",
                    tint = Color.White,
                    modifier = Modifier
                        .size(40.dp)
                        .padding(end = 10.dp)
                )
                Text(
                    text = "Play Footage",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }


        }

    }

}

//@Preview(showBackground = true)
//@Composable
//fun EventDetailsMainScreenPreview() {
//    EventDetailsMainScreen(
//        incident = dummyIncidentList.first(),
//        recentEvents = dummyIncidentList,
//        onItemClick = {}
//    )
//}

@Composable
fun SelectedVideoThumbnail(modifier: Modifier = Modifier) {

    Column(
        modifier = modifier
            .size(100.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(Color(0xFFE91E63))
            .padding(5.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(Color.Black)
    ) {
        Image(
            painter = painterResource(R.drawable.thumbnail),
            contentDescription = "",
            contentScale = ContentScale.Crop
        )

    }

}

@Composable
fun IconText(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "",
            tint = Color.White,
            modifier = Modifier
                .size(30.dp)
        )

        Text(
            text,
            color = Color.Gray,
            fontSize = 20.sp,
            fontFamily = FontFamily.Serif,
            modifier = Modifier
                .padding(start = 10.dp)
                .fillMaxWidth()
        )
    }

}
