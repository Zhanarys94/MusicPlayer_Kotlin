package org.hyperskill.musicplayer.model.playlist

import org.hyperskill.musicplayer.model.song.Song

class CurrentPlaylist(override var songs: MutableList<Song>) : Playlist {
    var currentTrack: Song = songs.first()
}
