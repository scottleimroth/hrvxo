package com.heartsyncradio.music

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import com.heartsyncradio.db.AllSessionSummaries
import com.heartsyncradio.db.AllTimeBestSong
import com.heartsyncradio.db.CoherenceTrend
import com.heartsyncradio.db.HrvXoDatabase
import com.heartsyncradio.db.OverallStats
import com.heartsyncradio.db.Song_coherence
import com.heartsyncradio.db.TopArtistByCoherence
import com.heartsyncradio.db.TopCoherenceSongs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class SongCoherenceRepository(private val database: HrvXoDatabase) {

    private val queries get() = database.songCoherenceQueries

    suspend fun insertSong(
        videoId: String,
        title: String,
        artist: String,
        avgCoherence: Double,
        avgRmssd: Double,
        meanHr: Double,
        durationListenedSec: Long,
        sessionDate: Long,
        movementDetected: Boolean = false
    ) = withContext(Dispatchers.Default) {
        queries.insertSong(
            video_id = videoId,
            title = title,
            artist = artist,
            avg_coherence = avgCoherence,
            avg_rmssd = avgRmssd,
            mean_hr = meanHr,
            duration_listened_sec = durationListenedSec,
            session_date = sessionDate,
            movement_detected = if (movementDetected) 1L else 0L
        )
    }

    fun topCoherenceSongs(limit: Long): Flow<List<TopCoherenceSongs>> {
        return queries.topCoherenceSongs(limit).asFlow().mapToList(Dispatchers.Default)
    }

    fun allSongsForSession(sessionDate: Long): Flow<List<Song_coherence>> {
        return queries.allSongsForSession(sessionDate).asFlow().mapToList(Dispatchers.Default)
    }

    fun songCount(): Flow<Long> {
        return queries.songCount().asFlow().mapToOne(Dispatchers.Default)
    }

    fun allSessionSummaries(): Flow<List<AllSessionSummaries>> {
        return queries.allSessionSummaries().asFlow().mapToList(Dispatchers.Default)
    }

    suspend fun allSongsForExport(): List<Song_coherence> = withContext(Dispatchers.Default) {
        queries.allSongsForExport().executeAsList()
    }

    suspend fun overallStats(): OverallStats? = withContext(Dispatchers.Default) {
        queries.overallStats().executeAsOneOrNull()
    }

    suspend fun topArtistByCoherence(): TopArtistByCoherence? = withContext(Dispatchers.Default) {
        queries.topArtistByCoherence().executeAsOneOrNull()
    }

    suspend fun coherenceTrend(): List<CoherenceTrend> = withContext(Dispatchers.Default) {
        queries.coherenceTrend().executeAsList()
    }

    suspend fun allTimeBestSong(): AllTimeBestSong? = withContext(Dispatchers.Default) {
        queries.allTimeBestSong().executeAsOneOrNull()
    }

    suspend fun distinctSessionDates(): List<Long> = withContext(Dispatchers.Default) {
        queries.distinctSessionDates().executeAsList()
    }

    suspend fun deleteSession(sessionDate: Long) = withContext(Dispatchers.Default) {
        queries.deleteSession(sessionDate)
    }

    suspend fun deleteAllData() = withContext(Dispatchers.Default) {
        queries.deleteAllData()
    }
}
