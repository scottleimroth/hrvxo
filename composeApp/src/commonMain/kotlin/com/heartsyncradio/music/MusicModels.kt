package com.heartsyncradio.music

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchRequest(val query: String)

@Serializable
data class SearchResult(
    val videoId: String,
    val title: String,
    val artist: String,
    val album: String? = null,
    val duration: String? = null
)

@Serializable
data class CreatePlaylistRequest(
    val title: String,
    val description: String = "",
    @SerialName("song_ids") val songIds: List<String>
)

@Serializable
data class CreatePlaylistResponse(val playlistId: String)
