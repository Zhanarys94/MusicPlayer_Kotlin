package org.hyperskill.musicplayer.playlist

import org.hyperskill.musicplayer.song.Song

interface Playlist {
    val name: String
    val songs: MutableList<Song>

    fun modifySong(modifiedSong: Song, position: Int)
}