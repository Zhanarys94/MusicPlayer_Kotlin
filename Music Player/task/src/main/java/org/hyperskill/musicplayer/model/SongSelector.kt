package org.hyperskill.musicplayer.model

import androidx.recyclerview.widget.DiffUtil

sealed class DataType {
    class Song(
        val id: Int,
        val title: String,
        val artist: String,
        val duration: Int
    ) : java.io.Serializable, DataType() {
        val durationString: String
            get() {
                val durationSec = (duration / 1000)
                val mins = durationSec / 60
                val sec = durationSec % 60
                return String.format("%02d:%02d", mins, sec)
            }

        var songState = SongState.STOPPED

        override fun hashCode(): Int {
            return this.artist.hashCode() + this.title.hashCode() + this.duration.hashCode() +
                    this.songState.hashCode()
        }

        override fun toString(): String {
            return "Artist: ${this.artist}\nTitle: ${this.title}\nDuration: ${this.durationString}"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Song) return false

            if (this.artist != other.artist) return false
            if (this.title != other.title) return false
            if (this.duration != other.duration) return false
            if (this.songState != other.songState) return false

            return true
        }
    }

    class SongSelector(val song: Song) : DataType() {
        var isSelected = false

        override fun hashCode(): Int {
            return song.artist.hashCode() + song.title.hashCode() + song.duration.hashCode() +
                    isSelected.hashCode()
        }

        override fun toString(): String {
            return "Artist: ${song.artist}\nTitle: ${song.title}\nDuration: ${song.durationString}"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is SongSelector) return false

            if (song.artist != other.song.artist) return false
            if (song.title != other.song.title) return false
            if (song.duration != other.song.duration) return false
            if (isSelected != other.isSelected) return false

            return true
        }
    }
}

fun songsToSelectors(songs: List<DataType.Song>): MutableList<DataType.SongSelector> {
    return songs.map { DataType.SongSelector(it) }.toMutableList()
}