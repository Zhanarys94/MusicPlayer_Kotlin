package org.hyperskill.musicplayer.playlist

import org.hyperskill.musicplayer.song.Song
import org.hyperskill.musicplayer.song.SongState

class CurrentPlaylistController(private val currentPlaylist: CurrentPlaylist) :
    CurrentPlaylistControllerInterface {

    override fun play(song: Song) {
        song.songState = SongState.PLAYING
    }

    override fun pause(song: Song) {
        song.songState = SongState.PAUSED
    }

    override fun stop(song: Song) {
        song.songState = SongState.STOPPED
    }

    fun playOrPauseTrackFromFragment() {
        if (currentPlaylist.currentTrack == null) {
            currentPlaylist.currentTrack = currentPlaylist.songs.first()
        }
        if (currentPlaylist.currentTrack!!.songState == SongState.PLAYING) {
            val isCurrentTrackHere = currentPlaylist.songs.contains(currentPlaylist.currentTrack!!)
            val currentSong = currentPlaylist.currentTrack
            if (isCurrentTrackHere) {
                val index = currentPlaylist.songs.indexOf(currentSong)
                val modifiedSong = Song(
                    currentSong!!.id, currentSong.title, currentSong.artist, currentSong.duration
                )
                currentPlaylist.songs[index] = modifiedSong
                currentPlaylist.currentTrack = modifiedSong
                pause(currentPlaylist.currentTrack!!)
            } else {
                val firstSong = currentPlaylist.songs.first()
                val modifiedSong = Song(
                    firstSong.id, firstSong.title, firstSong.artist, firstSong.duration
                )
                currentPlaylist.songs[0] = modifiedSong
                currentPlaylist.currentTrack = modifiedSong
                play(currentPlaylist.currentTrack!!)
            }
        } else {
            val isCurrentTrackHere = currentPlaylist.songs.contains(currentPlaylist.currentTrack!!)
            val currentSong = currentPlaylist.currentTrack
            if (isCurrentTrackHere) {
                val index = currentPlaylist.songs.indexOf(currentSong)
                val modifiedSong = Song(
                    currentSong!!.id, currentSong.title, currentSong.artist, currentSong.duration
                )
                currentPlaylist.songs[index] = modifiedSong
                currentPlaylist.currentTrack = modifiedSong
            } else {
                val firstSong = currentPlaylist.songs.first()
                val modifiedSong = Song(
                    firstSong.id, firstSong.title, firstSong.artist, firstSong.duration
                )
                currentPlaylist.songs[0] = modifiedSong
                currentPlaylist.currentTrack = modifiedSong
            }
            play(currentPlaylist.currentTrack!!)
        }
    }

    fun stopTrackFromFragment(): Song {
        if (currentPlaylist.currentTrack == null) {
            currentPlaylist.currentTrack = currentPlaylist.songs.first()
        }
        val currentSong = currentPlaylist.currentTrack
        val modifiedSong = Song(
            currentSong!!.id, currentSong.title, currentSong.artist, currentSong.duration
        )
        val index = currentPlaylist.songs.indexOf(currentSong)
        currentPlaylist.songs[index] = modifiedSong
        currentPlaylist.currentTrack = modifiedSong
        stop(modifiedSong)
        return modifiedSong
    }
}