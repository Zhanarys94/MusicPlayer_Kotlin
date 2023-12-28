package org.hyperskill.musicplayer

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
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
import org.hyperskill.musicplayer.ViewState.ADD_PLAYLIST
import org.hyperskill.musicplayer.ViewState.PLAY_MUSIC
import org.hyperskill.musicplayer.song.Song
import org.hyperskill.musicplayer.song.SongSelector
import org.hyperskill.musicplayer.viewModel.MainActivityViewModel
import org.hyperskill.musicplayer.playlist.MainAddPlaylistFragment
import org.hyperskill.musicplayer.playlist.MainPlayerControllerFragment

private const val DEFAULT_PLAYLIST_NAME = "All Songs"

class MainActivity : AppCompatActivity(),
    MainPlayerControllerFragment.OnFragmentInteractionListener,
    MainAddPlaylistFragment.AddPlaylistFragmentInteractionListener
{
    private lateinit var binding: ActivityMainBinding
    private val mainActivityViewModel: MainActivityViewModel by viewModels()
    /*private val playlists = PlaylistsRepositoryImpl()*/
    /*private val currentPlaylist = CurrentPlaylist(DEFAULT_PLAYLIST_NAME, playlists.getDefaultSongs())
    private val loadedPlaylist = LoadedPlaylist(DEFAULT_PLAYLIST_NAME, playlists.getDefaultSongs())*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        recyclerView.adapter = RecyclerAdapterSong()

        mainActivityViewModel.currentPlaylistLiveData.observe(this) { songs ->
            if (mainActivityViewModel.currentStateLiveData.value == PLAY_MUSIC) {
                (recyclerView.adapter as RecyclerAdapterSong).submitList(songs)
            }
        }
        mainActivityViewModel.playlistInAddPlaylistLiveData.observe(this) { songSelectors ->
            if (mainActivityViewModel.currentStateLiveData.value == ADD_PLAYLIST) {
                (recyclerView.adapter as RecyclerAdapterSong).submitList(songSelectors)
            }
        }
        mainActivityViewModel.currentStateLiveData.observe(this) {state ->
            if (state == PLAY_MUSIC) {
                (recyclerView.adapter as RecyclerAdapterSong).submitList(mainActivityViewModel.getCurrentPlaylist().toList())
                supportFragmentManager.popBackStack("AddPlaylist", 1)
            } else {
                (recyclerView.adapter as RecyclerAdapterSong).submitList(mainActivityViewModel.getLoadedPlaylist().songSelectors.toList())
                supportFragmentManager.beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.mainFragmentContainer, MainAddPlaylistFragment(),"AddPlaylist")
                    .addToBackStack("AddPlaylist")
                    .commit()

                onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        mainActivityViewModel.changeCurrentState(PLAY_MUSIC)
                        supportFragmentManager.popBackStack()
                        isEnabled = false
                    }
                })
            }
        }

        setSupportActionBar(binding.toolbar)

        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.mainMenuAddPlaylist -> mainMenuAddPlaylist()
                    R.id.mainMenuLoadPlaylist -> mainMenuLoadPlaylist()
                    R.id.mainMenuDeletePlaylist -> mainMenuDeletePlaylist()
                    else -> false
                }
            }
        })

        binding.mainButtonSearch.setOnClickListener {
            when (mainActivityViewModel.currentStateLiveData.value) {
                PLAY_MUSIC -> searchBtnClickInPlayMusic()
                ADD_PLAYLIST -> searchBtnClickInAddPlaylist()
                else -> {
                    mainActivityViewModel.searchBtnClickInitial()
                    mainActivityViewModel.changeCurrentState(PLAY_MUSIC)
                    mainActivityViewModel.updateCurrentPlaylist()
                }
            }
        }

        (recyclerView.adapter as RecyclerAdapterSong).apply {

            setOnItemClickListener(
                object : RecyclerAdapterSong.OnItemClickListener {
                    override fun onClick(songSelected: SongSelector, position: Int) {
                        val modifiedSong = Song(
                            songSelected.song.id, songSelected.song.title, songSelected.song.artist,
                            songSelected.song.duration
                        )
                        mainActivityViewModel.modifySongInLoadedPlaylist(songSelected, modifiedSong, position)
                        mainActivityViewModel.updatePlaylistInAddPlaylist()
                    }
                }
            )

            setOnItemLongClickListener(
                object : RecyclerAdapterSong.OnItemLongClickListener {
                    override fun onLongClick(songSelected: Song, position: Int) {
                        mainActivityViewModel.toAddPlaylistStateWithLongClick(songSelected)
                        mainActivityViewModel.changeCurrentState(ADD_PLAYLIST)
                    }
                }
            )

            setOnButtonPlayPauseClickListener(
                object : RecyclerAdapterSong.OnButtonPlayPauseClickListener {
                    override fun onClick(song: Song, position: Int) = playPauseBtnClick(song, position)
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

    override fun fragmentPlayPauseButtonClick() {
        mainActivityViewModel.fragmentPlayPauseClick()
    }

    override fun fragmentStopButtonClick() {
        mainActivityViewModel.fragmentStopClick()
    }

    override fun onCancelButtonClick() {
        mainActivityViewModel.changeCurrentState(PLAY_MUSIC)
    }

    override fun onOkButtonClick(name: String) {
        if (mainActivityViewModel.getLoadedPlaylist().songSelectors.none { it.isSelected }) {
            Toast.makeText(this, "Add at least one song to your playlist", Toast.LENGTH_LONG).show()
        } else if (name.isBlank()) {
            Toast.makeText(this, "Add a name to your playlist", Toast.LENGTH_LONG).show()
        } else if (name == DEFAULT_PLAYLIST_NAME) {
            Toast.makeText(this, "All Songs is a reserved name choose another playlist name", Toast.LENGTH_LONG).show()
        } else  {
            mainActivityViewModel.saveNewPlaylist(name)
            mainActivityViewModel.changeCurrentState(PLAY_MUSIC)
        }
    }

    private fun mainMenuAddPlaylist(): Boolean {
        return if (mainActivityViewModel.currentStateLiveData.value == ADD_PLAYLIST) {
            true
        } else {
            if (!mainActivityViewModel.toAddPlaylistStateFromMenu()) {
                Toast.makeText(this, "No songs loaded, click search to load songs", Toast.LENGTH_LONG).show()
                return false
            }
            mainActivityViewModel.changeCurrentState(ADD_PLAYLIST)
            Log.d("mainMenuAddPlaylist()", "State was changed to Add Playlist")
            true
        }
    }

    private fun mainMenuLoadPlaylist(): Boolean {
        val playlistsNames = mainActivityViewModel.getAllPlaylistsNames()
        val clickListener = DialogInterface.OnClickListener { _, itemIndex ->
            if (mainActivityViewModel.currentStateLiveData.value == PLAY_MUSIC) {
                mainActivityViewModel.loadPlaylistInPlayMusicState(playlistsNames, itemIndex)
            } else {
                mainActivityViewModel.loadPlaylistInAddPlaylistState(playlistsNames, itemIndex)
            }
        }
        alertDialogCreator("choose playlist to load", playlistsNames, clickListener)
        Log.d("mainMenuLoadPlaylist()", "Load playlist menu item was clicked")
        return true
    }

    private fun mainMenuDeletePlaylist(): Boolean {
        val playlistsNames = mainActivityViewModel.getAllPlaylistsNames().filterNot {
            it == DEFAULT_PLAYLIST_NAME
        }
        val clickListener = DialogInterface.OnClickListener { _, itemIndex ->
            val selectedPlaylistName = playlistsNames[itemIndex]
            mainActivityViewModel.deletePlaylist(selectedPlaylistName)
        }
        alertDialogCreator("choose playlist to delete", playlistsNames, clickListener)
        Log.d("mainMenuDeletePlaylist()", "Delete playlist menu item was clicked")
        return true
    }

    private fun searchBtnClickInitial() {
        mainActivityViewModel.searchBtnClickInitial()
    }
    private fun searchBtnClickInPlayMusic() {
        mainActivityViewModel.searchBtnClickPlayMusic()
    }

    private fun searchBtnClickInAddPlaylist() {
        mainActivityViewModel.searchBtnClickAddPlaylist()
    }

    private fun playPauseBtnClick(song: Song, position: Int) {
        mainActivityViewModel.songPlayPauseClick(song, position)
    }
}

