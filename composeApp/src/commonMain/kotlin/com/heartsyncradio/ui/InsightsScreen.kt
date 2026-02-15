package com.heartsyncradio.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

data class InsightsUi(
    val totalListens: Long = 0,
    val uniqueSongs: Long = 0,
    val totalSessions: Long = 0,
    val overallAvgCoherence: Double = 0.0,
    val allTimeBestCoherence: Double = 0.0,
    val totalListenMinutes: Long = 0,
    val bestSongTitle: String? = null,
    val bestSongArtist: String? = null,
    val topArtist: String? = null,
    val topArtistCoherence: Double = 0.0,
    val topArtistListenCount: Long = 0,
    val trendCoherences: List<Double> = emptyList(),
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val isLoaded: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    insights: InsightsUi,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Insights") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (!insights.isLoaded || insights.totalListens == 0L) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (!insights.isLoaded) "Loading..." else "Complete a session to see insights",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Stats overview
                StatsGrid(insights)

                // Coherence trend chart
                if (insights.trendCoherences.size >= 2) {
                    TrendCard(insights.trendCoherences)
                }

                // Streak card
                if (insights.currentStreak > 0 || insights.longestStreak > 0) {
                    StreakCard(insights.currentStreak, insights.longestStreak)
                }

                // Best song
                insights.bestSongTitle?.let { title ->
                    InsightCard(
                        label = "All-Time Best",
                        primary = "${(insights.allTimeBestCoherence * 100).toInt()}%",
                        secondary = "$title — ${insights.bestSongArtist ?: "Unknown"}"
                    )
                }

                // Top artist
                insights.topArtist?.let { artist ->
                    InsightCard(
                        label = "Best Artist (2+ listens)",
                        primary = "${(insights.topArtistCoherence * 100).toInt()}% avg",
                        secondary = "$artist (${insights.topArtistListenCount} listens)"
                    )
                }

                // Total listen time
                InsightCard(
                    label = "Total Listen Time",
                    primary = "${insights.totalListenMinutes} min",
                    secondary = "${insights.totalListens} listens across ${insights.totalSessions} sessions"
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun StatsGrid(insights: InsightsUi) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            label = "Avg Coherence",
            value = "${(insights.overallAvgCoherence * 100).toInt()}%",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Songs Tracked",
            value = "${insights.uniqueSongs}",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Sessions",
            value = "${insights.totalSessions}",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TrendCard(coherences: List<Double>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Coherence Trend",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            CoherenceChart(
                values = coherences,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "First",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Latest",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CoherenceChart(
    values: List<Double>,
    modifier: Modifier = Modifier
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)

    Canvas(modifier = modifier) {
        if (values.size < 2) return@Canvas

        val maxVal = values.max().coerceAtLeast(0.5)
        val minVal = values.min().coerceAtMost(0.0)
        val range = (maxVal - minVal).coerceAtLeast(0.1)

        val stepX = size.width / (values.size - 1)
        val padding = 4f

        // Grid lines
        for (i in 0..4) {
            val y = padding + (size.height - 2 * padding) * i / 4
            drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
        }

        // Line chart
        val path = Path()
        values.forEachIndexed { index, value ->
            val x = index * stepX
            val y = padding + (size.height - 2 * padding) * (1 - ((value - minVal) / range)).toFloat()
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, lineColor, style = Stroke(width = 3f, cap = StrokeCap.Round))

        // Dots
        values.forEachIndexed { index, value ->
            val x = index * stepX
            val y = padding + (size.height - 2 * padding) * (1 - ((value - minVal) / range)).toFloat()
            drawCircle(lineColor, radius = 4f, center = Offset(x, y))
        }
    }
}

@Composable
private fun StreakCard(currentStreak: Int, longestStreak: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$currentStreak",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = "day streak",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$longestStreak",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = "longest streak",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

@Composable
private fun InsightCard(
    label: String,
    primary: String,
    secondary: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = primary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = secondary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
