package org.hyperskill.musicplayer.repository

import android.util.ArrayMap
import org.hyperskill.musicplayer.song.Song

class PlaylistsRepositoryImpl : PlaylistsRepository {

    override val playlists = ArrayMap<String, List<Song>>()
    override fun getDefaultSongs(): MutableList<Song> {
        return MutableList(10) { i ->
            Song(i + 1, "title${i + 1}", "artist${i + 1}", 215_000)
        }
    }

    override fun getAllPlaylists(): ArrayMap<String, List<Song>> {
        return playlists
    }

    override fun getPlaylistByName(name: String): MutableList<Song>? {
        return playlists[name]?.toMutableList()
    }

    override fun addPlaylist(name: String, songs: Collection<Song>) {
        playlists[name] = songs.toList()
    }

    override fun removePlaylist(name: String) {
        playlists.remove(name)
    }
}