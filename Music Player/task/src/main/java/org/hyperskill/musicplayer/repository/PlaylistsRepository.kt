package org.hyperskill.musicplayer.repository

import android.util.ArrayMap
import org.hyperskill.musicplayer.model.song.Song

interface PlaylistsRepository {
    val playlists: ArrayMap<String, List<Song>>
    fun getDefaultSongs(): MutableList<Song>
    fun getPlaylist(name: String): MutableList<Song>?
    fun addPlaylist(name: String, songs: Collection<Song>)
    fun removePlaylist(name: String)
}