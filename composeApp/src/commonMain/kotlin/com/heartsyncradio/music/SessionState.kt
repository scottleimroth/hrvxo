package com.heartsyncradio.music

enum class SessionPhase {
    NOT_STARTED,
    ACTIVE_NO_SONG,
    ACTIVE_SETTLING,
    ACTIVE_RECORDING,
    ENDED
}

data class TaggedSong(
    val searchResult: SearchResult,
    val taggedAtMillis: Long,
    val coherenceReadings: MutableList<Double> = mutableListOf(),
    val rmssdReadings: MutableList<Double> = mutableListOf(),
    val hrReadings: MutableList<Double> = mutableListOf()
)

data class SongSessionResult(
    val searchResult: SearchResult,
    val avgCoherence: Double,
    val avgRmssd: Double,
    val meanHr: Double,
    val durationListenedSec: Int,
    val isValid: Boolean
)
