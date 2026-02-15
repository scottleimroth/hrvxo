package com.heartsyncradio.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heartsyncradio.db.AllSessionSummaries
import com.heartsyncradio.db.Song_coherence
import com.heartsyncradio.music.SongCoherenceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class HistoryViewModel(
    private val repository: SongCoherenceRepository
) : ViewModel() {

    val sessions: StateFlow<List<AllSessionSummaries>> = repository.allSessionSummaries()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _expandedSessionSongs = MutableStateFlow<Map<Long, List<Song_coherence>>>(emptyMap())
    val expandedSessionSongs: StateFlow<Map<Long, List<Song_coherence>>> = _expandedSessionSongs.asStateFlow()

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    fun toggleSession(sessionDate: Long) {
        val current = _expandedSessionSongs.value
        if (current.containsKey(sessionDate)) {
            _expandedSessionSongs.value = current - sessionDate
        } else {
            viewModelScope.launch {
                repository.allSongsForSession(sessionDate).collect { songs ->
                    _expandedSessionSongs.value = _expandedSessionSongs.value + (sessionDate to songs)
                }
            }
        }
    }

    suspend fun generateCsvExport(): String {
        _isExporting.value = true
        return try {
            val songs = repository.allSongsForExport()
            val sb = StringBuilder()
            sb.appendLine("Date,Song Title,Artist,Coherence %,RMSSD,Mean HR,Duration (s),Movement")
            for (song in songs) {
                val dateStr = formatDate(song.session_date)
                val coherencePct = "%.1f".format(song.avg_coherence * 100)
                val rmssd = "%.1f".format(song.avg_rmssd)
                val hr = "%.1f".format(song.mean_hr)
                val movement = if (song.movement_detected == 1L) "Yes" else "No"
                val title = "\"${song.title.replace("\"", "\"\"")}\""
                val artist = "\"${song.artist.replace("\"", "\"\"")}\""
                sb.appendLine("$dateStr,$title,$artist,$coherencePct,$rmssd,$hr,${song.duration_listened_sec},$movement")
            }
            sb.toString()
        } finally {
            _isExporting.value = false
        }
    }

    fun deleteSession(sessionDate: Long) {
        viewModelScope.launch {
            repository.deleteSession(sessionDate)
            _expandedSessionSongs.value = _expandedSessionSongs.value - sessionDate
        }
    }

    fun deleteAllData() {
        viewModelScope.launch {
            repository.deleteAllData()
            _expandedSessionSongs.value = emptyMap()
        }
    }

    companion object {
        private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault())

        fun formatDate(epochMillis: Long): String {
            return dateFormatter.format(Instant.ofEpochMilli(epochMillis))
        }
    }
}
