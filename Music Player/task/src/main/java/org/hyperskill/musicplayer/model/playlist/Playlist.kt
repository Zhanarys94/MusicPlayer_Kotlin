package org.hyperskill.musicplayer.model.playlist

import org.hyperskill.musicplayer.model.song.Song

interface Playlist {
    val songs: MutableList<Song>
}