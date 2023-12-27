package org.hyperskill.musicplayer.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.hyperskill.musicplayer.ViewState
import org.hyperskill.musicplayer.playlist.CurrentPlaylist
import org.hyperskill.musicplayer.playlist.LoadedPlaylist
import org.hyperskill.musicplayer.song.Song
import org.hyperskill.musicplayer.song.SongSelector
import org.hyperskill.musicplayer.repository.PlaylistsRepository
import org.hyperskill.musicplayer.repository.PlaylistsRepositoryImpl
import org.hyperskill.musicplayer.playlist.CurrentPlaylistController
import org.hyperskill.musicplayer.playlist.LoadedPlaylistController
import org.hyperskill.musicplayer.repository.PlaylistsRepositoryController

private const val DEFAULT_PLAYLIST_NAME = "All Songs"
class MainActivityViewModel : ViewModel() {

    private val playlists: PlaylistsRepository = PlaylistsRepositoryImpl()
    private val playlistsRepositoryController = PlaylistsRepositoryController(playlists)
    val currentPlaylist = CurrentPlaylist(DEFAULT_PLAYLIST_NAME, playlists.getDefaultSongs())
    private val currentPlaylistController = CurrentPlaylistController(currentPlaylist)
    val loadedPlaylist = LoadedPlaylist(DEFAULT_PLAYLIST_NAME, playlists.getDefaultSongs())
    private val loadedPlaylistController = LoadedPlaylistController(loadedPlaylist)

    private val _currentStateLiveData: MutableLiveData<ViewState> = MutableLiveData()
    val currentStateLiveData = _currentStateLiveData

    private val _currentPlaylistLiveData: MutableLiveData<List<Song>> = MutableLiveData()
    val currentPlaylistLiveData = _currentPlaylistLiveData

    private val _loadedPlaylistLiveData: MutableLiveData<List<SongSelector>> = MutableLiveData()
    val loadedPlaylistLiveData = _loadedPlaylistLiveData

    private val _currentSong: MutableLiveData<Song> = MutableLiveData()
    val currentSong = _currentSong

    fun changeCurrentState(currentState: ViewState) {
        _currentStateLiveData.value = currentState
    }

    fun changeCurrentPlaylist(songs: Collection<Song>) {
        _currentPlaylistLiveData.value = songs.toList()
    }

    fun changeLoadedPlaylist() {
        _loadedPlaylistLiveData.value = loadedPlaylist.songSelectors.toList()
    }

    fun changeCurrentSong(song: Song) {
        _currentSong.value = song
    }

    fun getDefaultPlaylist(): Collection<Song> {
        return playlistsRepositoryController.getDefaultSongs()
    }

    fun modifySongInCurrentPlaylist(newSong: Song, position: Int) {
        currentPlaylist.songs[position] = newSong
    }

    fun modifySongInLoadedPlaylist(originalSong: SongSelector, modifiedSong: Song, position: Int) {
        loadedPlaylistController.modifySongSelector(originalSong, modifiedSong, position)
    }

    fun toAddPlaylistStateWithLongClick(songSelected: Song) {
        loadedPlaylistController.transitionToAddPlaylistWithLongClick(playlistsRepositoryController.getDefaultSongs(), DEFAULT_PLAYLIST_NAME, songSelected)
    }

    fun fragmentPlayPauseClick() {
        currentPlaylistController.playOrPauseTrackFromFragment()
        changeCurrentPlaylist(currentPlaylist.songs)
    }

    fun fragmentStopClick() {
        val modifiedSong = currentPlaylistController.stopTrackFromFragment()
        changeCurrentSong(modifiedSong)
        changeCurrentPlaylist(currentPlaylist.songs)
    }

    fun saveNewPlaylist(name: String) {
        val selectedSongs = loadedPlaylistController.getSelectedSongs()
        playlistsRepositoryController.addPlaylist(name, selectedSongs)
    }
}
