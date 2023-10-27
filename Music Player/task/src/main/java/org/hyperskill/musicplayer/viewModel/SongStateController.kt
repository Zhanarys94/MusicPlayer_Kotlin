package org.hyperskill.musicplayer.viewModel

import org.hyperskill.musicplayer.model.song.SongType
import org.hyperskill.musicplayer.model.song.SongState

class SongStateController {

    fun play(song: SongType.Song) {
        song.songState = SongState.PLAYING
    }

    fun pause(song: SongType.Song) {
        song.songState = SongState.PAUSED
    }

    fun stop(song: SongType.Song) {
        song.songState = SongState.STOPPED
    }
}