package org.hyperskill.musicplayer.repository

import android.util.ArrayMap
import org.hyperskill.musicplayer.model.song.Song

class PlaylistsRepositoryImpl : PlaylistsRepository {

    override val playlists = ArrayMap<String, MutableList<Song>>()
    override fun getDefaultSongs(): MutableList<Song> {
        return MutableList(10) { i ->
            Song(i + 1, "title${i + 1}", "artist${i + 1}", 215_000)
        }
    }

    override fun getPlaylist(name: String): MutableList<Song>? {
        return playlists[name]
    }

    override fun addPlaylist(name: String, songs: MutableList<Song>) {
        playlists[name] = songs
    }

    override fun removePlaylist(name: String) {
        playlists.remove(name)
    }
}