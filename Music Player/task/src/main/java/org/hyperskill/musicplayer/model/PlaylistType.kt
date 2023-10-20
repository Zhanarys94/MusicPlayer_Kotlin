package org.hyperskill.musicplayer.model

sealed class PlaylistType {

    class Playlist(val name: String, val songs: List<DataType.Song>) : PlaylistType() {
    }

    class CurrentPlaylist(var playlist: Playlist) : PlaylistType() {
        var name = playlist.name
        var songs = playlist.songs
        var currentTrack: CurrentTrack? = null
    }

    class LoadedPlaylist(var playlist: Playlist) : PlaylistType() {
        var name = playlist.name
        var songs = playlist.songs
        var songsAsSelectors = songsToSelectors(songs)
    }
}