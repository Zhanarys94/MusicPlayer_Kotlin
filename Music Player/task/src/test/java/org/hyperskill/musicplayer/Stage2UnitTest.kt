package org.hyperskill.musicplayer

import android.Manifest
import android.app.AlertDialog
import android.graphics.Color
import android.widget.*
import androidx.fragment.app.FragmentContainerView
import androidx.recyclerview.widget.RecyclerView
import org.hyperskill.musicplayer.internals.CustomMediaPlayerShadow
import org.hyperskill.musicplayer.internals.MusicPlayerUnitTests
import org.hyperskill.musicplayer.internals.SongFake
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit

// version 1.3
@Config(shadows = [CustomMediaPlayerShadow::class])
@RunWith(RobolectricTestRunner::class)
class Stage2UnitTest : MusicPlayerUnitTests<MainActivity>(MainActivity::class.java){

    companion object {
        const val mainMenuItemIdAddPlaylist = "mainMenuAddPlaylist"
        const val mainMenuItemIdLoadPlaylist = "mainMenuLoadPlaylist"
        const val mainMenuItemIdDeletePlaylist = "mainMenuDeletePlaylist"

        val songFakeList = (1..10).map { idNum ->
            SongFake(
                id = idNum,
                artist = "artist$idNum",
                title = "title$idNum",
                duration = 215_000
            )
        }
    }

    private val mainButtonSearch by lazy {
        val view = activity.findViewByString<Button>("mainButtonSearch")

        val expectedText = "search"
        val actualText = view.text.toString().lowercase()
        assertEquals("wrong text for mainButtonSearch", expectedText, actualText)

        view
    }

    private val mainSongList by lazy {
        activity.findViewByString<RecyclerView>("mainSongList")
    }

    private val mainFragmentContainer by lazy {
        activity.findViewByString<FragmentContainerView>("mainFragmentContainer")
    }

    @Before
    fun setUp() {
        setupContentProvider(songFakeList)
        shadowActivity.grantPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
        CustomMediaPlayerShadow.setFakeSong(songFakeList[0])
    }


    @Test
    fun checkSongListAfterInitialClickOnSearch() {

        testActivity {
            shadowActivity.grantPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
            mainButtonSearch
            mainSongList

            mainButtonSearch.clickAndRun()

            mainSongList.assertListItems(songFakeList) { itemViewSupplier, index, songFake ->
                assertSongItem("Wrong data after search.", itemViewSupplier(), songFake)

                val songItemImgBtnPlayPause: ImageButton =
                    itemViewSupplier().findViewByString("songItemImgBtnPlayPause")

                songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                    "When a song from the song list is stopped " +
                            "the image of songItemPlayPauseImgBtn should be R.drawable.ic_play",
                    R.drawable.ic_play
                )
            }
        }
    }

    @Test
    fun checkSongListItemChangesImageOnImageButtonClick() {

        testActivity {
            mainButtonSearch
            mainSongList

            mainButtonSearch.clickAndRun()
            val songFakeIndex = 3
            CustomMediaPlayerShadow.setFakeSong(songFakeList[songFakeIndex])

            mainSongList.assertSingleListItem(songFakeIndex) { itemViewSupplier ->
                var songItemImgBtnPlayPause =
                    itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")

                songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                    "When a song from the song list is stopped " +
                            "the image of songItemPlayPauseImgBtn should be R.drawable.ic_play",
                    R.drawable.ic_play
                )

                songItemImgBtnPlayPause.clickAndRun()
                songItemImgBtnPlayPause =
                    itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")

                songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                    "After clicking on songItemPlayPauseImgBtn on a stopped song " +
                            "the image displayed should change to R.drawable.ic_pause",
                    R.drawable.ic_pause
                )

                songItemImgBtnPlayPause.clickAndRun()
                songItemImgBtnPlayPause =
                    itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")

                songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                    "After clicking on songItemPlayPauseImgBtn on a playing song " +
                            "the image displayed should change to R.drawable.ic_play",
                    R.drawable.ic_play
                )
            }
        }
    }

    @Test
    fun checkWhenCurrentTrackChangesAndOldCurrentTrackIsPlayingImageChangesToPaused() {

        testActivity {
            mainButtonSearch
            mainSongList

            mainButtonSearch.clickAndRun()
            val songFakeIndexBefore = 5
            val songFakeIndexAfter = 7

            CustomMediaPlayerShadow.setFakeSong(songFakeList[songFakeIndexBefore])
            mainSongList.assertSingleListItem(songFakeIndexBefore) { itemViewSupplierBefore ->
                var songItemImgBtnPlayPauseBefore =
                    itemViewSupplierBefore().findViewByString<ImageButton>("songItemImgBtnPlayPause")

                songItemImgBtnPlayPauseBefore.drawable.assertCreatedFromResourceId(
                    "When a song from the song list is stopped " +
                            "the image of songItemPlayPauseImgBtn should be R.drawable.ic_play",
                    R.drawable.ic_play
                )

                songItemImgBtnPlayPauseBefore.clickAndRun()
                songItemImgBtnPlayPauseBefore =
                    itemViewSupplierBefore().findViewByString<ImageButton>("songItemImgBtnPlayPause")

                songItemImgBtnPlayPauseBefore.drawable.assertCreatedFromResourceId(
                    "After clicking on songItemPlayPauseImgBtn on a stopped song" +
                            " the image displayed should change to R.drawable.ic_pause",
                    R.drawable.ic_pause
                )

                CustomMediaPlayerShadow.setFakeSong(songFakeList[songFakeIndexAfter])
                shadowLooper.idleFor(10_000L, TimeUnit.MILLISECONDS)
                mainSongList.assertSingleListItem(songFakeIndexAfter) { itemViewSupplierAfter ->
                    var songItemImgBtnPlayPauseAfter =
                        itemViewSupplierAfter().findViewByString<ImageButton>("songItemImgBtnPlayPause")

                    songItemImgBtnPlayPauseAfter.drawable.assertCreatedFromResourceId(
                        "When a song from the song list is stopped " +
                                "the image of songItemPlayPauseImgBtn should be R.drawable.ic_play",
                        R.drawable.ic_play
                    )

                    songItemImgBtnPlayPauseAfter.clickAndRun()
                    songItemImgBtnPlayPauseAfter =
                        itemViewSupplierAfter().findViewByString<ImageButton>("songItemImgBtnPlayPause")

                    songItemImgBtnPlayPauseAfter.drawable.assertCreatedFromResourceId(
                        "After clicking on songItemPlayPauseImgBtn on a paused song " +
                                "the image displayed should change to R.drawable.ic_pause",
                        R.drawable.ic_pause
                    )

                }
                songItemImgBtnPlayPauseBefore =
                    itemViewSupplierBefore().findViewByString<ImageButton>("songItemImgBtnPlayPause")

                songItemImgBtnPlayPauseBefore.drawable.assertCreatedFromResourceId(
                    "After changing the currentTrack with the old currentTrack playing" +
                            "the image displayed on the old currentTrack should change to R.drawable.ic_play",
                    R.drawable.ic_play
                )
            }
        }
    }

    @Test
    fun checkWhenCurrentTrackChangesAndOldCurrentTrackIsNotPlayingImageRemains() {

        testActivity {
            mainButtonSearch
            mainSongList

            mainButtonSearch.clickAndRun()
            val songFakeIndexBefore = 5
            val songFakeIndexAfter = 7

            CustomMediaPlayerShadow.setFakeSong(songFakeList[songFakeIndexBefore])
            mainSongList.assertSingleListItem(songFakeIndexBefore) { ItemViewSupplierBefore ->
                var songItemImgBtnPlayPauseBefore =
                    ItemViewSupplierBefore().findViewByString<ImageButton>("songItemImgBtnPlayPause")

                songItemImgBtnPlayPauseBefore.drawable.assertCreatedFromResourceId(
                    "When a song from the song list is paused the image of songItemPlayPauseImgBtn " +
                            "should be R.drawable.ic_play",
                    R.drawable.ic_play
                )

                songItemImgBtnPlayPauseBefore.clickAndRun()
                songItemImgBtnPlayPauseBefore =
                    ItemViewSupplierBefore().findViewByString<ImageButton>("songItemImgBtnPlayPause")

                songItemImgBtnPlayPauseBefore.drawable.assertCreatedFromResourceId(
                    "After clicking on songItemPlayPauseImgBtn on a paused song " +
                            "the image displayed should change to R.drawable.ic_pause",
                    R.drawable.ic_pause
                )

                songItemImgBtnPlayPauseBefore.clickAndRun()
                songItemImgBtnPlayPauseBefore =
                    ItemViewSupplierBefore().findViewByString<ImageButton>("songItemImgBtnPlayPause")

                songItemImgBtnPlayPauseBefore.drawable.assertCreatedFromResourceId(
                    "After clicking on songItemPlayPauseImgBtn on a playing song " +
                            "the image displayed should change to R.drawable.ic_play",
                    R.drawable.ic_play
                )

                CustomMediaPlayerShadow.setFakeSong(songFakeList[songFakeIndexAfter])
                mainSongList.assertSingleListItem(songFakeIndexAfter) { itemViewSupplierAfter ->
                    var songItemImgBtnPlayPauseAfter =
                        itemViewSupplierAfter().findViewByString<ImageButton>("songItemImgBtnPlayPause")

                    songItemImgBtnPlayPauseAfter.drawable.assertCreatedFromResourceId(
                        "When a song from the song list is paused " +
                                "the image of songItemPlayPauseImgBtn should be R.drawable.ic_play",
                        R.drawable.ic_play
                    )

                    songItemImgBtnPlayPauseAfter.clickAndRun()
                    songItemImgBtnPlayPauseAfter =
                        itemViewSupplierAfter().findViewByString<ImageButton>("songItemImgBtnPlayPause")

                    songItemImgBtnPlayPauseAfter.drawable.assertCreatedFromResourceId(
                        "After clicking on songItemPlayPauseImgBtn on a paused song " +
                                "the image displayed should change to R.drawable.ic_pause",
                        R.drawable.ic_pause
                    )

                }

                songItemImgBtnPlayPauseBefore =
                    ItemViewSupplierBefore().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                songItemImgBtnPlayPauseBefore.drawable.assertCreatedFromResourceId(
                    "After changing the currentTrack with the old currentTrack not playing " +
                            "the image displayed should remain being R.drawable.ic_play",
                    R.drawable.ic_play
                )
            }
        }
    }

    @Test
    fun checkAfterInitialSearchFirstListItemIsCurrentTrackAndRespondToControllerPlayPauseButton() {

        testActivity {
            mainButtonSearch
            mainFragmentContainer

            val controllerBtnPlayPause =
                mainFragmentContainer.findViewByString<Button>("controllerBtnPlayPause")

            mainButtonSearch.clickAndRun()


            mainSongList.assertSingleListItem(0) { itemViewSupplier ->

                var songItemImgBtnPlayPause =
                    itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")

                songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                    "When a song from the song list is paused " +
                            "the image of songItemPlayPauseImgBtn should be R.drawable.ic_play",
                    R.drawable.ic_play
                )

                songItemImgBtnPlayPause.clickAndRun()

                songItemImgBtnPlayPause =
                    itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                    "After clicking on songItemPlayPauseImgBtn on a paused song" +
                            " the image displayed should change to R.drawable.ic_pause",
                    R.drawable.ic_pause
                )

                controllerBtnPlayPause.clickAndRun()

                songItemImgBtnPlayPause =
                    itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                    "After clicking on songItemPlayPauseImgBtn on a playing song" +
                            " the image displayed should change to R.drawable.ic_play",
                    R.drawable.ic_play
                )
            }
        }
    }

    @Test
    fun checkCurrentTrackImgChangeAfterControllerStopButtonClickWithCurrentTrackPlaying() {

        testActivity {
            mainButtonSearch
            mainFragmentContainer

            val songFakeIndex = 4

            val controllerBtnStop =
                mainFragmentContainer.findViewByString<Button>("controllerBtnStop")

            mainButtonSearch.clickAndRun()

            CustomMediaPlayerShadow.setFakeSong(songFakeList[songFakeIndex])
            mainSongList.assertSingleListItem(songFakeIndex) { itemViewSupplier ->

                var songItemImgBtnPlayPause =
                    itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")


                songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                    "When a song from the song list is stopped " +
                            "the image of songItemPlayPauseImgBtn should be R.drawable.ic_play",
                    R.drawable.ic_play
                )

                songItemImgBtnPlayPause.clickAndRun()
                songItemImgBtnPlayPause =
                    itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                    "After clicking on songItemPlayPauseImgBtn on a stopped song " +
                            "the image displayed should change to R.drawable.ic_pause",
                    R.drawable.ic_pause
                )

                controllerBtnStop.clickAndRun()
                songItemImgBtnPlayPause =
                    itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                    "After clicking on controllerBtnStop on a playing song " +
                            "the image displayed should change to R.drawable.ic_play",
                    R.drawable.ic_play
                )

                songItemImgBtnPlayPause.clickAndRun()
                songItemImgBtnPlayPause =
                    itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                    "After clicking on songItemImgBtnPlayPause on a stopped song " +
                            "the image of songItemPlayPauseImgBtn should change to R.drawable.ic_pause",
                    R.drawable.ic_pause
                )

                songItemImgBtnPlayPause.clickAndRun()
                songItemImgBtnPlayPause =
                    itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                    "After clicking on songItemImgBtnPlayPause on a playing song" +
                            "the image of songItemPlayPauseImgBtn should change to R.drawable.ic_play",
                    R.drawable.ic_play
                )

                controllerBtnStop.clickAndRun()
                songItemImgBtnPlayPause =
                    itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                    "After clicking on controllerBtnStop on a paused song " +
                            "the image displayed should remain R.drawable.ic_play",
                    R.drawable.ic_play
                )

                controllerBtnStop.clickAndRun()
                songItemImgBtnPlayPause =
                    itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                    "After clicking on controllerBtnStop on a stopped song " +
                            "the image displayed should remain R.drawable.ic_play",
                    R.drawable.ic_play
                )
            }
        }
    }

    @Test
    fun checkListItemImgChangeMixedClicks() {

        testActivity {
            mainButtonSearch
            mainFragmentContainer

            val songFakeIndex = 6

            val controllerBtnStop =
                mainFragmentContainer.findViewByString<Button>("controllerBtnStop")
            val controllerBtnPlayPause =
                mainFragmentContainer.findViewByString<Button>("controllerBtnPlayPause")

            mainButtonSearch.clickAndRun()

            CustomMediaPlayerShadow.setFakeSong(songFakeList[songFakeIndex])
            mainSongList.assertSingleListItem(songFakeIndex) { listItemViewSupplier ->

                var songItemImgBtnPlayPause = listItemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")

                for(i in 1..10) {
                    /*
                        1 - paused -> songItemImgBtnPlayPause -> playing
                        2 - playing -> controllerBtnPlayPause -> paused
                        3 - paused -> controllerBtnPlayPause -> playing
                        4 - playing -> songItemImgBtnPlayPause -> paused
                        5 - paused -> controllerBtnPlayPause -> playing
                        6 - playing -> controllerBtnStop -> paused
                        7 - paused -> songItemImgBtnPlayPause -> playing
                        8 - playing -> controllerBtnPlayPause -> paused
                        9 - paused -> controllerBtnPlayPause -> playing
                        10 - playing -> songItemImgBtnPlayPause -> paused
                     */

                    val buttonClickedId = if(i == 6) {
                        controllerBtnStop.clickAndRun()
                        songItemImgBtnPlayPause = listItemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                        songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                            "After clicking on controllerBtnStop on a playing song the image displayed should change to R.drawable.ic_play",
                            R.drawable.ic_play
                        )
                        continue
                    } else if(i % 3 == 1) {
                        songItemImgBtnPlayPause.clickAndRun()
                        songItemImgBtnPlayPause = listItemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                        "songItemImgBtnPlayPause"
                    } else {
                        controllerBtnPlayPause.clickAndRun()
                        songItemImgBtnPlayPause = listItemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                        "controllerBtnPlayPause"
                    }

                    if(i % 2 == 1) {
                        songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                            "After clicking on $buttonClickedId on a paused song the image displayed should change to R.drawable.ic_pause",
                            R.drawable.ic_pause
                        )
                    } else {
                        songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                            "After clicking on $buttonClickedId on a playing song the image displayed should change to R.drawable.ic_play",
                            R.drawable.ic_play
                        )
                    }
                }
            }
        }
    }

    @Test
    fun checkAddPlaylistStateTriggeredByMenuItem() {

        testActivity {
            mainButtonSearch
            mainSongList

            mainButtonSearch.clickAndRun()
            activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)

            mainSongList.assertListItems(songFakeList) { itemViewSupplier, index, itemSongFake ->
                val listItemView = itemViewSupplier()
                // check songSelector items
                val songItemTvTitle: TextView = listItemView.findViewByString("songSelectorItemTvTitle")
                val songItemTvArtist: TextView = listItemView.findViewByString("songSelectorItemTvArtist")
                val songItemTvDuration: TextView = listItemView.findViewByString("songSelectorItemTvDuration")
                val songSelectorItemCheckBox: CheckBox = listItemView.findViewByString("songSelectorItemCheckBox")

                val actualSongTitle = songItemTvTitle.text.toString()
                assertEquals(
                    "songItemTvTitle with incorrect text",
                    actualSongTitle,
                    itemSongFake.title
                )

                val actualSongArtist = songItemTvArtist.text.toString()
                assertEquals(
                    "songItemTvArtist with incorrect text",
                    actualSongArtist,
                    itemSongFake.artist
                )

                val actualSongDuration = songItemTvDuration.text.toString()
                val expectedSongDuration = itemSongFake.duration.timeString()
                assertEquals(
                    "songItemTvDuration with incorrect text",
                    expectedSongDuration,
                    actualSongDuration,
                )

                assertEquals(
                    "No songSelectorItemCheckBox should be checked after click on mainMenuItemIdAddPlaylist",
                    false,
                    songSelectorItemCheckBox.isChecked
                )

                listItemView.assertBackgroundColor(
                    errorMessage = "The backgroundColor for all songSelectorItems should be Color.WHITE after click on mainMenuItemIdAddPlaylist",
                    expectedBackgroundColor = Color.WHITE
                )
                //
            }

            mainFragmentContainer.findViewByString<Button>("addPlaylistBtnCancel").also { addPlaylistBtnCancel ->
                assertEquals("Wrong text for addPlaylistBtnCancel", "cancel", addPlaylistBtnCancel.text.toString().lowercase())
            }
            mainFragmentContainer.findViewByString<Button>("addPlaylistBtnOk").also { addPlaylistBtnOk ->
                assertEquals("Wrong text for addPlaylistBtnOk", "ok", addPlaylistBtnOk.text.toString().lowercase())
            }
            mainFragmentContainer.findViewByString<EditText>("addPlaylistEtPlaylistName").also { addPlaylistEtPlaylistName ->
                assertEquals("Wrong hint for addPlaylistEtPlaylistName", "playlist name", addPlaylistEtPlaylistName.hint.toString().lowercase())
            }
        }
    }

    @Test
    fun checkAddingPlaylistWithEmptyListAddedToastErrorEmptyListMessage() {
        testActivity {
            mainButtonSearch
            mainSongList

            mainButtonSearch.clickAndRun()
            activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)

            val playlistName = "My Playlist"

            val addPlaylistButtonOk =
                mainFragmentContainer.findViewByString<Button>("addPlaylistBtnOk")

            val addPlaylistEtPlaylistName =
                mainFragmentContainer.findViewByString<EditText>("addPlaylistEtPlaylistName")
            addPlaylistEtPlaylistName.setText(playlistName)

            addPlaylistButtonOk.clickAndRun()

            assertLastToastMessageEquals(
                errorMessage = "When there is no song selected a toast message is expected after click on addPlaylistBtnOk",
                expectedMessage = "Add at least one song to your playlist"
            )
        }
    }

    @Test
    fun checkAddingPlaylistWithBothEmptyListAndEmptyPlaylistNameToastErrorEmptyListMessage() {
        testActivity {
            mainButtonSearch
            mainSongList

            mainButtonSearch.clickAndRun()
            activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)

            val addPlaylistButtonOk =
                mainFragmentContainer.findViewByString<Button>("addPlaylistBtnOk")

            addPlaylistButtonOk.clickAndRun()

            assertLastToastMessageEquals(
                errorMessage = "When there is no song selected a toast message is expected after click on addPlaylistBtnOk",
                expectedMessage = "Add at least one song to your playlist"
            )
        }
    }

    @Test
    fun checkAddingPlaylistWithReservedPlaylistNameAllSongsToastErrorReservedNameMessage() {
        testActivity {
            mainButtonSearch
            mainSongList

            mainButtonSearch.clickAndRun()
            activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)

            val addPlaylistButtonOk =
                mainFragmentContainer.findViewByString<Button>("addPlaylistBtnOk")

            val playlistName = "All Songs"
            val addPlaylistEtPlaylistName =
                mainFragmentContainer.findViewByString<EditText>("addPlaylistEtPlaylistName")

            mainSongList.assertSingleListItem(0) {
                it().clickAndRun()
            }

            addPlaylistEtPlaylistName.setText(playlistName)
            addPlaylistButtonOk.clickAndRun()

            assertLastToastMessageEquals(
                errorMessage = "All Songs should be a reserve name. A toast with message",
                expectedMessage = "All Songs is a reserved name choose another playlist name"
            )
        }
    }

    @Test
    fun checkLoadPlaylistInPlayMusicStateAfterAddingPlaylistWithMainMenuItem() {

        testActivity {
            mainButtonSearch
            mainSongList

            val testedItemsZeroBasedIndexes = listOf(1, 3, 6)
            val testedItemsOneBasedIndexes = listOf(2, 4, 7)

            mainButtonSearch.clickAndRun()
            activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)
            val playlistName = "My Playlist"

            addPlaylist(
                playlistName = playlistName,
                selectedItemsIndex = testedItemsZeroBasedIndexes,
                songListView = mainSongList,
                fragmentContainer = mainFragmentContainer,
                testEmptyName = true
            )

            CustomMediaPlayerShadow.setFakeSong(songFakeList[testedItemsZeroBasedIndexes[0]])
            loadPlaylist(
                menuItemIdLoadPlaylist = mainMenuItemIdLoadPlaylist,
                expectedPlaylistNameList = listOf("All Songs", playlistName),
                playlistToLoadIndex = 1
            )

            val playlistSongFake = songFakeList.filter { it.id in testedItemsOneBasedIndexes }

            mainSongList.assertListItems(playlistSongFake) { itemViewSupplier, position, song ->

                assertSongItem("Wrong list item after playlist loaded", itemViewSupplier(), song)
                CustomMediaPlayerShadow.setFakeSong(song)

                // check image changes after playlist loaded
                var songItemImgBtnPlayPause =
                    itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                val controllerBtnPlayPause =
                    mainFragmentContainer.findViewByString<Button>("controllerBtnPlayPause")
                val controllerBtnStop =
                    mainFragmentContainer.findViewByString<Button>("controllerBtnStop")


                songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                    "When a song from the song list is paused the image of songItemPlayPauseImgBtn should be R.drawable.ic_play",
                    R.drawable.ic_play
                )

                songItemImgBtnPlayPause.clickAndRun()
                songItemImgBtnPlayPause =
                    itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")

                songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                    "After clicking on songItemPlayPauseImgBtn on a paused song the image displayed should change to R.drawable.ic_pause",
                    R.drawable.ic_pause
                )


                controllerBtnPlayPause.clickAndRun()
                songItemImgBtnPlayPause =
                    itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")

                songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                    "After clicking on controllerBtnPlayPause on a playing song the image displayed should change to R.drawable.ic_play",
                    R.drawable.ic_play
                )

                controllerBtnPlayPause.clickAndRun()
                songItemImgBtnPlayPause =
                    itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")

                songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                    "After clicking on controllerBtnPlayPause on a paused song the image displayed should change to R.drawable.ic_pause",
                    R.drawable.ic_pause
                )

                controllerBtnStop.clickAndRun()
                songItemImgBtnPlayPause =
                    itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")

                songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                    "After clicking on controllerBtnStop on a playing song the image displayed should change to R.drawable.ic_play",
                    R.drawable.ic_play
                )
            }
        }
    }

    @Test
    fun checkLoadPlaylistInPlayMusicStateAfterAddingPlaylistWithLongClick() {

        testActivity {
            mainButtonSearch
            mainSongList

            val testedItemsZeroBasedIndexes = listOf(4, 7, 8)
            val testedItemsOneBasedIndexes = testedItemsZeroBasedIndexes.map { it + 1 }
            val longClickItemZeroBasedIndex = 5
            val longClickItemOneBasedIndex = longClickItemZeroBasedIndex + 1
            mainButtonSearch.clickAndRun()

            mainSongList.assertSingleListItem(longClickItemZeroBasedIndex) {
                it().clickLongAndRun()
            }

            // check long click item is checked and deselect item
            mainSongList.assertListItems(songFakeList) { itemViewSupplier, position, item ->
                when(item.id) {
                    longClickItemOneBasedIndex -> {
                        val itemView = itemViewSupplier()
                        val songSelectorItemCheckBox =
                            itemView.findViewByString<CheckBox>("songSelectorItemCheckBox")

                        assertEquals(
                            "On the item that received a long click songSelectorItemCheckBox should be check.",
                            true,
                            songSelectorItemCheckBox.isChecked
                        )

                        itemView.assertBackgroundColor(
                            "On the item that received a long click background color should be Color.LT_GRAY.",
                            Color.LTGRAY
                        )

                        itemView.clickAndRun()  // deselect
                    }
                    else -> {}
                }
            }
            //

            val playlistName = "My Playlist"
            addPlaylist(
                playlistName = playlistName,
                selectedItemsIndex = testedItemsZeroBasedIndexes,
                songListView = mainSongList,
                fragmentContainer = mainFragmentContainer,
                testEmptyName = true
            )
            CustomMediaPlayerShadow.setFakeSong(songFakeList[testedItemsZeroBasedIndexes.first()])
            loadPlaylist(
                menuItemIdLoadPlaylist = mainMenuItemIdLoadPlaylist,
                expectedPlaylistNameList = listOf("All Songs", playlistName),
                playlistToLoadIndex = 1
            )

            val playlistSongFake = songFakeList.filter { it.id in testedItemsOneBasedIndexes }

            mainSongList.assertListItems(playlistSongFake) { itemViewSupplier, position, song ->

                assertSongItem("Wrong list item after playlist loaded", itemViewSupplier(), song)
                CustomMediaPlayerShadow.setFakeSong(song)

                // check image changes after load
                val controllerBtnPlayPause =
                    mainFragmentContainer.findViewByString<Button>("controllerBtnPlayPause")
                val controllerBtnStop =
                    mainFragmentContainer.findViewByString<Button>("controllerBtnStop")

                var songItemImgBtnPlayPause =
                    itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")

                songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                    "When a song from the song list is paused the image of songItemPlayPauseImgBtn should be R.drawable.ic_play",
                    R.drawable.ic_play
                )

                songItemImgBtnPlayPause.clickAndRun()
                songItemImgBtnPlayPause =
                    itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")

                songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                    "After clicking on songItemPlayPauseImgBtn on a paused song the image displayed should change to R.drawable.ic_pause",
                    R.drawable.ic_pause
                )

                controllerBtnPlayPause.clickAndRun()
                songItemImgBtnPlayPause =
                    itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")

                songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                    "After clicking on controllerBtnPlayPause on a playing song the image displayed should change to R.drawable.ic_play",
                    R.drawable.ic_play
                )

                controllerBtnPlayPause.clickAndRun()
                songItemImgBtnPlayPause =
                    itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")

                songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                    "After clicking on controllerBtnPlayPause on a paused song the image displayed should change to R.drawable.ic_pause",
                    R.drawable.ic_pause
                )

                controllerBtnStop.clickAndRun()
                songItemImgBtnPlayPause =
                    itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")

                songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                    "After clicking on controllerBtnStop on a playing song the image displayed should change to R.drawable.ic_play",
                    R.drawable.ic_play
                )
                //
            }
        }
    }

    @Test
    fun checkLoadPlaylistOnPlayMusicStateWithCurrentTrackKeepsCurrentTrack() {

        testActivity {
            mainButtonSearch
            mainSongList

            val testedItemsZeroBasedIndexes = listOf(1, 3, 6)
            val selectedSongZeroIndex = testedItemsZeroBasedIndexes[1]

            mainButtonSearch.clickAndRun()

            CustomMediaPlayerShadow.setFakeSong(songFakeList[selectedSongZeroIndex])
            mainSongList.assertSingleListItem(selectedSongZeroIndex) { itemViewSupplier ->
                var songItemImgBtnPlayPause =
                    itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")

                songItemImgBtnPlayPause.clickAndRun()
                songItemImgBtnPlayPause =
                    itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                    "After clicking on songItemImgBtnPlayPause the image displayed should change to R.drawable.ic_pause",
                    R.drawable.ic_pause
                )
            }

            activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)

            val playlistName = "My Playlist"
            addPlaylist(
                playlistName = playlistName,
                selectedItemsIndex = testedItemsZeroBasedIndexes,
                songListView = mainSongList,
                fragmentContainer = mainFragmentContainer
            )

            // check item keeps selected state after list add
            mainSongList.assertListItems(songFakeList) { itemViewSupplier, position, item ->
                var songItemImgBtnPlayPause =
                    itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")

                if(item.id == selectedSongZeroIndex + 1) {
                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "The selected song should remain selected after adding a playlist",
                        R.drawable.ic_pause
                    )

                    val controllerUi = mainFragmentContainer.getControllerViews()

                    controllerUi.btnPlayPause.clickAndRun()
                    songItemImgBtnPlayPause =
                        itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "The selected song should remain responding to controllerBtnPlayPause clicks after adding a playlist",
                        R.drawable.ic_play
                    )

                    songItemImgBtnPlayPause.clickAndRun()
                    songItemImgBtnPlayPause =
                        itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "The selected song should remain responding to songItemImgBtnPlayPause clicks after adding a playlist",
                        R.drawable.ic_pause
                    )

                    controllerUi.btnStop.clickAndRun()
                    songItemImgBtnPlayPause =
                        itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "The selected song should remain responding to controllerBtnStop clicks after adding a playlist",
                        R.drawable.ic_play
                    )

                    songItemImgBtnPlayPause.clickAndRun()
                    songItemImgBtnPlayPause =
                        itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "The selected song should remain responding to songItemImgBtnPlayPause clicks after adding a playlist",
                        R.drawable.ic_pause
                    )

                } else {
                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "A unselected song should remain unselected after adding a playlist",
                        R.drawable.ic_play
                    )
                }
            }
            //


            loadPlaylist(
                menuItemIdLoadPlaylist = mainMenuItemIdLoadPlaylist,
                expectedPlaylistNameList = listOf("All Songs", playlistName),
                playlistToLoadIndex = 1
            )

            // check item keeps selected state after list load
            mainSongList.assertListItems(
                testedItemsZeroBasedIndexes.map { songFakeList[it] }) { itemViewSupplier, position, item ->
                var songItemImgBtnPlayPause =
                    itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")

                if(item.id == selectedSongZeroIndex + 1) {
                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "The selected song should remain selected after loading a playlist",
                        R.drawable.ic_pause
                    )

                    val controllerUi = mainFragmentContainer.getControllerViews()

                    controllerUi.btnPlayPause.clickAndRun()
                    songItemImgBtnPlayPause =
                        itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "The selected song should remain responding to controllerBtnPlayPause clicks after loading a playlist",
                        R.drawable.ic_play
                    )

                    songItemImgBtnPlayPause.clickAndRun()
                    songItemImgBtnPlayPause =
                        itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "The selected song should remain responding to songItemImgBtnPlayPause clicks after loading a playlist",
                        R.drawable.ic_pause
                    )

                    controllerUi.btnStop.clickAndRun()
                    songItemImgBtnPlayPause =
                        itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "The selected song should remain responding to controllerBtnStop clicks after loading a playlist",
                        R.drawable.ic_play
                    )

                    songItemImgBtnPlayPause.clickAndRun()
                    songItemImgBtnPlayPause =
                        itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "The selected song should remain responding to songItemImgBtnPlayPause clicks after loading a playlist",
                        R.drawable.ic_pause
                    )

                } else {
                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "A unselected song should remain unselected after loading a playlist",
                        R.drawable.ic_play
                    )
                }
            }
            //
        }
    }

    @Test
    fun checkLoadPlaylistOnPlayMusicStateWithoutCurrentTrackChangesCurrentTrack() {

        testActivity {
            mainButtonSearch
            mainSongList

            val testedItemsZeroBasedIndexes = listOf(1, 3, 6)
            val selectedSongZeroIndex = 8

            mainButtonSearch.clickAndRun()
            CustomMediaPlayerShadow.setFakeSong(songFakeList[selectedSongZeroIndex])
            mainSongList.assertSingleListItem(selectedSongZeroIndex) { itemViewSupplier ->
                var songItemImgBtnPlayPause =
                    itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")

                songItemImgBtnPlayPause.clickAndRun()
                songItemImgBtnPlayPause =
                    itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                    "After clicking on songItemImgBtnPlayPause the image displayed should change to R.drawable.ic_pause",
                    R.drawable.ic_pause
                )
            }

            activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)

            val playlistName = "My Playlist"
            addPlaylist(
                playlistName = playlistName,
                selectedItemsIndex = testedItemsZeroBasedIndexes,
                songListView = mainSongList,
                fragmentContainer = mainFragmentContainer
            )

            // check item keeps selected state after list add
            mainSongList.assertListItems(songFakeList) { itemViewSupplier, position, item ->
                var songItemImgBtnPlayPause =
                    itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")

                if(item.id == selectedSongZeroIndex + 1) {
                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "The selected song should remain selected after adding a playlist",
                        R.drawable.ic_pause
                    )

                    val controllerUi = mainFragmentContainer.getControllerViews()

                    controllerUi.btnPlayPause.clickAndRun()
                    songItemImgBtnPlayPause =
                        itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "The selected song should remain responding to controllerBtnPlayPause clicks after adding a playlist",
                        R.drawable.ic_play
                    )

                    songItemImgBtnPlayPause.clickAndRun()
                    songItemImgBtnPlayPause =
                        itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "The selected song should remain responding to songItemImgBtnPlayPause clicks after adding a playlist",
                        R.drawable.ic_pause
                    )

                    controllerUi.btnStop.clickAndRun()
                    songItemImgBtnPlayPause =
                        itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "The selected song should remain responding to controllerBtnStop clicks after adding a playlist",
                        R.drawable.ic_play
                    )

                    songItemImgBtnPlayPause.clickAndRun()
                    songItemImgBtnPlayPause =
                        itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "The selected song should remain responding to songItemImgBtnPlayPause clicks after adding a playlist",
                        R.drawable.ic_pause
                    )

                } else {
                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "A unselected song should remain unselected after adding a playlist",
                        R.drawable.ic_play
                    )
                }
            }
            //

            CustomMediaPlayerShadow.setFakeSong(songFakeList[testedItemsZeroBasedIndexes.first()])
            loadPlaylist(
                menuItemIdLoadPlaylist = mainMenuItemIdLoadPlaylist,
                expectedPlaylistNameList = listOf("All Songs", playlistName),
                playlistToLoadIndex = 1
            )

            // check default item selected after list load
            mainSongList.assertListItems(
                testedItemsZeroBasedIndexes.map { songFakeList[it] }) { itemViewSupplier, position, item ->
                var songItemImgBtnPlayPause =
                    itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")

                if(position == 0) {
                    val controllerUi = mainFragmentContainer.getControllerViews()

                    controllerUi.btnPlayPause.clickAndRun()
                    songItemImgBtnPlayPause =
                        itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "The first song should be the currentTrack after loading a playlist " +
                                "without the old currentTrack and respond to controllerBtnPlayPause clicks",
                        R.drawable.ic_pause
                    )

                    controllerUi.btnStop.clickAndRun()
                    songItemImgBtnPlayPause =
                        itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "The currentTrack should remain responding " +
                                "to controllerBtnStop clicks after loading a playlist",
                        R.drawable.ic_play
                    )

                    songItemImgBtnPlayPause.clickAndRun()
                    songItemImgBtnPlayPause =
                        itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "The currentTrack should remain responding " +
                                "to songItemImgBtnPlayPause clicks after loading a playlist",
                        R.drawable.ic_pause
                    )

                } else {
                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "A track that is not the currentTrack should remain not being the currentTrack",
                        R.drawable.ic_play
                    )
                }
            }
            //
        }
    }

    @Test
    fun checkLoadPlaylistInAddPlaylistStateKeepsSelectedItemsById() {

        testActivity {
            mainButtonSearch
            mainSongList

            val playlistAItemsZeroBasedIndexes = listOf(0, 3, 6, 7, 8, 9)
            val playlistAItemsOneBasedIndexes = playlistAItemsZeroBasedIndexes.map { it + 1 }
            val playlistBItemsZeroBasedIndexes = playlistAItemsZeroBasedIndexes.filter { it % 3 ==  0 }
            val playlistBItemsOneBasedIndexes = playlistBItemsZeroBasedIndexes.map { it + 1 }

            mainButtonSearch.clickAndRun()
            activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)

            val playlistName = "Weird Sounds"

            addPlaylist(
                playlistName = playlistName,
                selectedItemsIndex = playlistAItemsZeroBasedIndexes,
                songListView = mainSongList,
                fragmentContainer = mainFragmentContainer,
            )

            activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)

            // check default playlist "All Songs" in ADD_PLAYLIST state and select items
            mainSongList.assertListItems(songFakeList) { itemViewSupplier, position, item ->
                var itemView = itemViewSupplier()
                var checkBox =
                    itemView.findViewByString<CheckBox>("songSelectorItemCheckBox")

                assertEquals(
                    "No songSelectorItemCheckBox should be checked after click on mainMenuItemIdAddPlaylist",
                    false,
                    checkBox.isChecked
                )

                if(item.id in playlistBItemsOneBasedIndexes) {
                    itemView.clickAndRun()
                    itemView = itemViewSupplier()
                    checkBox = itemView.findViewByString<CheckBox>("songSelectorItemCheckBox")


                    assertEquals(
                        "songSelectorItemCheckBox should be checked after click on list item",
                        true,
                        checkBox.isChecked
                    )
                }
            }
            //

            loadPlaylist(
                menuItemIdLoadPlaylist = mainMenuItemIdLoadPlaylist,
                expectedPlaylistNameList = listOf("All Songs", playlistName),
                playlistToLoadIndex = 1
            )

            // check loaded playlist in ADD_PLAYLIST state keeps selected items
            mainSongList.assertListItems(
                songFakeList.filter { it.id in playlistAItemsOneBasedIndexes }
            ) { itemViewSupplier, position, item ->

                val checkBox =
                    itemViewSupplier().findViewByString<CheckBox>("songSelectorItemCheckBox")

                if(item.id in playlistBItemsOneBasedIndexes) {
                    assertEquals(
                        "songSelectorItemCheckBox should remain isChecked value" +
                                " after list loaded on ADD_PLAYLIST state",
                        true,
                        checkBox.isChecked
                    )
                } else {
                    assertEquals(
                        "songSelectorItemCheckBox should remain isChecked value" +
                                " after list loaded on ADD_PLAYLIST state",
                        false,
                        checkBox.isChecked
                    )
                }
            }
            //
        }
    }

    @Test
    fun checkLoadPlaylistInAddPlaylistStateKeepsCurrentTrackWhenReturningToPlayMusicState() {

        testActivity {
            mainButtonSearch
            mainSongList

            val playlistItemsZeroBasedIndexes = listOf(0, 3, 6, 7, 8, 9)
            val selectedItem = 1

            mainButtonSearch.clickAndRun()

            CustomMediaPlayerShadow.setFakeSong(songFakeList[selectedItem])
            mainSongList.assertSingleListItem(selectedItem) { itemViewSupplier ->
                itemViewSupplier()
                    .findViewByString<ImageButton>("songItemImgBtnPlayPause")
                    .clickAndRun()
            }

            activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)

            val playlistName = "Party Songs"

            addPlaylist(
                playlistName = playlistName,
                selectedItemsIndex = playlistItemsZeroBasedIndexes,
                songListView = mainSongList,
                fragmentContainer = mainFragmentContainer,
            )

            activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)

            loadPlaylist(
                menuItemIdLoadPlaylist = mainMenuItemIdLoadPlaylist,
                expectedPlaylistNameList = listOf("All Songs", playlistName),
                playlistToLoadIndex = 1
            )

            mainFragmentContainer
                .findViewByString<Button>("addPlaylistBtnCancel")
                .clickAndRun()

            mainSongList.assertListItems(songFakeList){ itemViewSupplier, position, song ->
                assertSongItem(
                    "The currentPlaylist should not change " +
                            "after loading a playlist in ADD_PLAYLIST state",
                    itemViewSupplier(), song
                )
                val songItemImgBtnPlayPause =
                    itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")

                if(position == selectedItem) {
                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "The currentTrack should keep its playing state " +
                                "after loading a playlist on ADD_PLAYLIST state and returning to PLAY_MUSIC state",
                        R.drawable.ic_pause
                    )
                } else {
                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "A track that is not the currentTrack should not be playing",
                        R.drawable.ic_play
                    )
                }
            }
        }
    }

    @Test
    fun checkPlaylistSavedAfterSelectingSongsAfterLoadingPlaylistInAddPlaylistState() {

        testActivity {
            mainButtonSearch
            mainSongList

            val playlistOne = listOf(1, 2, 3)
            val selectItemsOne = listOf(0, 1)
            val selectItemsTwo = listOf(2, 3)

            mainButtonSearch.clickAndRun()
            val playlistName1 = "playlist1"
            val playlistName2 = "playlist2"

            activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)
            addPlaylist(
                playlistName = playlistName1,
                selectedItemsIndex = playlistOne,
                songListView = mainSongList,
                fragmentContainer = mainFragmentContainer
            )

            activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)


            mainSongList.assertListItems(songFakeList) {itemViewSupplier, position, item ->
                if(item.id - 1 in selectItemsOne) {
                    itemViewSupplier().clickAndRun()
                }
            }
            loadPlaylist(
                menuItemIdLoadPlaylist = mainMenuItemIdLoadPlaylist,
                expectedPlaylistNameList = listOf("All Songs", playlistName1),
                playlistToLoadIndex = 1
            )

            val loadPlaylistOneSongs = songFakeList.filter { it.id - 1 in playlistOne }
            mainSongList.assertListItems(loadPlaylistOneSongs) {itemViewSupplier, position, item ->
                if(item.id - 1 in selectItemsTwo) {
                    itemViewSupplier().clickAndRun()
                }
            }
            mainFragmentContainer.findViewByString<EditText>("addPlaylistEtPlaylistName").apply {
                setText(playlistName2)
            }
            mainFragmentContainer.findViewByString<Button>("addPlaylistBtnOk").clickAndRun()

            CustomMediaPlayerShadow.setFakeSong(songFakeList[playlistOne.first()])
            loadPlaylist(
                menuItemIdLoadPlaylist = mainMenuItemIdLoadPlaylist,
                expectedPlaylistNameList = listOf("All Songs", playlistName1, playlistName2),
                playlistToLoadIndex = 2
            )
            val messageItemsSaved =
                "The playlist saved should contain the selected items when clicking addPlaylistBtnOk"
            mainSongList.assertListItems(loadPlaylistOneSongs) { itemViewSupplier, position, song ->
                assertSongItem(messageItemsSaved, itemViewSupplier(), song)
            }
        }
    }

    @Test
    fun checkCancellingAddPlaylistKeepsCurrentPlaylist() {

        testActivity {
            mainButtonSearch
            mainSongList

            val playlistAItemsZeroBasedIndexes = listOf(3, 7, 8)
            val playlistAItemsOneBasedIndexes = playlistAItemsZeroBasedIndexes.map { it + 1 }

            mainButtonSearch.clickAndRun()
            val playlistName = "Cool Songs"

            activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)
            addPlaylist(
                playlistName = playlistName,
                selectedItemsIndex = playlistAItemsZeroBasedIndexes,
                songListView = mainSongList,
                fragmentContainer = mainFragmentContainer,
                testEmptyName = true
            )

            CustomMediaPlayerShadow.setFakeSong(songFakeList[playlistAItemsZeroBasedIndexes[0]])
            loadPlaylist(
                menuItemIdLoadPlaylist = mainMenuItemIdLoadPlaylist,
                expectedPlaylistNameList = listOf("All Songs", playlistName),
                playlistToLoadIndex = 1
            )

            // check loaded items
            val playlistSongFake = songFakeList.filter { it.id in playlistAItemsOneBasedIndexes }

            mainSongList.assertListItems(playlistSongFake) { itemViewSupplier, position, song ->
                assertSongItem("Wrong list item after playlist loaded", itemViewSupplier(), song)
            }
            //

            // canceling an add playlist
            activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)

            mainFragmentContainer.findViewByString<Button>("addPlaylistBtnCancel").clickAndRun()
            //

            // check loaded items remains
            mainSongList.assertListItems(playlistSongFake) { itemViewSupplier, position, item ->
                val messageWrongListItemAfterCancel =
                    "Playlist loaded should remain after addPlaylistBtnCancel clicked"
                assertSongItem(messageWrongListItemAfterCancel, itemViewSupplier(), item)
            }
            //
        }
    }

    @Test
    fun checkCancelingAddPlaylistKeepsCurrentTrackPlayingState() {

        testActivity {
            mainButtonSearch
            mainSongList

            val testedItemsZeroBasedIndexes = listOf(1, 3, 6)
            val selectedSongZeroIndex = testedItemsZeroBasedIndexes[1]

            mainButtonSearch.clickAndRun()

            CustomMediaPlayerShadow.setFakeSong(songFakeList[selectedSongZeroIndex])
            mainSongList.assertSingleListItem(selectedSongZeroIndex) { itemViewSupplier ->
                var songItemImgBtnPlayPause =
                    itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")

                songItemImgBtnPlayPause.clickAndRun()
                songItemImgBtnPlayPause =
                    itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")

                songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                    "After clicking on songItemImgBtnPlayPause " +
                            "the image displayed should change to R.drawable.ic_pause",
                    R.drawable.ic_pause
                )
            }

            activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)

            mainFragmentContainer.findViewByString<Button>("addPlaylistBtnCancel").clickAndRun()

            // check item keeps selected state after cancel add list
            mainSongList.assertListItems(songFakeList) { itemViewSupplier, position, item ->
                var songItemImgBtnPlayPause =
                    itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")

                if(item.id == selectedSongZeroIndex + 1) {
                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "The currentTrack should remain being the currentTrack " +
                                "after canceling adding a playlist",
                        R.drawable.ic_pause
                    )

                    val controllerUi = mainFragmentContainer.getControllerViews()

                    controllerUi.btnPlayPause.clickAndRun()
                    songItemImgBtnPlayPause =
                        itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "The currentTrack should remain responding to controllerBtnPlayPause clicks " +
                                "after canceling adding a playlist",
                        R.drawable.ic_play
                    )

                    songItemImgBtnPlayPause.clickAndRun()
                    songItemImgBtnPlayPause =
                        itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "The currentTrack should remain responding to songItemImgBtnPlayPause clicks " +
                                "after canceling adding a playlist",
                        R.drawable.ic_pause
                    )

                    controllerUi.btnStop.clickAndRun()
                    songItemImgBtnPlayPause =
                        itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "The currentTrack should remain responding to controllerBtnStop clicks " +
                                "after canceling adding a playlist",
                        R.drawable.ic_play
                    )

                    songItemImgBtnPlayPause.clickAndRun()
                    songItemImgBtnPlayPause =
                        itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "The currentTrack should remain responding to songItemImgBtnPlayPause clicks " +
                                "after canceling adding a playlist",
                        R.drawable.ic_pause
                    )

                } else {
                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "A track that is not the currentTrack should remain not being " +
                                "the currentTrack after canceling adding a playlist",
                        R.drawable.ic_play
                    )
                }
            }
            //
        }
    }

    @Test
    fun checkDeletePlaylistOnPlayMusicStateDeletingPlaylistThatIsNotCurrentPlaylist() {

        testActivity {
            mainButtonSearch
            mainSongList

            val testedItemsZeroBasedIndexes = listOf(1, 3, 6)

            mainButtonSearch.clickAndRun()
            activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)

            addPlaylist(
                playlistName = "My Playlist",
                selectedItemsIndex = testedItemsZeroBasedIndexes,
                songListView = mainSongList,
                fragmentContainer = mainFragmentContainer
            )

            // delete playlist
            activity.clickMenuItemAndRun(mainMenuItemIdDeletePlaylist)

            getLastAlertDialogWithShadow(
                "An AlertDialog should be displayed after click on mainMenuItemDeletePlaylist"
            ).also { (dialog, shadowDialog)  ->
                val dialogItems = shadowDialog.items.map { it.toString() }

                assertEquals("Wrong list displayed on AlertDialog after click on mainMenuItemDeletePlaylist",
                    listOf("My Playlist"),
                    dialogItems
                )
                shadowDialog.clickAndRunOnItem(0)
            }
            //

            // check delete dialog don't display deleted playlist
            activity.clickMenuItemAndRun(mainMenuItemIdDeletePlaylist)

            getLastAlertDialogWithShadow(
                "An AlertDialog should be displayed after click on mainMenuItemDeletePlaylist"
            ).also { (dialog, shadowDialog)  ->
                val dialogItems = shadowDialog.items.map { it.toString() }

                assertEquals("Wrong list displayed on AlertDialog after click on mainMenuItemDeletePlaylist",
                    listOf<String>(),
                    dialogItems
                )

                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).clickAndRun()
            }
            //

            // check load dialog don't display deleted playlist
            activity.clickMenuItemAndRun(mainMenuItemIdLoadPlaylist)

            getLastAlertDialogWithShadow(
                "An AlertDialog should be displayed after click on mainMenuItemLoadPlaylist"
            ).also { (dialog, shadowDialog)  ->
                val dialogItems = shadowDialog.items.map { it.toString() }

                assertEquals("Wrong list displayed on AlertDialog after click on mainMenuItemLoadPlaylist",
                    listOf("All Songs"),
                    dialogItems
                )

                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).clickAndRun()
            }
            //

            //check currentPlaylist remains
            mainSongList.assertListItems(songFakeList) { itemViewSupplier, position, song ->
                assertSongItem(
                    "Deleting a playlist that is not the currentPlaylist " +
                            "should not change the currentPlaylist",
                    itemViewSupplier(), song
                )
            }
        }
    }

    @Test
    fun checkDeletePlaylistOnPlayMusicStateWithCurrentPlaylistBeingDeleted() {

        testActivity {
            mainButtonSearch
            mainSongList

            val testedItemsZeroBasedIndexes = listOf(1, 3, 6)
            val testedItemsOneBasedIndexes = testedItemsZeroBasedIndexes.map { it + 1 }

            mainButtonSearch.clickAndRun()
            activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)

            val playlistName = "My Playlist"
            addPlaylist(
                playlistName = playlistName,
                selectedItemsIndex = testedItemsZeroBasedIndexes,
                songListView = mainSongList,
                fragmentContainer = mainFragmentContainer
            )
            CustomMediaPlayerShadow.setFakeSong(songFakeList[testedItemsZeroBasedIndexes.first()])
            loadPlaylist(
                menuItemIdLoadPlaylist = mainMenuItemIdLoadPlaylist,
                expectedPlaylistNameList = listOf("All Songs", playlistName),
                playlistToLoadIndex = 1
            )

            // check loaded items
            val playlistSongFake = songFakeList.filter { it.id in testedItemsOneBasedIndexes }

            mainSongList.assertListItems(playlistSongFake) { itemViewSupplier, position, item ->
                val messageWrongListItemAfterPlaylistLoaded = "Wrong list item after playlist loaded"
                assertSongItem(messageWrongListItemAfterPlaylistLoaded, itemViewSupplier(), item)
            }
            //

            // delete playlist
            activity.clickMenuItemAndRun(mainMenuItemIdDeletePlaylist)

            getLastAlertDialogWithShadow(
                "An AlertDialog should be displayed after click on mainMenuItemDeletePlaylist"
            ).also { (dialog, shadowDialog)  ->
                val dialogItems = shadowDialog.items.map { it.toString() }

                assertEquals("Wrong list displayed on AlertDialog after click on mainMenuItemDeletePlaylist",
                    listOf(playlistName),
                    dialogItems
                )
                shadowDialog.clickAndRunOnItem(0)
            }
            //

            //check items
            mainSongList.assertListItems(songFakeList) {itemViewSupplier, position, item ->
                val messageWrongItem =
                    "Wrong list item found after deleting current playlist, " +
                            "expected \"All songs\" playlist to be loaded"
                assertSongItem(messageWrongItem, itemViewSupplier(), item)
            }

            // check delete dialog don't display deleted playlist
            activity.clickMenuItemAndRun(mainMenuItemIdDeletePlaylist)

            getLastAlertDialogWithShadow(
                "An AlertDialog should be displayed after click on mainMenuItemDeletePlaylist"
            ).also { (dialog, shadowDialog)  ->
                val dialogItems = shadowDialog.items.map { it.toString() }

                assertEquals(
                    "Wrong list displayed on AlertDialog after click on mainMenuItemDeletePlaylist",
                    listOf<String>(),
                    dialogItems
                )

                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).clickAndRun()
            }
            //

            // check load dialog don't display deleted playlist
            activity.clickMenuItemAndRun(mainMenuItemIdLoadPlaylist)

            getLastAlertDialogWithShadow(
                "An AlertDialog should be displayed after click on mainMenuItemLoadPlaylist"
            ).also { (dialog, shadowDialog)  ->
                val dialogItems = shadowDialog.items.map { it.toString() }

                assertEquals("Wrong list displayed on AlertDialog after click on mainMenuItemLoadPlaylist",
                    listOf("All Songs"),
                    dialogItems
                )

                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).clickAndRun()
            }
            //
        }
    }

    @Test
    fun checkDeletePlaylistOnAddPlaylistStateDeletingPlaylistThatIsNotDisplayingAndNotCurrentPlaylist() {

        testActivity {
            mainButtonSearch
            mainSongList

            val testedItemsZeroBasedIndexes = listOf(1, 3, 6)

            mainButtonSearch.clickAndRun()
            activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)

            addPlaylist(
                playlistName = "My Playlist",
                selectedItemsIndex = testedItemsZeroBasedIndexes,
                songListView = mainSongList,
                fragmentContainer = mainFragmentContainer
            )

            activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)

            // delete playlist
            activity.clickMenuItemAndRun(mainMenuItemIdDeletePlaylist)

            getLastAlertDialogWithShadow(
                "An AlertDialog should be displayed after click on mainMenuItemDeletePlaylist"
            ).also { (dialog, shadowDialog)  ->
                val dialogItems = shadowDialog.items.map { it.toString() }

                assertEquals("Wrong list displayed on AlertDialog after click on mainMenuItemDeletePlaylist",
                    listOf("My Playlist"),
                    dialogItems
                )
                shadowDialog.clickAndRunOnItem(0)
            }
            //

            // check delete dialog don't display deleted playlist
            activity.clickMenuItemAndRun(mainMenuItemIdDeletePlaylist)

            getLastAlertDialogWithShadow(
                "An AlertDialog should be displayed after click on mainMenuItemDeletePlaylist"
            ).also { (dialog, shadowDialog)  ->
                val dialogItems = shadowDialog.items.map { it.toString() }

                assertEquals("Wrong list displayed on AlertDialog after click on mainMenuItemDeletePlaylist",
                    listOf<String>(),
                    dialogItems
                )

                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).clickAndRun()
            }
            //

            // check load dialog don't display deleted playlist
            activity.clickMenuItemAndRun(mainMenuItemIdLoadPlaylist)

            getLastAlertDialogWithShadow(
                "An AlertDialog should be displayed after click on mainMenuItemLoadPlaylist"
            ).also { (dialog, shadowDialog)  ->
                val dialogItems = shadowDialog.items.map { it.toString() }

                assertEquals("Wrong list displayed on AlertDialog after click on mainMenuItemLoadPlaylist",
                    listOf("All Songs"),
                    dialogItems
                )

                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).clickAndRun()
            }
            //

            // check SongSelector items remains
            mainSongList.assertListItems(songFakeList) { itemViewSupplier, position, song ->
                val itemView = itemViewSupplier()
                val actualArtist = itemView
                    .findViewByString<TextView>("songSelectorItemTvArtist")
                    .text.toString().lowercase()
                val actualTitle = itemView
                    .findViewByString<TextView>("songSelectorItemTvTitle")
                    .text.toString().lowercase()
                val actualDuration = itemView
                    .findViewByString<TextView>("songSelectorItemTvDuration")
                    .text.toString().lowercase()
                val errorMessage =
                    "After deleting in ADD_PLAYLIST state a playlist that is not displaying " +
                            "the playlist that is displaying should remain"

                assertEquals(errorMessage, song.artist, actualArtist)
                assertEquals(errorMessage, song.title, actualTitle)
                assertEquals(errorMessage, song.duration.timeString(), actualDuration)
            }

            mainFragmentContainer
                .findViewByString<Button>("addPlaylistBtnCancel")
                .clickAndRun()

            //check currentPlaylist remains
            mainSongList.assertListItems(songFakeList) { itemViewSupplier, position, song ->
                val errorMessage =
                    "After deleting in ADD_PLAYLIST state a playlist that is not the currentPlaylist" +
                            "the currentPlaylist should remain"

                assertSongItem(errorMessage, itemViewSupplier(), song)
            }
        }
    }

    @Test
    fun checkDeletePlaylistOnAddPlaylistStateWithCurrentDisplayingAndCurrentPlaylistBeingDeleted() {

        testActivity {
            mainButtonSearch
            mainSongList

            val testedItemsZeroBasedIndexes = listOf(1, 3, 6)
            val playlistName = "My Playlist"

            mainButtonSearch.clickAndRun()
            activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)

            addPlaylist(
                playlistName = playlistName,
                selectedItemsIndex = testedItemsZeroBasedIndexes,
                songListView = mainSongList,
                fragmentContainer = mainFragmentContainer
            )

            // load list in PLAY_MUSIC state
            CustomMediaPlayerShadow.setFakeSong(songFakeList[testedItemsZeroBasedIndexes.first()])
            loadPlaylist(
                menuItemIdLoadPlaylist = mainMenuItemIdLoadPlaylist,
                expectedPlaylistNameList = listOf("All Songs", playlistName),
                playlistToLoadIndex = 1
            )

            activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)

            // load list in ADD_PLAYLIST state
            loadPlaylist(
                menuItemIdLoadPlaylist = mainMenuItemIdLoadPlaylist,
                expectedPlaylistNameList = listOf("All Songs", playlistName),
                playlistToLoadIndex = 1
            )

            CustomMediaPlayerShadow.setFakeSong(songFakeList.first())
            // delete playlist
            activity.clickMenuItemAndRun(mainMenuItemIdDeletePlaylist)

            getLastAlertDialogWithShadow(
                "An AlertDialog should be displayed after click on mainMenuItemDeletePlaylist"
            ).also { (dialog, shadowDialog)  ->
                val dialogItems = shadowDialog.items.map { it.toString() }

                assertEquals("Wrong list displayed on AlertDialog after click on mainMenuItemDeletePlaylist",
                    listOf(playlistName),
                    dialogItems
                )
                shadowDialog.clickAndRunOnItem(0)
            }
            //

            // check delete dialog don't display deleted playlist
            activity.clickMenuItemAndRun(mainMenuItemIdDeletePlaylist)

            getLastAlertDialogWithShadow(
                "An AlertDialog should be displayed after click on mainMenuItemDeletePlaylist"
            ).also { (dialog, shadowDialog)  ->
                val dialogItems = shadowDialog.items.map { it.toString() }

                assertEquals("Wrong list displayed on AlertDialog after click on mainMenuItemDeletePlaylist",
                    listOf<String>(),
                    dialogItems
                )

                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).clickAndRun()
            }
            //

            // check load dialog don't display deleted playlist
            activity.clickMenuItemAndRun(mainMenuItemIdLoadPlaylist)

            getLastAlertDialogWithShadow(
                "An AlertDialog should be displayed after click on mainMenuItemLoadPlaylist"
            ).also { (dialog, shadowDialog)  ->
                val dialogItems = shadowDialog.items.map { it.toString() }

                assertEquals("Wrong list displayed on AlertDialog after click on mainMenuItemLoadPlaylist",
                    listOf("All Songs"),
                    dialogItems
                )

                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).clickAndRun()
            }
            //

            // check SongSelector changes to "All Songs"
            mainSongList.assertListItems(songFakeList) { itemViewSupplier, position, song ->
                val itemView = itemViewSupplier()
                val actualArtist = itemView
                    .findViewByString<TextView>("songSelectorItemTvArtist")
                    .text.toString().lowercase()
                val actualTitle = itemView
                    .findViewByString<TextView>("songSelectorItemTvTitle")
                    .text.toString().lowercase()
                val actualDuration = itemView
                    .findViewByString<TextView>("songSelectorItemTvDuration")
                    .text.toString().lowercase()
                val errorMessage =
                    "After deleting in ADD_PLAYLIST state a playlist that is displaying " +
                            "the playlist that is displaying should change to \"All Songs\""

                assertEquals(errorMessage, song.artist, actualArtist)
                assertEquals(errorMessage, song.title, actualTitle)
                assertEquals(errorMessage, song.duration.timeString(), actualDuration)
            }

            mainFragmentContainer
                .findViewByString<Button>("addPlaylistBtnCancel")
                .clickAndRun()

            //check currentPlaylist changes to "All Songs"
            mainSongList.assertListItems(songFakeList) { itemViewSupplier, position, song ->
                val errorMessage =
                    "After deleting in ADD_PLAYLIST state a playlist that is the currentPlaylist" +
                            "the currentPlaylist should change to \"All Songs\""

                assertSongItem(errorMessage, itemViewSupplier(), song)
            }
        }
    }

    @Test
    fun checkSearchInPlayMusicStateChangeCurrentPlaylistToAllSongs() {

        testActivity {
            mainButtonSearch
            mainSongList

            val testedItemsZeroBasedIndexes = listOf(1, 3, 6)

            mainButtonSearch.clickAndRun()
            activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)
            val playlistName = "My Playlist"

            addPlaylist(
                playlistName = playlistName,
                selectedItemsIndex = testedItemsZeroBasedIndexes,
                songListView = mainSongList,
                fragmentContainer = mainFragmentContainer,
                testEmptyName = true
            )

            CustomMediaPlayerShadow.setFakeSong(songFakeList[testedItemsZeroBasedIndexes[0]])
            loadPlaylist(
                menuItemIdLoadPlaylist = mainMenuItemIdLoadPlaylist,
                expectedPlaylistNameList = listOf("All Songs", playlistName),
                playlistToLoadIndex = 1
            )

            mainButtonSearch.clickAndRun()

            mainSongList.assertListItems(songFakeList) { itemViewSupplier, position, song ->
                assertSongItem("Wrong list item after search button clicked", itemViewSupplier(), song)
            }
        }
    }

    @Test
    fun checkSearchInAddPlaylistStateDisplaysAllSongsOnAddPlaylistStateAndKeepsCurrentPlaylistInPlayMusicState() {

        testActivity {
            mainButtonSearch
            mainSongList

            val testedItemsZeroBasedIndexes = listOf(1, 3, 6)

            mainButtonSearch.clickAndRun()
            activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)
            val playlistName = "My Playlist"

            addPlaylist(
                playlistName = playlistName,
                selectedItemsIndex = testedItemsZeroBasedIndexes,
                songListView = mainSongList,
                fragmentContainer = mainFragmentContainer,
                testEmptyName = true
            )

            CustomMediaPlayerShadow.setFakeSong(songFakeList[testedItemsZeroBasedIndexes[0]])
            loadPlaylist(
                menuItemIdLoadPlaylist = mainMenuItemIdLoadPlaylist,
                expectedPlaylistNameList = listOf("All Songs", playlistName),
                playlistToLoadIndex = 1
            )

            val playlist =  testedItemsZeroBasedIndexes.map { songFakeList[it] }

            activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)
            loadPlaylist(
                menuItemIdLoadPlaylist = mainMenuItemIdLoadPlaylist,
                expectedPlaylistNameList = listOf("All Songs", playlistName),
                playlistToLoadIndex = 1
            )

            mainButtonSearch.clickAndRun()

            mainSongList.assertListItems(songFakeList) { itemViewSupplier, position, song ->
                val itemView = itemViewSupplier()
                val actualArtist = itemView
                    .findViewByString<TextView>("songSelectorItemTvArtist")
                    .text.toString().lowercase()
                val actualTitle = itemView
                    .findViewByString<TextView>("songSelectorItemTvTitle")
                    .text.toString().lowercase()
                val actualDuration = itemView
                    .findViewByString<TextView>("songSelectorItemTvDuration")
                    .text.toString().lowercase()
                val errorMessage =
                    "After mainButtonSearch is clicked on ADD_PLAYLIST state " +
                            "the \"All Songs\" playlist should be displaying"

                assertEquals(errorMessage, song.artist, actualArtist)
                assertEquals(errorMessage, song.title, actualTitle)
                assertEquals(errorMessage, song.duration.timeString(), actualDuration)
            }

            mainFragmentContainer
                .findViewByString<Button>("addPlaylistBtnCancel")
                .clickAndRun()

            mainSongList.assertListItems(playlist) { itemViewSupplier, position, song ->
                assertSongItem(
                    "After mainButtonSearch is clicked on ADD_PLAYLIST state " +
                            "the currentPlaylist in PLAY_MUSIC state should not change",
                    itemViewSupplier(), song
                )
            }
        }
    }
}