package com.heartsyncradio.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heartsyncradio.music.SongCoherenceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class InsightsState(
    val totalListens: Long = 0,
    val uniqueSongs: Long = 0,
    val totalSessions: Long = 0,
    val overallAvgCoherence: Double = 0.0,
    val allTimeBestCoherence: Double = 0.0,
    val totalListenTimeSec: Long = 0,
    val bestSongTitle: String? = null,
    val bestSongArtist: String? = null,
    val topArtist: String? = null,
    val topArtistCoherence: Double = 0.0,
    val topArtistListenCount: Long = 0,
    val coherenceTrend: List<TrendPoint> = emptyList(),
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val isLoaded: Boolean = false
)

data class TrendPoint(
    val sessionDate: Long,
    val avgCoherence: Double
)

class InsightsViewModel(
    private val repository: SongCoherenceRepository
) : ViewModel() {

    private val _state = MutableStateFlow(InsightsState())
    val state: StateFlow<InsightsState> = _state.asStateFlow()

    init {
        loadInsights()
    }

    private fun loadInsights() {
        viewModelScope.launch {
            val stats = repository.overallStats()
            val bestSong = repository.allTimeBestSong()
            val topArtist = repository.topArtistByCoherence()
            val trend = repository.coherenceTrend()
            val sessionDates = repository.distinctSessionDates()

            val trendPoints = trend.map { t ->
                TrendPoint(
                    sessionDate = t.session_date,
                    avgCoherence = t.avg_coh ?: 0.0
                )
            }

            // Calculate streaks from session dates
            val dates = sessionDates.map { sd ->
                Instant.ofEpochMilli(sd)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }.distinct().sortedDescending()

            val (current, longest) = calculateStreaks(dates)

            _state.value = InsightsState(
                totalListens = stats?.total_listens ?: 0,
                uniqueSongs = stats?.unique_songs ?: 0,
                totalSessions = stats?.total_sessions ?: 0,
                overallAvgCoherence = stats?.overall_avg_coherence ?: 0.0,
                allTimeBestCoherence = stats?.all_time_best_coherence ?: 0.0,
                totalListenTimeSec = stats?.total_listen_time_sec ?: 0,
                bestSongTitle = bestSong?.title,
                bestSongArtist = bestSong?.artist,
                topArtist = topArtist?.artist,
                topArtistCoherence = topArtist?.avg_coh ?: 0.0,
                topArtistListenCount = topArtist?.listen_count ?: 0,
                coherenceTrend = trendPoints,
                currentStreak = current,
                longestStreak = longest,
                isLoaded = true
            )
        }
    }

    fun refresh() {
        loadInsights()
    }

    companion object {
        fun calculateStreaks(datesDescending: List<LocalDate>): Pair<Int, Int> {
            if (datesDescending.isEmpty()) return Pair(0, 0)

            val today = LocalDate.now()
            var currentStreak = 0
            var longestStreak = 0
            var streak = 1

            // Current streak: count consecutive days from today backwards
            val hasToday = datesDescending.firstOrNull() == today
            val hasYesterday = datesDescending.firstOrNull() == today.minusDays(1)

            if (hasToday || hasYesterday) {
                val startDate = if (hasToday) today else today.minusDays(1)
                currentStreak = 1
                var expected = startDate.minusDays(1)
                for (date in datesDescending) {
                    if (date == startDate) continue
                    if (date == expected) {
                        currentStreak++
                        expected = expected.minusDays(1)
                    } else if (date.isBefore(expected)) {
                        break
                    }
                }
            }

            // Longest streak: scan all dates ascending
            val sorted = datesDescending.reversed()
            var maxStreak = 1
            var runStreak = 1
            for (i in 1 until sorted.size) {
                if (sorted[i] == sorted[i - 1].plusDays(1)) {
                    runStreak++
                } else {
                    maxStreak = maxOf(maxStreak, runStreak)
                    runStreak = 1
                }
            }
            maxStreak = maxOf(maxStreak, runStreak)
            longestStreak = maxStreak

            return Pair(currentStreak, longestStreak)
        }
    }
}
