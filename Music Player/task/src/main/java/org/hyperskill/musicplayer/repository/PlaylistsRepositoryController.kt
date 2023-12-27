package org.hyperskill.musicplayer.repository

import android.util.ArrayMap
import org.hyperskill.musicplayer.song.Song

class PlaylistsRepositoryController(private val playlistsRepository: PlaylistsRepository) {
    fun getDefaultSongs(): MutableList<Song> {
        return playlistsRepository.getDefaultSongs()
    }

    fun getAllPlaylists(): ArrayMap<String, List<Song>> {
        return playlistsRepository.getAllPlaylists()
    }

    fun addPlaylist(name: String, songs: Collection<Song>) {
        playlistsRepository.addPlaylist(name, songs)
    }

    fun getPlaylistByName(name: String): MutableList<Song>? {
        return playlistsRepository.getPlaylistByName(name)
    }

    fun removePlaylist(name: String) {
        playlistsRepository.removePlaylist(name)
    }
}