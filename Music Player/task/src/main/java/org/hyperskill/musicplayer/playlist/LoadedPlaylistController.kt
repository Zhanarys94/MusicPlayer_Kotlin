package org.hyperskill.musicplayer.playlist

import org.hyperskill.musicplayer.song.Song
import org.hyperskill.musicplayer.song.SongSelector
import org.hyperskill.musicplayer.repository.PlaylistsRepository

class LoadedPlaylistController(private val loadedPlaylist: LoadedPlaylist) {

    fun modifySongSelector(originalSong: SongSelector, modifiedSong: Song, position: Int) {
        loadedPlaylist.modifySong(modifiedSong, position)
        loadedPlaylist.songSelectors[position] = SongSelector(modifiedSong)
        loadedPlaylist.songSelectors[position].isSelected = !originalSong.isSelected
    }

    fun transitionToAddPlaylistWithLongClick(defaultPlaylist: MutableList<Song>, playlistName: String, songSelected: Song) {
        loadedPlaylist.songs = defaultPlaylist
        loadedPlaylist.name = playlistName
        val index = loadedPlaylist.songs.indexOfFirst {
            it.artist == songSelected.artist && it.title == songSelected.title &&
                    it.duration == songSelected.duration
        }
        loadedPlaylist.updateSongSelectors()
        loadedPlaylist.songSelectors[index].isSelected = true
    }

    fun getSelectedSongs(): List<Song> {
        return loadedPlaylist.songSelectors.filter { it.isSelected }.map { it.song }
    }
}