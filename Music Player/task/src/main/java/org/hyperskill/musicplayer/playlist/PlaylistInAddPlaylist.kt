package org.hyperskill.musicplayer.playlist

import org.hyperskill.musicplayer.song.Song
import org.hyperskill.musicplayer.song.SongSelector

class PlaylistInAddPlaylist(override var name: String, override val songs: MutableList<Song>) : Playlist {
    var songSelectors = songs.map { SongSelector(it) }.toMutableList()

    fun updateSongSelectors() {
        songSelectors = songs.map { SongSelector(it) }.toMutableList()
    }

    override fun modifySong(modifiedSong: Song, position: Int) {
        songs[position] = modifiedSong
    }
}