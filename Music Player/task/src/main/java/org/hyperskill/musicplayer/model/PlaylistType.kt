package org.hyperskill.musicplayer.model

/*sealed class PlaylistType {

    class Playlist(val name: String, val songs: MutableList<DataType.Song>) : PlaylistType() {
    }

    class CurrentPlaylist(var playlist: Playlist) : PlaylistType() {
        var currentTrack: DataType.Song? = null
    }

    class LoadedPlaylist(var playlist: Playlist) : PlaylistType() {
        var songsAsSelectors = playlist.songs.map { DataType.SongSelector(it) }.toMutableList()
    }
}*/
class CurrentPlaylist(override var songs: MutableList<DataType.Song>) : Playlist {
    var currentTrack: DataType.Song = songs.first()
}

class LoadedPlaylist(override var songs: MutableList<DataType.Song>) : Playlist {
    var songSelectors = songs.map { DataType.SongSelector(it) }.toMutableList()

    fun updateSongSelectors() {
        songSelectors = songs.map { DataType.SongSelector(it) }.toMutableList()
    }
}

interface Playlist {
    val songs: MutableList<DataType.Song>
}

/*
fun transformSongsToSongSelectors(songs: MutableList<DataType.Song>): MutableList<DataType.SongSelector> {
    return songs.map { DataType.SongSelector(it) }.toMutableList()
}*/
