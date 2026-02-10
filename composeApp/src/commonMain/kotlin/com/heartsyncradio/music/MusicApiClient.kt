package com.heartsyncradio.music

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class MusicApiClient(
    private val baseUrl: String = "https://hrvxo-music.fly.dev"
) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun search(query: String): List<SearchResult> {
        val response = client.post("$baseUrl/search") {
            contentType(ContentType.Application.Json)
            setBody(SearchRequest(query))
        }
        return response.body()
    }

    suspend fun createPlaylist(
        title: String,
        description: String = "",
        songIds: List<String>
    ): String {
        val response = client.post("$baseUrl/create-playlist") {
            contentType(ContentType.Application.Json)
            setBody(CreatePlaylistRequest(title, description, songIds))
        }
        return response.body<CreatePlaylistResponse>().playlistId
    }

    fun close() {
        client.close()
    }
}
