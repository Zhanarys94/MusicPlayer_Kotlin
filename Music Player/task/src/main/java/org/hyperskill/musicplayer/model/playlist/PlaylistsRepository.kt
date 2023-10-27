package org.hyperskill.musicplayer.model.playlist

import android.util.ArrayMap
import org.hyperskill.musicplayer.model.song.SongType

class PlaylistsRepositoryImpl {

    private val playlists = ArrayMap<String, MutableList<SongType.Song>>()
    fun getDefaultSongs(): MutableList<SongType.Song> {
        return MutableList(10) { i ->
            SongType.Song(i + 1, "title${i + 1}", "artist${i + 1}", 215_000)
        }
    }

    fun getPlaylist(name: String): MutableList<SongType.Song>? {
        return playlists[name]
    }

    fun addPlaylist(name: String, songs: MutableList<SongType.Song>) {
        playlists[name] = songs
    }

    fun removePlaylist(name: String) {
        playlists.remove(name)
    }
}