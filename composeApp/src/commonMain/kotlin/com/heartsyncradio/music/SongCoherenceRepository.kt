package com.heartsyncradio.music

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import com.heartsyncradio.db.HrvXoDatabase
import com.heartsyncradio.db.Song_coherence
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
        sessionDate: Long
    ) = withContext(Dispatchers.Default) {
        queries.insertSong(
            video_id = videoId,
            title = title,
            artist = artist,
            avg_coherence = avgCoherence,
            avg_rmssd = avgRmssd,
            mean_hr = meanHr,
            duration_listened_sec = durationListenedSec,
            session_date = sessionDate
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
}
