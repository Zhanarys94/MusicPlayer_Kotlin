package org.hyperskill.musicplayer

import android.app.AlertDialog
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.FragmentContainerView
import androidx.recyclerview.widget.RecyclerView
import org.hyperskill.musicplayer.internals.MusicPlayerUnitTests
import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows

// version 1.3
@RunWith(RobolectricTestRunner::class)
class Stage1UnitTestB : MusicPlayerUnitTests<MainActivity>(MainActivity::class.java){

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

    val mainMenuItemIdAddPlaylist = "mainMenuAddPlaylist"
    val mainMenuItemIdLoadPlaylist = "mainMenuLoadPlaylist"
    val mainMenuItemIdDeletePlaylist = "mainMenuDeletePlaylist"


    @Test
    fun checkMainActivityComponentsExist() {
        testActivity {
            mainButtonSearch
            mainSongList
            mainFragmentContainer
        }
    }

    @Test
    fun checkPlayerControllerFragmentComponentsExist() {
        testActivity {
            mainFragmentContainer

            val controllerTvCurrentTime =
                mainFragmentContainer.findViewByString<TextView>("controllerTvCurrentTime")


            val actualCurrentTime = controllerTvCurrentTime.text.toString()
            val expectedCurrentTime = "00:00"
            val messageWrongInitialCurrentTime = "Wrong initial value for controllerTvCurrentTime"
            assertEquals(messageWrongInitialCurrentTime, expectedCurrentTime, actualCurrentTime)

            val controllerTvTotalTime =
                mainFragmentContainer.findViewByString<TextView>("controllerTvTotalTime")


            val actualTotalTime = controllerTvTotalTime.text.toString()
            val expectedTotalTime = "00:00"
            val messageWrongInitialTotalTime = "Wrong initial value for controllerTvTotalTime"
            assertEquals(messageWrongInitialTotalTime, expectedTotalTime, actualTotalTime)

            mainFragmentContainer.findViewByString<SeekBar>("controllerSeekBar")

            val controllerBtnPlayPause =
                mainFragmentContainer.findViewByString<Button>("controllerBtnPlayPause")

            val actualBtnPlayPauseText = controllerBtnPlayPause.text.toString().lowercase()
            val expectedBtnPlayPauseText = "play/pause"
            val messageWrongInitialBtnPlayPauseText = "Wrong initial value for controllerBtnPlayPause"
            assertEquals(messageWrongInitialBtnPlayPauseText, expectedBtnPlayPauseText, actualBtnPlayPauseText)

            val controllerBtnStop =
                mainFragmentContainer.findViewByString<Button>("controllerBtnStop")
            val actualBtnStopText = controllerBtnStop.text.toString().lowercase()
            val expectedBtnStopText = "stop"
            val messageWrongInitialBtnStopText = "Wrong initial value for controllerBtnStop"
            assertEquals(messageWrongInitialBtnStopText, expectedBtnStopText, actualBtnStopText)
        }
    }

    @Ignore
    @Test
    fun checkSearchButtonNoSongsFound() {
        testActivity {
            mainButtonSearch

            mainButtonSearch.clickAndRun()
            assertLastToastMessageEquals(
                "wrong toast message after click to mainButtonSearch",
                "no songs found"
            )
        }
    }

    @Test
    fun checkMenuItemAddPlaylistWithNoSongs() {
        testActivity {
            activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)
            assertLastToastMessageEquals(
                "wrong toast message after click to mainMenuItemIdAddPlaylist",
                "no songs loaded, click search to load songs"
            )
        }
    }



    @Test
    fun checkMenuItemLoadPlaylist() {
        testActivity {
            activity.clickMenuItemAndRun(mainMenuItemIdLoadPlaylist)

            val (alertDialog, shadowAlertDialog) = getLastAlertDialogWithShadow(
                errorMessageNotFound = "No Dialog was shown after click on mainMenuLoadPlaylist."
            )

            val actualTitle = shadowAlertDialog.title.toString().lowercase()
            val messageWrongTitle =
                "Wrong title found on dialog shown after click on mainMenuLoadPlaylist"
            val expectedTitle = "choose playlist to load"
            assertEquals(messageWrongTitle, expectedTitle, actualTitle)


            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).clickAndRun()
        }
    }

    @Test
    fun checkMenuItemDeletePlaylist() {
        testActivity {
            activity.clickMenuItemAndRun(mainMenuItemIdDeletePlaylist)


            val (alertDialog, shadowAlertDialog) = getLastAlertDialogWithShadow(
                errorMessageNotFound = "No Dialog was shown after click on mainMenuDeletePlaylist."
            )

            val actualTitle = shadowAlertDialog.title.toString().lowercase()
            val messageWrongTitle =
                "Wrong title found on dialog shown after click on mainMenuDeletePlaylist"
            val expectedTitle = "choose playlist to delete"
            assertEquals(messageWrongTitle, expectedTitle, actualTitle)


            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).clickAndRun()
        }
    }

    @Test
    fun checkControllerStopButtonBeforeSearch() {

        testActivity {
            mainFragmentContainer

            val controllerBtnStop =
                mainFragmentContainer.findViewByString<Button>("controllerBtnStop")

            controllerBtnStop.clickAndRun()
            // should not throw Exception
        }
    }

    @Test
    fun checkControllerSeekBarBeforeSearch() {

        testActivity {
            mainFragmentContainer

            val controllerSeekBar =
                mainFragmentContainer.findViewByString<SeekBar>("controllerSeekBar")

            if(Shadows.shadowOf(controllerSeekBar).onSeekBarChangeListener != null) {
                controllerSeekBar.setProgressAsUser(1)
                //should not throw exception
            } else {
                // ok
            }

        }
    }

    @Test
    fun checkControllerPlayPauseButtonBeforeSearch() {

        testActivity {
            mainFragmentContainer

            val controllerBtnPlayPause =
                mainFragmentContainer.findViewByString<Button>("controllerBtnPlayPause")

            controllerBtnPlayPause.clickAndRun()
            // should not throw Exception
        }
    }
}