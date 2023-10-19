package org.hyperskill.musicplayer.internals

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.provider.MediaStore
// version 1.3
class FakeContentProvider : ContentProvider() {

    companion object {
        var fakeSongResult: List<SongFake> = listOf()
        var hasPermissionToReadExternalStorage = true
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {

        if(hasPermissionToReadExternalStorage.not()) {
            throw AssertionError(
                "You are trying to read from external storage, " +
                        "but you don't have permission to read from external storage"
            )
        }

        val columns = listOf (
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
        )

        return if (uri == MediaStore.Audio.Media.EXTERNAL_CONTENT_URI) {
            val cursor = MatrixCursor(
                columns.filter { projection?.contains(it) ?: true }
                    .sortedBy { projection?.indexOf(it) ?: columns.indexOf(it)}
                    .toTypedArray()
            )
            addMockRows(cursor, projection)
            cursor
        } else {
            null
        }
    }

    private fun addMockRows(cursor: MatrixCursor, projection: Array<out String>?) {
        for (song in fakeSongResult) {
            val rowBuilder = cursor.newRow()
            if (projection?.contains(MediaStore.Audio.Media._ID) != false) {  // if (true || null) then add
                rowBuilder.add(MediaStore.Audio.Media._ID, song.id)
            }
            if (projection?.contains(MediaStore.Audio.Media.ARTIST) != false) {
                rowBuilder.add(MediaStore.Audio.Media.ARTIST, song.artist)
            }
            if (projection?.contains(MediaStore.Audio.Media.TITLE) != false) {
                rowBuilder.add(MediaStore.Audio.Media.TITLE, song.title)
            }
            if (projection?.contains(MediaStore.Audio.Media.DURATION) != false) {
                rowBuilder.add(MediaStore.Audio.Media.DURATION, song.duration)
            }
        }
    }

    override fun getType(uri: Uri): String? {
        throw NotImplementedError()
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw NotImplementedError()
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        throw NotImplementedError()
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        throw NotImplementedError()
    }
}