package org.hyperskill.musicplayer.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.hyperskill.musicplayer.model.song.Song

class SongViewModel : ViewModel() {
    private val _currentSong: MutableLiveData<Song> = MutableLiveData()
    val currentSong = _currentSong

    fun changeCurrentSong(song: Song) {
        _currentSong.value = song
    }
}