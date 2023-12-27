package org.hyperskill.musicplayer.playlist

import org.hyperskill.musicplayer.song.Song

class CurrentPlaylist(
    override var name: String,
    override var songs: MutableList<Song>
) : Playlist {
    var currentTrack: Song? = null

    override fun modifySong(modifiedSong: Song, position: Int) {
        songs[position] = modifiedSong
    }
}
