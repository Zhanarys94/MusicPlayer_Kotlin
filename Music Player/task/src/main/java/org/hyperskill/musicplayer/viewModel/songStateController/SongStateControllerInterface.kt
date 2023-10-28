package org.hyperskill.musicplayer.viewModel.songStateController

import org.hyperskill.musicplayer.model.song.Song

interface SongStateControllerInterface {
    fun play(song: Song)
    fun pause(song: Song)
    fun stop(song: Song)
}