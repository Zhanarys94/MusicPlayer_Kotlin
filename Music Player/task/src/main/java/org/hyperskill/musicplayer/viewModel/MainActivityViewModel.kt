package org.hyperskill.musicplayer.viewModel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.hyperskill.musicplayer.model.DataType
import org.hyperskill.musicplayer.model.ViewState

/*class MainActivityViewModel : ViewModel() {

    private val _viewState = MutableStateFlow(ViewState.PLAY_MUSIC)
    val viewState = _viewState.asStateFlow()

    private val defaultSongs = buildList<DataType.Song> {
        for (i in 1..10) {
            add(
                DataType.Song(i, "title$i", "artist$i", 215_000)
            )
        }
    }

    val currentPlaylistStateFlow = MutableStateFlow(defaultSongs)

    val loadedPlaylistStateFlow = MutableStateFlow(songsToSelectors(defaultSongs))

    fun changeIsSelectedState(songSelector: DataType.SongSelector) {
        val loadedPlaylist = loadedPlaylistStateFlow.value
        loadedPlaylist[loadedPlaylist.indexOf(songSelector)].isSelected = !songSelector.isSelected
    }

    fun changeSongState(song: DataType.Song) {
        val currentPlaylist = currentPlaylistStateFlow.value
        currentPlaylist[currentPlaylist.indexOf(song)].songState = song.songState
    }
}*/
