package org.hyperskill.musicplayer.viewModel.songStateController

import org.hyperskill.musicplayer.model.song.Song
import org.hyperskill.musicplayer.model.song.SongState

class SongStateController : SongStateControllerInterface {

    override fun play(song: Song) {
        song.songState = SongState.PLAYING
    }

    override fun pause(song: Song) {
        song.songState = SongState.PAUSED
    }

    override fun stop(song: Song) {
        song.songState = SongState.STOPPED
    }
}