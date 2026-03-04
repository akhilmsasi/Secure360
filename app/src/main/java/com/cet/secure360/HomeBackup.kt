package com.cet.secure360

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cet.secure360.localdatabase.RetrofitClient
import com.cet.secure360.model.IncidentRecord
import com.cet.secure360.viewmodel.RecordingViewModel

@Composable
fun HomeScreen1(modifier: Modifier = Modifier) {

    val viewModel: RecordingViewModel = viewModel(
        factory = RecordingViewModel.Factory(RetrofitClient.apiService)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        RecordingScreen1(viewModel)
        IncidentListScreen1(viewModel)
    }
}

@Composable
fun RecordingScreen1(
    viewModel: RecordingViewModel
) {
    // Collect state from ViewModel
    val currentStatus by viewModel.recordingStatus.collectAsState()
    val isUpdating by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (currentStatus == 1) "RECORDING ACTIVE" else "SYSTEM IDLE",
            fontSize = 24.sp,
            color = if (currentStatus == 1) Color.Red else Color.Gray,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (isUpdating) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { viewModel.setRecordingStatus(if (currentStatus == 1) 0 else 1) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (currentStatus == 1) Color.Red else Color.Gray
                ),
                modifier = Modifier.width(200.dp)
            ) {
                Text(if (currentStatus == 1) "Stop Recording" else "Start Recording")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { viewModel.refreshStatus() }) {
            Text("Refresh Manual Sync")
        }
    }
}


@Composable
fun IncidentListScreen1(viewModel: RecordingViewModel) {
    // Observe the StateFlow from ViewModel
    val incidentList by viewModel.incidents.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(incidentList) { incident ->
            IncidentRow1(incident)
        }
    }
}

@Composable
fun IncidentRow1(incident: IncidentRecord) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = incident.title, style = MaterialTheme.typography.titleMedium)
            Text(text = "City: ${incident.placeCityName}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Speed: ${incident.vehicleSpeed} km/h", color = Color.Red)
            Text(text = "Time: ${incident.time}", style = MaterialTheme.typography.labelSmall)
        }
    }
}
