package org.hyperskill.musicplayer.playlist

import org.hyperskill.musicplayer.song.Song
import org.hyperskill.musicplayer.song.SongSelector
import org.hyperskill.musicplayer.repository.PlaylistsRepository

class LoadedPlaylist(override var name: String, override var songs: MutableList<Song>) : Playlist {
    var songSelectors = songs.map { SongSelector(it) }.toMutableList()

/*    val songSelectors: MutableList<SongSelector>
        get() = songs.map { SongSelector(it) }.toMutableList()*/

    fun updateSongSelectors() {
        songSelectors = songs.map { SongSelector(it) }.toMutableList()
    }

    override fun modifySong(modifiedSong: Song, position: Int) {
        songs[position] = modifiedSong
        updateSongSelectors()
    }
}