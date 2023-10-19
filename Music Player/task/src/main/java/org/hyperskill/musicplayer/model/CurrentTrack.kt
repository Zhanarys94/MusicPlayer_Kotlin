package org.hyperskill.musicplayer.model

class CurrentTrack(val song: DataType.Song) {
    var state: SongState = SongState.STOPPED

    fun play() {
        state = SongState.PLAYED
        song.songState = SongState.PLAYED
    }

    fun pause() {
        state = SongState.PAUSED
        song.songState = SongState.PAUSED
    }

    fun stop() {
        state = SongState.STOPPED
        song.songState = SongState.STOPPED
    }
}