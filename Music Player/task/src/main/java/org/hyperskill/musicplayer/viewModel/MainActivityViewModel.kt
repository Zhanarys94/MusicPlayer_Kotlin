package org.hyperskill.musicplayer.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.hyperskill.musicplayer.model.ViewState
import org.hyperskill.musicplayer.model.song.Song
import org.hyperskill.musicplayer.model.song.SongSelector

class MainActivityViewModel : ViewModel() {

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

    fun changeLoadedPlaylist(songs: Collection<SongSelector>) {
        _loadedPlaylistLiveData.value = songs.toList()
    }

    fun changeCurrentSong(song: Song) {
        _currentSong.value = song
    }

}
