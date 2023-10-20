package org.hyperskill.musicplayer

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.app.AlertDialog
import androidx.collection.ArrayMap
import androidx.core.view.MenuProvider
import androidx.recyclerview.widget.LinearLayoutManager
import org.hyperskill.musicplayer.databinding.ActivityMainBinding
import org.hyperskill.musicplayer.model.Constants
import org.hyperskill.musicplayer.model.CurrentTrack
import org.hyperskill.musicplayer.model.DataType
import org.hyperskill.musicplayer.model.PlaylistType
import org.hyperskill.musicplayer.model.SongState
import org.hyperskill.musicplayer.model.ViewState
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var viewState: ViewState = ViewState.PLAY_MUSIC
    private val playlists: ArrayMap<String, PlaylistType.Playlist> = ArrayMap()
    private val defaultPlaylistName = "All Songs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.plant(Timber.DebugTree())
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.mainFragmentContainer, MainPlayerControllerFragment())
                .commit()
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val defaultSongs = Constants.getDefaultPlaylist()
        val currentPlaylist = PlaylistType.CurrentPlaylist(PlaylistType.Playlist(defaultPlaylistName, defaultSongs))
        val loadedPlaylist = PlaylistType.LoadedPlaylist(PlaylistType.Playlist(defaultPlaylistName, defaultSongs))

        val recyclerView = binding.mainSongList
        val adapter = RecyclerAdapterSong(viewState)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        setSupportActionBar(binding.toolbar)

        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.mainMenuAddPlaylist -> {
                        if (viewState == ViewState.ADD_PLAYLIST) return true
                        if (playlists[defaultPlaylistName] == null) {
                            Toast.makeText(
                                this@MainActivity,
                                "no songs loaded, click search to load songs",
                                Toast.LENGTH_LONG
                            ).show()
                            return true
                        } else {
                            viewState = ViewState.ADD_PLAYLIST
                            loadedPlaylist.playlist = playlists[defaultPlaylistName]!!
                            val newList = loadedPlaylist.songsAsSelectors
                            adapter.changeState(viewState)
                            adapter.submitList(newList.toMutableList())
                            Timber.log(1, "Add playlist menu item was clicked")
                            true
                        }
                    }
                    R.id.mainMenuLoadPlaylist -> {
                        alertDialogCreator("choose playlist to load")
                        Timber.log(1, "Load playlist menu item was clicked")
                        true
                    }
                    R.id.mainMenuDeletePlaylist -> {
                        alertDialogCreator("choose playlist to delete")
                        Timber.log(1, "Delete playlist menu item was clicked")
                        true
                    }
                    else -> false
                }
            }
        })

        binding.mainButtonSearch.setOnClickListener {
            val playlist: PlaylistType.Playlist
            when (viewState) {
                ViewState.PLAY_MUSIC -> {
                    if (!playlists.contains(defaultPlaylistName)) {
                        playlist = PlaylistType.Playlist(defaultPlaylistName, defaultSongs)
                        playlists[defaultPlaylistName] = playlist
                    }
                    val newList = playlists[defaultPlaylistName]!!.songs
                    currentPlaylist.playlist = playlists[defaultPlaylistName]!!
                    adapter.submitList(newList.toMutableList())
                }
                ViewState.ADD_PLAYLIST -> {
                    playlists[defaultPlaylistName] = PlaylistType.Playlist(defaultPlaylistName, defaultSongs)
                    loadedPlaylist.playlist = playlists[defaultPlaylistName]!!
                    adapter.submitList(loadedPlaylist.songsAsSelectors.toMutableList())
                }
            }
        }

        adapter.apply {

            setOnItemClickListener(
                object : RecyclerAdapterSong.OnItemClickListener {
                    override fun onClick(songSelected: DataType.SongSelector, position: Int) {
                        val modifiedList = currentList.toMutableList()
                        val modifiedSong = DataType.SongSelector(DataType.Song(
                            songSelected.song.id, songSelected.song.title, songSelected.song.artist,
                            songSelected.song.duration
                        ))
                        modifiedSong.isSelected = !songSelected.isSelected
                        modifiedList[position] = modifiedSong
                        submitList(modifiedList)
                    }
                }
            )

            setOnItemLongClickListener(
                object : RecyclerAdapterSong.OnItemLongClickListener {
                    override fun onLongClick(songSelected: DataType.Song, position: Int) {
                        val modifiedSong = DataType.Song(
                            songSelected.id, songSelected.title, songSelected.artist,
                            songSelected.duration
                        )
                        val modifiedSongSelector = DataType.SongSelector(modifiedSong)
                        modifiedSongSelector.isSelected = true
                        /*loadedPlaylist.songs[position] = modifiedSong*/
                        loadedPlaylist.songsAsSelectors[position] = modifiedSongSelector
                        viewState = ViewState.ADD_PLAYLIST
                        changeState(viewState)
                        submitList(null)
                        submitList(loadedPlaylist.songsAsSelectors.toMutableList())
                    }
                }
            )

            setOnButtonPlayPauseClickListener(
                object : RecyclerAdapterSong.OnButtonPlayPauseClickListener {
                    override fun onClick(song: DataType.Song, position: Int) {
                        if (currentPlaylist.currentTrack == null) {
                            val newList = currentPlaylist.songs.toTypedArray().copyOf()
                            newList[position].songState = SongState.PLAYED
                            currentPlaylist.currentTrack = CurrentTrack(newList[position])
                            submitList(newList.toMutableList())
                        } else if (song != currentPlaylist.currentTrack!!.song) {
                            val oldCurrentSong = DataType.Song(currentPlaylist.currentTrack!!.song.id, currentPlaylist.currentTrack!!.song.title, currentPlaylist.currentTrack!!.song.artist, currentPlaylist.currentTrack!!.song.duration)
                            val indexOfOldSong = currentList.indexOf(currentPlaylist.currentTrack!!.song)
                            val oldCurrentSongRef = currentList[indexOfOldSong] as DataType.Song
                            oldCurrentSong.songState = SongState.STOPPED
                            notifyItemChanged(
                                indexOfOldSong,
                                DataTypeDiffCallbackObj.getChangePayload(oldCurrentSongRef, oldCurrentSong)
                            )
                            val newCurrentTrackRef = currentList[position] as DataType.Song
                            val newCurrentTrack = CurrentTrack(DataType.Song(newCurrentTrackRef.id, newCurrentTrackRef.title, newCurrentTrackRef.artist, newCurrentTrackRef.duration))
                            currentPlaylist.currentTrack = newCurrentTrack
                            newCurrentTrack.play()
                            notifyItemChanged(
                                position,
                                DataTypeDiffCallbackObj.getChangePayload(newCurrentTrackRef, newCurrentTrack.song)
                            )
                        } else {
                            if (currentPlaylist.currentTrack!!.state == SongState.PLAYED) {
                                currentPlaylist.currentTrack!!.pause()
                                notifyItemChanged(
                                    position,
                                    DataTypeDiffCallbackObj.getChangePayload(song, currentPlaylist.currentTrack!!.song))
                            } else {
                                currentPlaylist.currentTrack!!.play()
                                notifyItemChanged(
                                    position,
                                    DataTypeDiffCallbackObj.getChangePayload(song, currentPlaylist.currentTrack!!.song))
                            }
                        }
                    }
                }
            )

        }
    }
    private fun alertDialogCreator(title: String): AlertDialog {
        return AlertDialog.Builder(this)
            .setTitle(title)
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

}

