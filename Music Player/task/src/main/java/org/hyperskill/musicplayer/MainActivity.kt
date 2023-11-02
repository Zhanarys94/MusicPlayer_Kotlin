package org.hyperskill.musicplayer

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.recyclerview.widget.LinearLayoutManager
import org.hyperskill.musicplayer.databinding.ActivityMainBinding
import org.hyperskill.musicplayer.model.ViewState.ADD_PLAYLIST
import org.hyperskill.musicplayer.model.ViewState.PLAY_MUSIC
import org.hyperskill.musicplayer.model.playlist.CurrentPlaylist
import org.hyperskill.musicplayer.model.playlist.LoadedPlaylist
import org.hyperskill.musicplayer.model.song.Song
import org.hyperskill.musicplayer.model.song.SongSelector
import org.hyperskill.musicplayer.model.song.SongState
import org.hyperskill.musicplayer.repository.PlaylistsRepositoryImpl
import org.hyperskill.musicplayer.viewModel.MainActivityViewModel
import org.hyperskill.musicplayer.viewModel.MainAddPlaylistFragment
import org.hyperskill.musicplayer.viewModel.MainPlayerControllerFragment
import org.hyperskill.musicplayer.viewModel.songStateController.SongStateController
import timber.log.Timber

private const val DEFAULT_PLAYLIST_NAME = "All Songs"

class MainActivity : AppCompatActivity(),
    MainPlayerControllerFragment.OnFragmentInteractionListener,
    MainAddPlaylistFragment.AddPlaylistFragmentInteractionListener
{
    private lateinit var binding: ActivityMainBinding
    private val playlists = PlaylistsRepositoryImpl()
    private val currentPlaylist = CurrentPlaylist(playlists.getDefaultSongs())
    private val loadedPlaylist = LoadedPlaylist(playlists.getDefaultSongs())
    private val mainActivityViewModel: MainActivityViewModel by viewModels()
    private val adapter = RecyclerAdapterSong()
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

        mainActivityViewModel.currentPlaylistLiveData.observe(this) { songs ->
            if (mainActivityViewModel.currentStateLiveData.value == PLAY_MUSIC) {
                (recyclerView.adapter as RecyclerAdapterSong).submitList(songs)
            }
        }
        mainActivityViewModel.loadedPlaylistLiveData.observe(this) { songSelectors ->
            if (mainActivityViewModel.currentStateLiveData.value == ADD_PLAYLIST) {
                (recyclerView.adapter as RecyclerAdapterSong).submitList(songSelectors)
            }
        }
        mainActivityViewModel.currentStateLiveData.observe(this) {state ->
            if (state == PLAY_MUSIC) {
                (recyclerView.adapter as RecyclerAdapterSong).submitList(currentPlaylist.songs.toList())
                supportFragmentManager.popBackStack("AddPlaylist", 1)
            } else {
                (recyclerView.adapter as RecyclerAdapterSong).submitList(loadedPlaylist.songSelectors.toList())
                supportFragmentManager.beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.mainFragmentContainer, MainAddPlaylistFragment(),"AddPlaylist")
                    .addToBackStack("AddPlaylist")
                    .commit()

                onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (mainActivityViewModel.currentStateLiveData.value == ADD_PLAYLIST) {
                            mainActivityViewModel.changeCurrentState(PLAY_MUSIC)
                        }
                        supportFragmentManager.popBackStack()
                        isEnabled = false
                    }
                }
                )
            }
        }

        setSupportActionBar(binding.toolbar)

        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.mainMenuAddPlaylist -> {
                        if (mainActivityViewModel.currentStateLiveData.value == ADD_PLAYLIST) return true
                        if (playlists.getPlaylistByName(DEFAULT_PLAYLIST_NAME) == null) {
                            Toast.makeText(
                                this@MainActivity,
                                "no songs loaded, click search to load songs",
                                Toast.LENGTH_LONG
                            ).show()
                            return true
                        } else {
                            loadedPlaylist.songs = playlists.getPlaylistByName(DEFAULT_PLAYLIST_NAME)!!
                            loadedPlaylist.updateSongSelectors()
                            mainActivityViewModel.changeCurrentState(ADD_PLAYLIST)
                            Timber.log(1, "Add playlist menu item was clicked")
                            true
                        }
                    }
                    R.id.mainMenuLoadPlaylist -> {
                        val savedPlaylists = playlists.getAllPlaylists()
                        val playlistsNames = savedPlaylists.map { it.key }
                        val clickListener = DialogInterface.OnClickListener { _, p1 ->
                            val selectedPlaylistName = savedPlaylists.keyAt(p1)
                            val selectedPlaylist = savedPlaylists[selectedPlaylistName]!!.toMutableList()
                            if (mainActivityViewModel.currentStateLiveData.value == PLAY_MUSIC) {
                                val currentSong = currentPlaylist.currentTrack
                                currentPlaylist.songs = selectedPlaylist
                                if (selectedPlaylist.any {
                                        it.title == currentSong.title && it.artist == currentSong.artist
                                                && it.duration == currentSong.duration
                                    }) {
                                    val _currentSong = selectedPlaylist.find {
                                        it.artist == currentSong.artist && it.title == currentSong.title
                                                && it.duration == currentSong.duration
                                    }!!
                                    _currentSong.songState = currentSong.songState
                                    currentPlaylist.currentTrack = _currentSong
                                }
                                mainActivityViewModel.changeCurrentPlaylist(currentPlaylist.songs)
                            } else {
                                val selectedSongs = loadedPlaylist.songSelectors.filter { it.isSelected }
                                val songSelectors = selectedPlaylist.map { SongSelector(it) }
                                selectedSongs.forEach {
                                    for (songSelector in songSelectors) {
                                        if (it.song.artist == songSelector.song.artist &&
                                            it.song.title == songSelector.song.title &&
                                            it.song.duration == songSelector.song.duration) {
                                            songSelector.isSelected = true
                                        }
                                    }
                                }
                                loadedPlaylist.songs = selectedPlaylist
                                loadedPlaylist.songSelectors = songSelectors.toMutableList()
                                mainActivityViewModel.changeLoadedPlaylist(loadedPlaylist.songSelectors)
                            }
                        }
                        alertDialogCreator("choose playlist to load", playlistsNames, clickListener)
                        Timber.log(1, "Load playlist menu item was clicked")
                        true
                    }
                    R.id.mainMenuDeletePlaylist -> {
                        /*alertDialogCreator("choose playlist to delete")*/
                        Timber.log(1, "Delete playlist menu item was clicked")
                        true
                    }
                    else -> false
                }
            }
        })

        binding.mainButtonSearch.setOnClickListener {
            when (mainActivityViewModel.currentStateLiveData.value) {
                PLAY_MUSIC -> {
                    if (playlists.getPlaylistByName(DEFAULT_PLAYLIST_NAME) == null) {
                        playlists.addPlaylist(DEFAULT_PLAYLIST_NAME, playlists.getDefaultSongs())
                        currentPlaylist.songs = playlists.getPlaylistByName(DEFAULT_PLAYLIST_NAME)!!
                        mainActivityViewModel.changeCurrentPlaylist(currentPlaylist.songs)
                    } else {
                        val currentTrack = currentPlaylist.currentTrack
                        playlists.addPlaylist(DEFAULT_PLAYLIST_NAME, playlists.getDefaultSongs())
                        currentPlaylist.songs = playlists.getPlaylistByName(DEFAULT_PLAYLIST_NAME)!!
                        if (playlists.getPlaylistByName(DEFAULT_PLAYLIST_NAME)!!.any {
                                it.artist == currentTrack.artist && it.title == currentTrack.title &&
                                        it.duration == currentTrack.duration
                        }) {
                            val index = currentPlaylist.songs.indexOfFirst {
                                it.artist == currentTrack.artist && it.title == currentTrack.title &&
                                        it.duration == currentTrack.duration
                            }
                            currentPlaylist.songs[index].songState = currentTrack.songState
                            currentPlaylist.currentTrack = currentPlaylist.songs[index]
                        }
                        mainActivityViewModel.changeCurrentPlaylist(currentPlaylist.songs)
                    }
                }
                ADD_PLAYLIST -> {
                    loadedPlaylist.songs = playlists.getPlaylistByName(DEFAULT_PLAYLIST_NAME)!!
                    loadedPlaylist.updateSongSelectors()
                    mainActivityViewModel.changeLoadedPlaylist(loadedPlaylist.songSelectors)
                }
                else -> {
                    if (playlists.getPlaylistByName(DEFAULT_PLAYLIST_NAME) == null) {
                        playlists.addPlaylist(DEFAULT_PLAYLIST_NAME, playlists.getDefaultSongs())
                        currentPlaylist.songs = playlists.getPlaylistByName(DEFAULT_PLAYLIST_NAME)!!
                        mainActivityViewModel.changeCurrentState(PLAY_MUSIC)
                        mainActivityViewModel.changeCurrentPlaylist(currentPlaylist.songs)
                    } else {
                        currentPlaylist.songs = playlists.getPlaylistByName(DEFAULT_PLAYLIST_NAME)!!
                        mainActivityViewModel.changeCurrentPlaylist(currentPlaylist.songs)
                    }
                }
            }
        }

        adapter.apply {

            setOnItemClickListener(
                object : RecyclerAdapterSong.OnItemClickListener {
                    override fun onClick(songSelected: SongSelector, position: Int) {
                        val modifiedSong = Song(
                            songSelected.song.id, songSelected.song.title, songSelected.song.artist,
                            songSelected.song.duration
                        )
                        loadedPlaylist.songs[position] = modifiedSong
                        loadedPlaylist.songSelectors[position] = SongSelector(modifiedSong)
                        loadedPlaylist.songSelectors[position].isSelected = !songSelected.isSelected
                        mainActivityViewModel.changeLoadedPlaylist(loadedPlaylist.songSelectors.toList())
                    }
                }
            )

            setOnItemLongClickListener(
                object : RecyclerAdapterSong.OnItemLongClickListener {
                    override fun onLongClick(songSelected: Song, position: Int) {
                        loadedPlaylist.songs = playlists.getPlaylistByName(DEFAULT_PLAYLIST_NAME)!!
                        val index = loadedPlaylist.songs.indexOfFirst {
                            it.artist == songSelected.artist && it.title == songSelected.title &&
                            it.duration == songSelected.duration
                        }
                        loadedPlaylist.updateSongSelectors()
                        loadedPlaylist.songSelectors[index].isSelected = true
                        mainActivityViewModel.changeCurrentState(ADD_PLAYLIST)
                    }
                }
            )

            setOnButtonPlayPauseClickListener(
                object : RecyclerAdapterSong.OnButtonPlayPauseClickListener {
                    override fun onClick(song: Song, position: Int) {
                        if (song != currentPlaylist.currentTrack) {
                            val oldCurrentSong = currentPlaylist.currentTrack
                            val modifiedOldCurrentSong = Song(
                                oldCurrentSong.id,
                                oldCurrentSong.title,
                                oldCurrentSong.artist,
                                oldCurrentSong.duration
                            )

                            val indexOfOldSong = currentPlaylist.songs.indexOfFirst {
                                it.artist == oldCurrentSong.artist && it.title == oldCurrentSong.title
                                        && it.duration == oldCurrentSong.duration
                            }
                            currentPlaylist.songs[indexOfOldSong] = modifiedOldCurrentSong

                            val newSongRef = currentPlaylist.songs[position]
                            val newSong = Song(
                                newSongRef.id,
                                newSongRef.title,
                                newSongRef.artist,
                                newSongRef.duration
                            )
                            currentPlaylist.songs[position] = newSong
                            currentPlaylist.currentTrack = newSong
                            songStateController.play(newSong)
                            mainActivityViewModel.changeCurrentSong(newSong)
                            mainActivityViewModel.changeCurrentPlaylist(currentPlaylist.songs)
                        } else if (song == currentPlaylist.currentTrack) {
                            if (currentPlaylist.currentTrack.songState == SongState.PLAYING) {
                                val modifiedSong = Song(
                                    song.id, song.title, song.artist, song.duration
                                )
                                currentPlaylist.songs[position] = modifiedSong
                                currentPlaylist.currentTrack = modifiedSong
                                songStateController.pause(modifiedSong)
                                mainActivityViewModel.changeCurrentPlaylist(currentPlaylist.songs)
                            } else {
                                val modifiedSong = Song(
                                    song.id, song.title, song.artist, song.duration
                                )
                                currentPlaylist.songs[position] = modifiedSong
                                currentPlaylist.currentTrack = modifiedSong
                                songStateController.play(modifiedSong)
                                mainActivityViewModel.changeCurrentPlaylist(currentPlaylist.songs)
                            }
                        }
                    }
                }
            )
        }
    }
    private fun alertDialogCreator(title: String, playlists: List<String>, onClickListener: DialogInterface.OnClickListener): AlertDialog {
        return AlertDialog.Builder(this)
            .setTitle(title)
            .setItems(playlists.toTypedArray()) { dialog, i ->
                onClickListener.onClick(dialog, i)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onPlayPauseButtonClick() {
        if (currentPlaylist.currentTrack.songState == SongState.PLAYING) {
            val currentSong = currentPlaylist.currentTrack
            val modifiedSong = Song(
                currentSong.id, currentSong.title, currentSong.artist, currentSong.duration
            )
            val index = currentPlaylist.songs.indexOf(currentSong)
            currentPlaylist.songs[index] = modifiedSong
            currentPlaylist.currentTrack = modifiedSong
            songStateController.pause(modifiedSong)
            mainActivityViewModel.changeCurrentPlaylist(currentPlaylist.songs)
        } else {
            val currentSong = currentPlaylist.currentTrack
            val modifiedSong = Song(
                currentSong.id, currentSong.title, currentSong.artist, currentSong.duration
            )
            val index = currentPlaylist.songs.indexOf(currentSong)
            currentPlaylist.songs[index] = modifiedSong
            currentPlaylist.currentTrack = modifiedSong
            songStateController.play(modifiedSong)
            mainActivityViewModel.changeCurrentPlaylist(currentPlaylist.songs)
        }
    }

    override fun onStopButtonClick() {
        val currentSong = currentPlaylist.currentTrack
        val modifiedSong = Song(
            currentSong.id, currentSong.title, currentSong.artist, currentSong.duration
        )
        val index = currentPlaylist.songs.indexOf(currentSong)
        currentPlaylist.songs[index] = modifiedSong
        currentPlaylist.currentTrack = modifiedSong
        songStateController.stop(modifiedSong)
        mainActivityViewModel.changeCurrentSong(modifiedSong)
        mainActivityViewModel.changeCurrentPlaylist(currentPlaylist.songs)
    }

    override fun onCancelButtonClick() {
        mainActivityViewModel.changeCurrentState(PLAY_MUSIC)
    }

    override fun onOkButtonClick(name: String) {
        if (loadedPlaylist.songSelectors.none { it.isSelected }) {
            Toast.makeText(this, "Add at least one song to your playlist", Toast.LENGTH_LONG).show()
        } else if (name.isBlank()) {
            Toast.makeText(this, "Add a name to your playlist", Toast.LENGTH_LONG).show()
        } else if (name == DEFAULT_PLAYLIST_NAME) {
            Toast.makeText(this, "All Songs is a reserved name choose another playlist name", Toast.LENGTH_LONG).show()
        } else  {
            val songs = loadedPlaylist.songSelectors.filter { it.isSelected }.map { it.song }
            playlists.addPlaylist(name, songs)
            mainActivityViewModel.changeCurrentState(PLAY_MUSIC)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}

