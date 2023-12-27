package org.hyperskill.musicplayer.playlist

import org.hyperskill.musicplayer.song.Song

interface CurrentPlaylistControllerInterface {
    fun play(song: Song)
    fun pause(song: Song)
    fun stop(song: Song)
}