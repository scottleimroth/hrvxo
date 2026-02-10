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

    companion object {
        const val SETTLE_IN_MS = 15_000L
        const val MIN_RECORDING_SEC = 60
    }

    fun startSession() {
        _phase.value = SessionPhase.ACTIVE_NO_SONG
        _currentSong.value = null
        _results.value = emptyList()
        _settleCountdownSec.value = 0
        _recordingDurationSec.value = 0
    }

    fun tagSong(result: SearchResult, currentTimeMillis: Long) {
        finalizeCurrent(currentTimeMillis)
        _currentSong.value = TaggedSong(
            searchResult = result,
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
    }

    fun reset() {
        _phase.value = SessionPhase.NOT_STARTED
        _currentSong.value = null
        _results.value = emptyList()
        _settleCountdownSec.value = 0
        _recordingDurationSec.value = 0
    }

    private fun finalizeCurrent(currentTimeMillis: Long) {
        val song = _currentSong.value ?: return
        if (song.coherenceReadings.isEmpty()) return

        val durationSec =
            ((currentTimeMillis - song.taggedAtMillis - SETTLE_IN_MS) / 1000).toInt()
                .coerceAtLeast(0)

        val result = SongSessionResult(
            searchResult = song.searchResult,
            avgCoherence = song.coherenceReadings.average(),
            avgRmssd = song.rmssdReadings.average(),
            meanHr = song.hrReadings.average(),
            durationListenedSec = durationSec,
            isValid = durationSec >= MIN_RECORDING_SEC
        )
        _results.value = _results.value + result
    }
}
