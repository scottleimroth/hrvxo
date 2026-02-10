package com.heartsyncradio.music

import com.heartsyncradio.hrv.HrvMetrics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager {

    private val _phase = MutableStateFlow(SessionPhase.NOT_STARTED)
    val phase: StateFlow<SessionPhase> = _phase.asStateFlow()

    private val _currentSong = MutableStateFlow<TaggedSong?>(null)
    val currentSong: StateFlow<TaggedSong?> = _currentSong.asStateFlow()

    private val _results = MutableStateFlow<List<SongSessionResult>>(emptyList())
    val results: StateFlow<List<SongSessionResult>> = _results.asStateFlow()

    private val _settleCountdownSec = MutableStateFlow(0)
    val settleCountdownSec: StateFlow<Int> = _settleCountdownSec.asStateFlow()

    private val _recordingDurationSec = MutableStateFlow(0)
    val recordingDurationSec: StateFlow<Int> = _recordingDurationSec.asStateFlow()

    // The song the user selected from search, waiting for playback detection
    private val _pendingSong = MutableStateFlow<SearchResult?>(null)
    val pendingSong: StateFlow<SearchResult?> = _pendingSong.asStateFlow()

    companion object {
        const val SETTLE_IN_MS = 15_000L
        const val MIN_RECORDING_SEC = 60
    }

    fun startSession() {
        _phase.value = SessionPhase.ACTIVE_NO_SONG
        _currentSong.value = null
        _pendingSong.value = null
        _results.value = emptyList()
        _settleCountdownSec.value = 0
        _recordingDurationSec.value = 0
    }

    /**
     * User selected a song from search results.
     * App will open it in YouTube Music — we wait for playback detection.
     */
    fun selectSong(result: SearchResult) {
        _pendingSong.value = result
        _phase.value = SessionPhase.ACTIVE_WAITING_PLAYBACK
    }

    /**
     * Called when playback is detected from YouTube Music.
     * If we have a pending song, use that (has videoId for playlist).
     * If no pending song, create a SearchResult from detected metadata.
     */
    fun onPlaybackDetected(detectedTitle: String, detectedArtist: String, currentTimeMillis: Long) {
        if (_phase.value == SessionPhase.NOT_STARTED || _phase.value == SessionPhase.ENDED) return

        val searchResult = _pendingSong.value ?: SearchResult(
            videoId = "",
            title = detectedTitle,
            artist = detectedArtist
        )
        _pendingSong.value = null

        finalizeCurrent(currentTimeMillis)
        _currentSong.value = TaggedSong(
            searchResult = searchResult,
            taggedAtMillis = currentTimeMillis
        )
        _phase.value = SessionPhase.ACTIVE_SETTLING
        _settleCountdownSec.value = (SETTLE_IN_MS / 1000).toInt()
        _recordingDurationSec.value = 0
    }

    /**
     * Called when playback stops (pause/stop) in YouTube Music.
     * Finalizes the current song — if not enough data, it's marked invalid.
     */
    fun onPlaybackStopped(currentTimeMillis: Long) {
        if (_phase.value !in listOf(
                SessionPhase.ACTIVE_SETTLING,
                SessionPhase.ACTIVE_RECORDING
            )
        ) return

        finalizeCurrent(currentTimeMillis)
        _currentSong.value = null
        _phase.value = SessionPhase.ACTIVE_NO_SONG
    }

    /**
     * Called when a new song is detected (metadata changed while playing).
     * Finalizes previous song, starts settle-in for the new one.
     */
    fun onSongChanged(
        newTitle: String,
        newArtist: String,
        currentTimeMillis: Long,
        resolvedResult: SearchResult? = null
    ) {
        if (_phase.value == SessionPhase.NOT_STARTED || _phase.value == SessionPhase.ENDED) return

        finalizeCurrent(currentTimeMillis)
        _currentSong.value = TaggedSong(
            searchResult = resolvedResult ?: SearchResult(
                videoId = "",
                title = newTitle,
                artist = newArtist
            ),
            taggedAtMillis = currentTimeMillis
        )
        _phase.value = SessionPhase.ACTIVE_SETTLING
        _settleCountdownSec.value = (SETTLE_IN_MS / 1000).toInt()
        _recordingDurationSec.value = 0
    }

    fun recordMetrics(metrics: HrvMetrics, currentTimeMillis: Long) {
        val song = _currentSong.value ?: return

        when (_phase.value) {
            SessionPhase.ACTIVE_SETTLING -> {
                val elapsed = currentTimeMillis - song.taggedAtMillis
                val remaining = ((SETTLE_IN_MS - elapsed) / 1000).toInt().coerceAtLeast(0)
                _settleCountdownSec.value = remaining
                if (elapsed >= SETTLE_IN_MS) {
                    _phase.value = SessionPhase.ACTIVE_RECORDING
                }
            }
            SessionPhase.ACTIVE_RECORDING -> {
                song.coherenceReadings.add(metrics.coherenceScore)
                song.rmssdReadings.add(metrics.rmssd)
                song.hrReadings.add(metrics.meanHr)
                _recordingDurationSec.value =
                    ((currentTimeMillis - song.taggedAtMillis - SETTLE_IN_MS) / 1000).toInt()
            }
            else -> {}
        }
    }

    fun endSession(currentTimeMillis: Long) {
        finalizeCurrent(currentTimeMillis)
        _phase.value = SessionPhase.ENDED
        _currentSong.value = null
        _pendingSong.value = null
    }

    fun reset() {
        _phase.value = SessionPhase.NOT_STARTED
        _currentSong.value = null
        _pendingSong.value = null
        _results.value = emptyList()
        _settleCountdownSec.value = 0
        _recordingDurationSec.value = 0
    }

    private fun finalizeCurrent(currentTimeMillis: Long) {
        val song = _currentSong.value ?: return

        val durationSec = if (song.coherenceReadings.isEmpty()) {
            0
        } else {
            ((currentTimeMillis - song.taggedAtMillis - SETTLE_IN_MS) / 1000).toInt()
                .coerceAtLeast(0)
        }

        // Always create a result so the user sees what happened
        if (song.coherenceReadings.isNotEmpty() || durationSec > 0) {
            val result = SongSessionResult(
                searchResult = song.searchResult,
                avgCoherence = if (song.coherenceReadings.isNotEmpty()) song.coherenceReadings.average() else 0.0,
                avgRmssd = if (song.rmssdReadings.isNotEmpty()) song.rmssdReadings.average() else 0.0,
                meanHr = if (song.hrReadings.isNotEmpty()) song.hrReadings.average() else 0.0,
                durationListenedSec = durationSec,
                isValid = durationSec >= MIN_RECORDING_SEC
            )
            _results.value = _results.value + result
        }
    }
}
