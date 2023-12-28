package org.hyperskill.musicplayer.playlist

import org.hyperskill.musicplayer.song.Song
import org.hyperskill.musicplayer.song.SongSelector

class PlaylistInAddPlaylistController(private val playlistInAddPlaylist: PlaylistInAddPlaylist) {

    fun getPlaylist(): MutableList<Song> {
        return playlistInAddPlaylist.songs
    }

    fun getPlaylistName(): String {
        return playlistInAddPlaylist.name
    }

    fun modifySongSelector(originalSong: SongSelector, modifiedSong: Song, position: Int) {
        playlistInAddPlaylist.modifySong(modifiedSong, position)
        playlistInAddPlaylist.songSelectors[position] = SongSelector(modifiedSong)
        playlistInAddPlaylist.songSelectors[position].isSelected = !originalSong.isSelected
    }

    fun changePlaylist(newPlaylist: MutableList<Song>) {
        playlistInAddPlaylist.songs.clear()
        playlistInAddPlaylist.songs.addAll(newPlaylist)
    }

    fun changePlaylistName(newName: String) {
        playlistInAddPlaylist.name = newName
    }

    fun transitionToAddPlaylistFromMenu(defaultPlaylist: MutableList<Song>, playlistName: String) {
        playlistInAddPlaylist.songs.clear()
        playlistInAddPlaylist.songs.addAll(defaultPlaylist)
        /*playlistInAddPlaylist.songs = defaultPlaylist*/
        playlistInAddPlaylist.name = playlistName
        playlistInAddPlaylist.updateSongSelectors()
    }

    fun transitionToAddPlaylistWithLongClick(defaultPlaylist: MutableList<Song>, playlistName: String, songSelected: Song) {
        playlistInAddPlaylist.songs.clear()
        playlistInAddPlaylist.songs.addAll(defaultPlaylist)
        playlistInAddPlaylist.name = playlistName
        val index = playlistInAddPlaylist.songs.indexOfFirst {
            it.artist == songSelected.artist && it.title == songSelected.title &&
                    it.duration == songSelected.duration
        }
        playlistInAddPlaylist.updateSongSelectors()
        if (index != -1) {
            playlistInAddPlaylist.songSelectors[index].isSelected = true
        }
    }

    fun getSelectedSongs(): List<Song> {
        return playlistInAddPlaylist.songSelectors.filter { it.isSelected }.map { it.song }
    }

    fun getSongSelectors(): MutableList<SongSelector> {
        return playlistInAddPlaylist.songSelectors
    }

    fun updateSongSelectors() {
        playlistInAddPlaylist.updateSongSelectors()
    }

    fun changeSongSelectors(newSongSelectors: MutableList<SongSelector>) {
        playlistInAddPlaylist.songSelectors = newSongSelectors
    }
}