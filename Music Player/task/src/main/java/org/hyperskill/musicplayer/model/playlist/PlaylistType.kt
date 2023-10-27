package org.hyperskill.musicplayer.model.playlist

import org.hyperskill.musicplayer.model.song.SongType

class CurrentPlaylist(override var songs: MutableList<SongType.Song>) : Playlist {
    var currentTrack: SongType.Song = songs.first()
}

class LoadedPlaylist(override var songs: MutableList<SongType.Song>) : Playlist {
    var songSelectors = songs.map { SongType.SongSelector(it) }.toMutableList()

    fun updateSongSelectors() {
        songSelectors = songs.map { SongType.SongSelector(it) }.toMutableList()
    }
}
