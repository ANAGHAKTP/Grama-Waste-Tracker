package com.grama.wastetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.grama.wastetracker.ui.theme.GramaTheme
import com.grama.wastetracker.ui.theme.SpaceGroteskFamily

@Composable
fun ReportIssueScreen(onSubmitSuccess: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GramaTheme.colors.bgPrimary)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Log.Issue",
            style = MaterialTheme.typography.displaySmall.copy(
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold
            ),
            color = GramaTheme.colors.textPrimary
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onSubmitSuccess) {
            Text("SUBMIT REPORT")
        }
    }
}
