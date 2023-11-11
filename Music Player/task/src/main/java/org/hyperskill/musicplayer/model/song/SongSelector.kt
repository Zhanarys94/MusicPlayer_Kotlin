package org.hyperskill.musicplayer.model.song

class SongSelector(val song: Song) : SongType() {
    var isSelected = false

    override fun hashCode(): Int {
        return song.hashCode() + isSelected.hashCode()
    }

    override fun toString(): String {
        return "Artist: ${song.artist}\nTitle: ${song.title}\nDuration: ${song.durationString}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SongSelector) return false

        if (song.id != other.song.id) return false
        if (song.artist != other.song.artist) return false
        if (song.title != other.song.title) return false
        if (song.duration != other.song.duration) return false
        if (isSelected != other.isSelected) return false

        return true
    }
}