package org.hyperskill.musicplayer.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/*
class PlaylistData(loadedPlaylist: MutableList<DataType.SongSelector>) {
    private val playlistLiveData = MutableLiveData(loadedPlaylist)

    // Adds songs to the playlist Live Data and posts them
    fun addSongs(songs: MutableList<DataType.SongSelector>) {
        val currentPlaylist = playlistLiveData.value
        if (currentPlaylist == null) {
            playlistLiveData.postValue(songs)
        } else {
            currentPlaylist.addAll(songs)
            playlistLiveData.postValue(currentPlaylist)
        }
    }

    // Removes songs from playlist Live Data and posts them
    fun removeSongs(songs: MutableList<DataType.SongSelector>) {
        val currentPlaylist = playlistLiveData.value
        if (currentPlaylist != null) {
            currentPlaylist.removeAll(songs)
            playlistLiveData.postValue(currentPlaylist)
        }
    }

    fun clearList() {
        playlistLiveData.value?.clear()
    }

    fun changeSelectedState(position: Int, state: Boolean) {
        val currentPlaylist = playlistLiveData.value
        if (currentPlaylist != null) {
            currentPlaylist[position].isSelected = state
            playlistLiveData.postValue(currentPlaylist)
        }
    }

    fun getSongByTitle(title: String): DataType.SongSelector? {
        playlistLiveData.value?.let { playlist ->
            return playlist.firstOrNull { it.song.title == title }
        }
        return null
    }

    fun getPlaylist(): LiveData<MutableList<DataType.SongSelector>> {
        return playlistLiveData
    }

}*/
