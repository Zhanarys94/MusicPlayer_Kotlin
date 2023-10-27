package org.hyperskill.musicplayer.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.hyperskill.musicplayer.model.song.SongType

class SongViewModel : ViewModel() {
    private val _currentSong: MutableLiveData<SongType.Song> = MutableLiveData()
    val currentSong = _currentSong

    fun changeCurrentSong(song: SongType.Song) {
        _currentSong.value = song
    }
}