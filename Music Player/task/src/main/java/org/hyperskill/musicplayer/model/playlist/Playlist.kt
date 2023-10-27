package org.hyperskill.musicplayer.model.playlist

import org.hyperskill.musicplayer.model.song.SongType

interface Playlist {
    val songs: MutableList<SongType.Song>
}