package org.hyperskill.musicplayer.model.song

class Song(
    val id: Int,
    var title: String,
    var artist: String,
    val duration: Int
) : java.io.Serializable, SongType() {
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