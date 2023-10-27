package org.hyperskill.musicplayer

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.app.AlertDialog
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.view.MenuProvider
import androidx.recyclerview.widget.LinearLayoutManager
import org.hyperskill.musicplayer.databinding.ActivityMainBinding
import org.hyperskill.musicplayer.model.playlist.CurrentPlaylist
import org.hyperskill.musicplayer.model.song.SongType
import org.hyperskill.musicplayer.model.playlist.LoadedPlaylist
import org.hyperskill.musicplayer.model.playlist.PlaylistsRepositoryImpl
import org.hyperskill.musicplayer.model.song.SongState
import org.hyperskill.musicplayer.model.ViewState
import org.hyperskill.musicplayer.viewModel.MainAddPlaylistFragment
import org.hyperskill.musicplayer.viewModel.MainPlayerControllerFragment
import org.hyperskill.musicplayer.viewModel.RecyclerAdapterSong
import org.hyperskill.musicplayer.viewModel.SongStateController
import org.hyperskill.musicplayer.viewModel.SongViewModel
import timber.log.Timber

private const val DEFAULT_PLAYLIST_NAME = "All Songs"

class MainActivity : AppCompatActivity(), MainPlayerControllerFragment.OnFragmentInteractionListener {

    private lateinit var binding: ActivityMainBinding
    private var viewState: ViewState = ViewState.PLAY_MUSIC
    private val playlists = PlaylistsRepositoryImpl()
    private val defaultSongs = playlists.getDefaultSongs()
    private val currentPlaylist = CurrentPlaylist(defaultSongs)
    private val loadedPlaylist = LoadedPlaylist(defaultSongs)
    private val adapter = RecyclerAdapterSong(viewState)
    private val currentSongViewModel: SongViewModel by viewModels()
    private val songStateController = SongStateController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.plant(Timber.DebugTree())

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.mainFragmentContainer, MainPlayerControllerFragment(), "PlayerController")
                .commit()
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recyclerView = binding.mainSongList
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        setSupportActionBar(binding.toolbar)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when (viewState) {
                    ViewState.PLAY_MUSIC -> onBackPressed()
                    ViewState.ADD_PLAYLIST -> {
                        viewState = ViewState.PLAY_MUSIC
                        adapter.changeState(viewState)
                        supportFragmentManager.popBackStack()
                        adapter.submitList(currentPlaylist.songs.toList())
                    }
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.mainMenuAddPlaylist -> {
                        if (viewState == ViewState.ADD_PLAYLIST) return true
                        if (playlists.getPlaylist(DEFAULT_PLAYLIST_NAME) == null) {
                            Toast.makeText(
                                this@MainActivity,
                                "no songs loaded, click search to load songs",
                                Toast.LENGTH_LONG
                            ).show()
                            return true
                        } else {
                            viewState = ViewState.ADD_PLAYLIST
                            loadedPlaylist.songs = playlists.getPlaylist(DEFAULT_PLAYLIST_NAME)!!.toMutableList()
                            loadedPlaylist.updateSongSelectors()
                            val newList = loadedPlaylist.songSelectors
                            adapter.changeState(viewState)
                            adapter.submitList(newList.toList())
                            supportFragmentManager.beginTransaction()
                                .setReorderingAllowed(true)
                                .replace(R.id.mainFragmentContainer, MainAddPlaylistFragment(),"AddPlaylist")
                                .addToBackStack(null)
                                .commit()
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
            when (viewState) {
                ViewState.PLAY_MUSIC -> {
                    if (playlists.getPlaylist(DEFAULT_PLAYLIST_NAME) == null) {
                        playlists.addPlaylist(DEFAULT_PLAYLIST_NAME, defaultSongs)
                        currentPlaylist.songs = defaultSongs
                        adapter.submitList(currentPlaylist.songs.toList())
                    } else {
                        currentPlaylist.songs = playlists.getPlaylist(DEFAULT_PLAYLIST_NAME)!!.toMutableList()
                        adapter.submitList(currentPlaylist.songs.toList())
                    }
                }
                ViewState.ADD_PLAYLIST -> {
                    playlists.removePlaylist(DEFAULT_PLAYLIST_NAME)
                    playlists.addPlaylist(DEFAULT_PLAYLIST_NAME, playlists.getDefaultSongs())
                    loadedPlaylist.songs = playlists.getPlaylist(DEFAULT_PLAYLIST_NAME)!!.toMutableList()
                    loadedPlaylist.updateSongSelectors()
                    adapter.submitList(loadedPlaylist.songSelectors.toList())
                }
            }
        }

        adapter.apply {

            setOnItemClickListener(
                object : RecyclerAdapterSong.OnItemClickListener {
                    override fun onClick(songSelected: SongType.SongSelector, position: Int) {
                        val modifiedSong = SongType.Song(
                            songSelected.song.id, songSelected.song.title, songSelected.song.artist,
                            songSelected.song.duration
                        )
                        loadedPlaylist.songs[position] = modifiedSong
                        loadedPlaylist.songSelectors[position] = SongType.SongSelector(loadedPlaylist.songs[position])
                        loadedPlaylist.songSelectors[position].isSelected = !songSelected.isSelected
                        submitList(loadedPlaylist.songSelectors.toList())
                    }
                }
            )

            setOnItemLongClickListener(
                object : RecyclerAdapterSong.OnItemLongClickListener {
                    override fun onLongClick(songSelected: SongType.Song, position: Int) {
                        val modifiedSong = SongType.Song(
                            songSelected.id, songSelected.title, songSelected.artist,
                            songSelected.duration
                        )
                        loadedPlaylist.songs[position] = modifiedSong
                        loadedPlaylist.songSelectors[position] = SongType.SongSelector(loadedPlaylist.songs[position])
                        loadedPlaylist.songSelectors[position].isSelected = true
                        viewState = ViewState.ADD_PLAYLIST
                        changeState(viewState)
                        submitList(null)
                        submitList(loadedPlaylist.songSelectors.toList())
                        supportFragmentManager.beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.mainFragmentContainer, MainAddPlaylistFragment())
                            .addToBackStack(null)
                            .commit()
                    }
                }
            )

            setOnButtonPlayPauseClickListener(
                object : RecyclerAdapterSong.OnButtonPlayPauseClickListener {
                    override fun onClick(song: SongType.Song, position: Int) {
                        if (song != currentPlaylist.currentTrack) {
                            val oldCurrentSong = currentPlaylist.currentTrack
                            val modifiedOldCurrentSong = SongType.Song(
                                oldCurrentSong.id,
                                oldCurrentSong.title,
                                oldCurrentSong.artist,
                                oldCurrentSong.duration
                            )
                            songStateController.stop(modifiedOldCurrentSong)

                            val indexOfOldSong = currentPlaylist.songs.indexOf(oldCurrentSong)
                            currentPlaylist.songs[indexOfOldSong] = modifiedOldCurrentSong

                            val newSongRef = currentPlaylist.songs[position]
                            val newSong = SongType.Song(
                                newSongRef.id,
                                newSongRef.title,
                                newSongRef.artist,
                                newSongRef.duration
                            )
                            currentPlaylist.songs[position] = newSong
                            currentPlaylist.currentTrack = currentPlaylist.songs[position]
                            songStateController.play(currentPlaylist.currentTrack)
                            currentSongViewModel.changeCurrentSong(currentPlaylist.currentTrack)
                            submitList(currentPlaylist.songs.toList())
                        } else if (song == currentPlaylist.currentTrack) {
                            if (currentPlaylist.currentTrack.songState == SongState.PLAYING) {
                                val modifiedSong = SongType.Song(
                                    song.id, song.title, song.artist, song.duration
                                )
                                currentPlaylist.songs[position] = modifiedSong
                                currentPlaylist.currentTrack = currentPlaylist.songs[position]
                                songStateController.pause(currentPlaylist.currentTrack)
                                submitList(currentPlaylist.songs.toList())
                            } else {
                                val modifiedSong = SongType.Song(
                                    song.id, song.title, song.artist, song.duration
                                )
                                currentPlaylist.songs[position] = modifiedSong
                                currentPlaylist.currentTrack = currentPlaylist.songs[position]
                                songStateController.play(currentPlaylist.currentTrack)
                                submitList(currentPlaylist.songs.toList())
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

    override fun onPlayPauseButtonClick() {
        if (currentPlaylist.currentTrack.songState == SongState.PLAYING) {
            val currentSong = currentPlaylist.currentTrack
            val modifiedSong = SongType.Song(
                currentSong.id, currentSong.title, currentSong.artist, currentSong.duration
            )
            val index = currentPlaylist.songs.indexOf(currentSong)
            currentPlaylist.songs[index] = modifiedSong
            currentPlaylist.currentTrack = currentPlaylist.songs[index]
            songStateController.pause(currentPlaylist.currentTrack)
            adapter.submitList(currentPlaylist.songs.toList())
        } else {
            val currentSong = currentPlaylist.currentTrack
            val modifiedSong = SongType.Song(
                currentSong.id, currentSong.title, currentSong.artist, currentSong.duration
            )
            val index = currentPlaylist.songs.indexOf(currentSong)
            currentPlaylist.songs[index] = modifiedSong
            currentPlaylist.currentTrack = currentPlaylist.songs[index]
            songStateController.play(currentPlaylist.currentTrack)
            adapter.submitList(currentPlaylist.songs.toList())
        }
    }

    override fun onStopButtonClick() {
        val currentSong = currentPlaylist.currentTrack
        val modifiedSong = SongType.Song(
            currentSong.id, currentSong.title, currentSong.artist, currentSong.duration
        )
        val index = currentPlaylist.songs.indexOf(currentSong)
        currentPlaylist.songs[index] = modifiedSong
        currentPlaylist.currentTrack = currentPlaylist.songs[index]
        songStateController.stop(currentPlaylist.currentTrack)
        currentSongViewModel.changeCurrentSong(modifiedSong)
        adapter.submitList(currentPlaylist.songs.toList())
    }
}

