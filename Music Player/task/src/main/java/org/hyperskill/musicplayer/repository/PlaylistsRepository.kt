package org.hyperskill.musicplayer.repository

import android.util.ArrayMap
import org.hyperskill.musicplayer.song.Song

interface PlaylistsRepository {
    val playlists: ArrayMap<String, List<Song>>
    fun getDefaultSongs(): MutableList<Song>
    fun getAllPlaylists(): ArrayMap<String, List<Song>>
    fun getPlaylistByName(name: String): MutableList<Song>?
    fun addPlaylist(name: String, songs: Collection<Song>)
    fun removePlaylist(name: String)
}