package org.hyperskill.musicplayer.viewModel

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.hyperskill.musicplayer.ViewState
import org.hyperskill.musicplayer.playlist.CurrentPlaylist
import org.hyperskill.musicplayer.playlist.PlaylistInAddPlaylist
import org.hyperskill.musicplayer.song.Song
import org.hyperskill.musicplayer.song.SongSelector
import org.hyperskill.musicplayer.repository.PlaylistsRepository
import org.hyperskill.musicplayer.repository.PlaylistsRepositoryImpl
import org.hyperskill.musicplayer.playlist.CurrentPlaylistController
import org.hyperskill.musicplayer.playlist.PlaylistInAddPlaylistController
import org.hyperskill.musicplayer.repository.PlaylistsRepositoryController
import org.hyperskill.musicplayer.song.SongState

private const val DEFAULT_PLAYLIST_NAME = "All Songs"
class MainActivityViewModel : ViewModel() {

    private val playlists: PlaylistsRepository = PlaylistsRepositoryImpl()
    private val playlistsRepositoryController = PlaylistsRepositoryController(playlists)
    private val currentPlaylist = CurrentPlaylist(DEFAULT_PLAYLIST_NAME, playlists.getDefaultSongs())
    private val currentPlaylistController = CurrentPlaylistController(currentPlaylist)
    private val playlistInAddPlaylist = PlaylistInAddPlaylist(DEFAULT_PLAYLIST_NAME, playlists.getDefaultSongs())
    private val playlistInAddPlaylistController = PlaylistInAddPlaylistController(playlistInAddPlaylist)

    private val _currentStateLiveData: MutableLiveData<ViewState> = MutableLiveData()
    val currentStateLiveData = _currentStateLiveData

    private val _currentPlaylistLiveData: MutableLiveData<List<Song>> = MutableLiveData()
    val currentPlaylistLiveData = _currentPlaylistLiveData

    private val _playlistInAddPlaylistLiveData: MutableLiveData<List<SongSelector>> = MutableLiveData()
    val playlistInAddPlaylistLiveData = _playlistInAddPlaylistLiveData

    private val _currentSongLiveData: MutableLiveData<Song> = MutableLiveData()
    val currentSongLiveData = _currentSongLiveData

    fun changeCurrentState(currentState: ViewState) {
        _currentStateLiveData.value = currentState
    }

    fun updateCurrentPlaylist() {
        _currentPlaylistLiveData.value = currentPlaylistController.getCurrentPlaylist().toList()
    }

    fun updatePlaylistInAddPlaylist() {
        _playlistInAddPlaylistLiveData.value = playlistInAddPlaylistController.getSongSelectors().toList()
    }

    fun changeCurrentSong(song: Song) {
        _currentSongLiveData.value = song
    }

    fun getDefaultPlaylist(): Collection<Song> {
        return playlistsRepositoryController.getDefaultSongs()
    }

    fun getCurrentPlaylist(): MutableList<Song> {
        return currentPlaylistController.getCurrentPlaylist()
    }

    fun getLoadedPlaylist(): PlaylistInAddPlaylist {
        return playlistInAddPlaylist
    }

    fun getAllPlaylistsNames(): List<String> {
        return playlistsRepositoryController.getAllPlaylists().map { it.key }.sorted()
    }

    fun modifySongInLoadedPlaylist(originalSong: SongSelector, modifiedSong: Song, position: Int) {
        playlistInAddPlaylistController.modifySongSelector(originalSong, modifiedSong, position)
    }

    fun updateSongSelectors() {
        playlistInAddPlaylistController.updateSongSelectors()
    }

    fun toAddPlaylistStateFromMenu(): Boolean {
        return if (playlistsRepositoryController.getPlaylistByName(DEFAULT_PLAYLIST_NAME) == null) {
            false
        } else {
            playlistInAddPlaylistController.transitionToAddPlaylistFromMenu(
                playlistsRepositoryController.getDefaultSongs(),
                DEFAULT_PLAYLIST_NAME
            )
            true
        }
    }

    fun toAddPlaylistStateWithLongClick(songSelected: Song) {
        playlistInAddPlaylistController.transitionToAddPlaylistWithLongClick(playlistsRepositoryController.getDefaultSongs(), DEFAULT_PLAYLIST_NAME, songSelected)
    }

    fun loadPlaylistInPlayMusicState(playlistsNames: List<String>, position: Int) {
        val savedPlaylists = playlistsRepositoryController.getAllPlaylists()
        val selectedPlaylistName = playlistsNames[position]
        val selectedPlaylist = savedPlaylists[selectedPlaylistName]!!.toMutableList()
        val currentSong = currentPlaylistController.getCurrentTrack()
        currentPlaylistController.changeCurrentPlaylist(selectedPlaylist)
        currentPlaylistController.changeCurrentPlaylistName(selectedPlaylistName)
        if (selectedPlaylist.any {
                it.title == currentSong.title && it.artist == currentSong.artist
                        && it.duration == currentSong.duration
            }) {
            val _currentSong = selectedPlaylist.find {
                it.artist == currentSong.artist && it.title == currentSong.title
                        && it.duration == currentSong.duration
            }!!
            _currentSong.songState = currentSong.songState
            currentPlaylistController.changeCurrentTrack(_currentSong)
        }
        updateCurrentPlaylist()
    }

    fun loadPlaylistInAddPlaylistState(playlistsNames: List<String>, position: Int) {
        val savedPlaylists = playlistsRepositoryController.getAllPlaylists()
        val selectedPlaylistName = playlistsNames[position]
        val selectedPlaylist = savedPlaylists[selectedPlaylistName]!!.toMutableList()
        val selectedSongs = playlistInAddPlaylistController.getSelectedSongs()
        val songSelectors = selectedPlaylist.map { SongSelector(it) }
        selectedSongs.forEach {
            for (songSelector in songSelectors) {
                if (it.artist == songSelector.song.artist &&
                    it.title == songSelector.song.title &&
                    it.duration == songSelector.song.duration) {
                    songSelector.isSelected = true
                }
            }
        }
        playlistInAddPlaylistController.changePlaylist(selectedPlaylist)
        playlistInAddPlaylistController.changePlaylistName(selectedPlaylistName)
        playlistInAddPlaylistController.changeSongSelectors(songSelectors.toMutableList())
        updatePlaylistInAddPlaylist()
    }

    fun deletePlaylist(name: String) {
        if (currentPlaylist.name == name) {
            currentPlaylistController.changeCurrentPlaylist(playlistsRepositoryController.getDefaultSongs())
            currentPlaylistController.changeCurrentPlaylistName(DEFAULT_PLAYLIST_NAME)
            updateCurrentPlaylist()
        }
        if (playlistInAddPlaylist.name == name) {
            playlistInAddPlaylistController.changePlaylist(playlistsRepositoryController.getDefaultSongs())
            playlistInAddPlaylistController.changePlaylistName(DEFAULT_PLAYLIST_NAME)
            playlistInAddPlaylist.updateSongSelectors()
            updatePlaylistInAddPlaylist()
        }
        playlistsRepositoryController.removePlaylist(name)
    }

    fun searchBtnClickInitial() {
        playlistsRepositoryController.addPlaylist(DEFAULT_PLAYLIST_NAME, playlistsRepositoryController.getDefaultSongs())
        currentPlaylistController.changeCurrentPlaylist(playlistsRepositoryController.getPlaylistByName(DEFAULT_PLAYLIST_NAME)!!)
        currentPlaylistController.changeCurrentPlaylistName(DEFAULT_PLAYLIST_NAME)
    }

    fun searchBtnClickPlayMusic() {
        val currentTrack = currentPlaylistController.getCurrentTrack()
        playlistsRepositoryController.addPlaylist(DEFAULT_PLAYLIST_NAME, playlistsRepositoryController.getDefaultSongs())
        currentPlaylistController.changeCurrentPlaylist(playlistsRepositoryController.getPlaylistByName(DEFAULT_PLAYLIST_NAME)!!)
        currentPlaylistController.changeCurrentPlaylistName(DEFAULT_PLAYLIST_NAME)
        if (playlistsRepositoryController.getPlaylistByName(DEFAULT_PLAYLIST_NAME)!!.any {
            it.artist == currentTrack.artist && it.title == currentTrack.title &&
                    it.duration == currentTrack.duration
        }) {
            val index = currentPlaylistController.getCurrentPlaylist().indexOfFirst {
                it.artist == currentTrack.artist && it.title == currentTrack.title &&
                        it.duration == currentTrack.duration
            }
            val newCurrentTrack = currentPlaylistController.getCurrentPlaylist()[index]
            currentPlaylistController.changeStateOfSong(currentTrack, index)
            currentPlaylistController.changeCurrentTrack(newCurrentTrack)
        }
        updateCurrentPlaylist()
    }

    fun searchBtnClickAddPlaylist() {
        playlistInAddPlaylistController.changePlaylist(playlistsRepositoryController.getPlaylistByName(
            DEFAULT_PLAYLIST_NAME)!!)
        playlistInAddPlaylistController.changePlaylistName(DEFAULT_PLAYLIST_NAME)
        updatePlaylistInAddPlaylist()
    }

    fun fragmentPlayPauseClick() {
        currentPlaylistController.playOrPauseTrackFromFragment()
        updateCurrentPlaylist()
    }

    fun fragmentStopClick() {
        val modifiedSong = currentPlaylistController.stopTrackFromFragment()
        changeCurrentSong(modifiedSong)
        updateCurrentPlaylist()
    }

    fun saveNewPlaylist(name: String) {
        val selectedSongs = playlistInAddPlaylistController.getSelectedSongs()
        playlistsRepositoryController.addPlaylist(name, selectedSongs)
    }

    fun songPlayPauseClick(song: Song, position: Int) {
        if (song != currentPlaylistController.getCurrentTrack()) {
            val oldCurrentSong = currentPlaylistController.getCurrentTrack()
            if (currentPlaylistController.getCurrentPlaylist().contains(oldCurrentSong)) {
                val modifiedOldCurrentSong = Song(
                    oldCurrentSong.id,
                    oldCurrentSong.title,
                    oldCurrentSong.artist,
                    oldCurrentSong.duration
                )
                val indexOfOldSong = currentPlaylistController.getCurrentPlaylist().indexOfFirst {
                    it.artist == oldCurrentSong.artist && it.title == oldCurrentSong.title
                            && it.duration == oldCurrentSong.duration
                }
                currentPlaylistController.modifySong(modifiedOldCurrentSong, indexOfOldSong)
            }

            val newSongRef = currentPlaylistController.getCurrentPlaylist()[position]
            val newSong = Song(
                newSongRef.id,
                newSongRef.title,
                newSongRef.artist,
                newSongRef.duration
            )
            currentPlaylistController.modifySong(newSong, position)
            currentPlaylistController.changeCurrentTrack(newSong)
            currentPlaylistController.play(newSong)
            changeCurrentSong(newSong)
            updateCurrentPlaylist()
        } else if (song == currentPlaylistController.getCurrentTrack()) {
            if (currentPlaylistController.getCurrentTrack().songState == SongState.PLAYING) {
                val modifiedSong = Song(
                    song.id, song.title, song.artist, song.duration
                )
                currentPlaylistController.modifySong(modifiedSong,position)
                currentPlaylistController.changeCurrentTrack(modifiedSong)
                currentPlaylistController.pause(modifiedSong)
                updateCurrentPlaylist()
            } else {
                val modifiedSong = Song(
                    song.id, song.title, song.artist, song.duration
                )
                currentPlaylistController.modifySong(modifiedSong, position)
                currentPlaylistController.changeCurrentTrack(modifiedSong)
                currentPlaylistController.play(modifiedSong)
                updateCurrentPlaylist()
            }
        }    }
}
