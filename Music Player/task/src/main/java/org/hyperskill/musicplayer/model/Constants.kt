package org.hyperskill.musicplayer.model

object Constants {
    fun getDefaultPlaylist(): List<DataType.Song> {
        val list = mutableListOf<DataType.Song>()
        for (i in 1..10) {
            list.add(
                DataType.Song(i, "title$i", "artist$i", 215_000)
            )
        }
        return list.toList()
    }
}