package org.hyperskill.musicplayer.model.playlist

import org.hyperskill.musicplayer.model.song.Song
import org.hyperskill.musicplayer.model.song.SongSelector

class LoadedPlaylist(override var name: String, override var songs: MutableList<Song>) : Playlist {
    var songSelectors = songs.map { SongSelector(it) }.toMutableList()

    fun updateSongSelectors() {
        songSelectors = songs.map { SongSelector(it) }.toMutableList()
    }
}