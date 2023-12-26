package org.hyperskill.musicplayer

import android.Manifest
import android.os.Handler
import android.os.SystemClock
import android.widget.Button
import android.widget.ImageButton
import androidx.fragment.app.FragmentContainerView
import androidx.recyclerview.widget.RecyclerView
import org.hyperskill.musicplayer.internals.CustomMediaPlayerShadow
import org.hyperskill.musicplayer.internals.CustomShadowAsyncDifferConfig
import org.hyperskill.musicplayer.internals.CustomShadowCountDownTimer
import org.hyperskill.musicplayer.internals.MusicPlayerUnitTests
import org.hyperskill.musicplayer.internals.SongFake
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Duration

// version 1.4
@Config(shadows = [CustomMediaPlayerShadow::class, CustomShadowCountDownTimer::class, CustomShadowAsyncDifferConfig::class])
@RunWith(RobolectricTestRunner::class)
class Stage3UnitTest : MusicPlayerUnitTests<MainActivity>(MainActivity::class.java) {


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
        CustomShadowCountDownTimer.handler = Handler(activity.mainLooper)
        setupContentProvider(songFakeList)
        shadowActivity.grantPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
        CustomMediaPlayerShadow.setFakeSong(songFakeList[0])
    }

    @Test
    fun checkControllerTriggersMediaPlayerOnDefaultItem(){

        testActivity {
            mainButtonSearch
            mainButtonSearch.clickAndRun()

            if(isPlayerNull().not()) {
                val messagePlayerPlayingOnSearchClick =
                        "After initial click on mainButtonSearch no MediaPlayer should be playing"
                assertEquals(messagePlayerPlayingOnSearchClick, false, player.isPlaying)
            }

            val controllerUi = mainFragmentContainer.getControllerViews()
            var playTime = 0

            playTime += controllerUi.btnPlayPause.clickAndRun(1_200) // play
            val messagePlayerShouldStartPlay =
                    "After click on controllerBtnPlayPause right after mainButtonSearch " +
                            "the default song item should start playing."
            player.assertControllerPlay(messagePlayerShouldStartPlay, controllerUi, expectedPosition = playTime)

            controllerUi.btnPlayPause.clickAndRun(20_000) // pause
            val messagePlayingShouldPauseOnClick =
                    "After click on controllerBtnPlayPause on a playing song the mediaPlayer should pause."
            player.assertControllerPause(messagePlayingShouldPauseOnClick, controllerUi, expectedPosition = playTime)

            playTime += controllerUi.btnPlayPause.clickAndRun(10_100) // play
            val messagePlayingShouldResumeOnClick =
                    "After click on controllerBtnPlayPause on a paused song the mediaPlayer should resume playing."
            player.assertControllerPlay(messagePlayingShouldResumeOnClick, controllerUi, expectedPosition = playTime)
            assertEquals(messagePlayingShouldResumeOnClick, true, player.isPlaying)

            controllerUi.btnStop.clickAndRun(10_000)  // stop
            val messagePlayingShouldStopOnStopClick =
                    "After click on controllerBtnPlayStop the player should stop."
            player.assertControllerStop(messagePlayingShouldStopOnStopClick, controllerUi)
        }
    }

    @Test
    fun checkImgButtonTriggersMediaPlayerOnListItem() {
        testActivity {
            mainButtonSearch

            mainButtonSearch.clickAndRun()
            val controllerUi = mainFragmentContainer.getControllerViews()

            if(isPlayerNull().not()) {
                val messagePlayerPlayingOnSearchClick =
                        "After initial click on mainButtonSearch no MediaPlayer should be playing"
                assertEquals(messagePlayerPlayingOnSearchClick, false, player.isPlaying)
            }
            var playingTime = 0

            val selectedItemIndex = 1
            CustomMediaPlayerShadow.setFakeSong(songFakeList[selectedItemIndex])
            mainSongList.assertSingleListItem(selectedItemIndex) { itemViewSupplier ->
                // invoking itemViewSupplier might increase clock time

                val timeBefore1 = SystemClock.currentGnssTimeClock().millis()
                var songItemImgBtnPlayPause =
                        itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause") //play
                val timeAfter1 = SystemClock.currentGnssTimeClock().millis()

                playingTime += songItemImgBtnPlayPause.clickAndRun(1_200 ) + (timeAfter1 - timeBefore1).toInt()

                // refresh reference to songItemImgBtnPlayPause
                val timeBefore2 = SystemClock.currentGnssTimeClock().millis()
                songItemImgBtnPlayPause =
                        itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                val timeAfter2 = SystemClock.currentGnssTimeClock().millis()

                playingTime += (timeAfter2 - timeBefore2).toInt()

                val messagePlayerShouldStartPlay =
                        "After click on songItemImgBtnPlayPause the song item should start playing."
                player.assertControllerPlay(messagePlayerShouldStartPlay, controllerUi, expectedPosition = playingTime)

                songItemImgBtnPlayPause.clickAndRun(20_000) // pause
                songItemImgBtnPlayPause =
                        itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")

                val messagePlayingShouldPauseOnClick =
                        "After click on songItemImgBtnPlayPause on a playing song the mediaPlayer should pause."
                player.assertControllerPause(messagePlayingShouldPauseOnClick, controllerUi, expectedPosition = playingTime)

                playingTime += songItemImgBtnPlayPause.clickAndRun(10_100) // play

                val messagePlayingShouldResumeOnClick =
                        "After click on songItemImgBtnPlayPause on a paused song the mediaPlayer should resume playing."
                player.assertControllerPlay(messagePlayingShouldResumeOnClick, controllerUi, expectedPosition = playingTime)

                controllerUi.btnStop.clickAndRun(10_000) //stop

                val messagePlayingShouldStopOnStopClick =
                        "After click on controllerBtnPlayStop the player should stop."
                player.assertControllerStop(messagePlayingShouldStopOnStopClick, controllerUi)
            }
        }
    }

    @Test
    fun checkSeekBarChangeWhilePlaying() {
        testActivity {
            mainButtonSearch

            mainButtonSearch.clickAndRun()

            if(isPlayerNull().not()) {
                val messagePlayerPlayingOnSearchClick =
                        "After initial click on mainButtonSearch MediaPlayer should not be playing"
                assertEquals(messagePlayerPlayingOnSearchClick, false, player.isPlaying)
            }

            val controllerUi = mainFragmentContainer.getControllerViews()

            var playingTime = 0
            playingTime += controllerUi.btnPlayPause.clickAndRun(1_200)  // play
            val messagePlayerShouldStartPlay =
                    "After click on controllerBtnPlayPause right after mainButtonSearch the default song item should start playing."
            player.assertControllerPlay(messagePlayerShouldStartPlay, controllerUi, expectedPosition = playingTime)

            controllerUi.seekBar.setProgressAsUser(100)  // seek with play
            shadowLooper.idleFor(Duration.ofMillis(100))
            playingTime = 100_100

            val errorSeekBarChange =
                    "After changing controllerSeekBar progress as user on a playing song " +
                            "the mediaPlayer should update its currentPosition and remain playing."
            player.assertControllerPlay(errorSeekBarChange, controllerUi, expectedPosition = playingTime)

            controllerUi.btnPlayPause.clickAndRun()  // pause
            val messagePauseAfterSeekBarChange =
                    "It should be possible to pause a song after changing controllerSeekBar."
            player.assertControllerPause(
                    messagePauseAfterSeekBarChange, controllerUi, expectedPosition = playingTime
            )
        }
    }

    @Test
    fun checkSeekBarBeforePlaying() {
        testActivity {
            mainButtonSearch

            mainButtonSearch.clickAndRun()

            if(isPlayerNull().not()) {
                val messagePlayerPlayingOnSearchClick =
                        "After initial click on mainButtonSearch MediaPlayer should not be playing."
                assertEquals(messagePlayerPlayingOnSearchClick, false, player.isPlaying)
            }

            val controllerUi = mainFragmentContainer.getControllerViews()
            var playingTime = 0

            controllerUi.seekBar.setProgressAsUser(100) // seek with stop
            shadowLooper.idleFor(Duration.ofMillis(100))
            playingTime = 100_000

            val messageSeekBarChangeBeforePlaying =
                    "After changing controllerSeekBar progress as user before playing a song " +
                            "the mediaPlayer should update its currentPosition and remain paused."
            player.assertControllerPause(
                    messageSeekBarChangeBeforePlaying, controllerUi, expectedPosition = playingTime
            )

            playingTime += controllerUi.btnPlayPause.clickAndRun(10_400) // play

            val messagePlayAfterSeekBarChangeBeforePlaying =
                    "It should be possible to play a song after " +
                            "changing controllerSeekBar progress as user before playing a song."
            player.assertControllerPlay(
                    messagePlayAfterSeekBarChangeBeforePlaying, controllerUi, expectedPosition = playingTime
            )
        }
    }

    @Test
    fun checkSeekBarAfterStop() {
        testActivity {
            mainButtonSearch

            mainButtonSearch.clickAndRun()

            if(isPlayerNull().not()) {
                val messagePlayerPlayingOnSearchClick =
                        "After initial click on mainButtonSearch MediaPlayer should not be playing."
                assertEquals(messagePlayerPlayingOnSearchClick, false, player.isPlaying)
            }

            val controllerUi = mainFragmentContainer.getControllerViews()

            controllerUi.btnPlayPause.clickAndRun(10_000) // play
            controllerUi.btnStop.clickAndRun() // stop

            controllerUi.seekBar.setProgressAsUser(100) // seek with stop
            shadowLooper.idleFor(Duration.ofMillis(1_000))

            val messageSeekBarChangeAfterStop =
                    "After changing controllerSeekBar progress as user with a stopped song " +
                            "the mediaPlayer should update its currentPosition and remain paused."
            player.assertControllerPause(
                    messageSeekBarChangeAfterStop, controllerUi, expectedPosition = 100_000
            )
        }
    }

    @Test
    fun checkSeekBarAfterPause() {
        testActivity {
            mainButtonSearch

            mainButtonSearch.clickAndRun()

            if(isPlayerNull().not()) {
                val messagePlayerPlayingOnSearchClick =
                        "After initial click on mainButtonSearch MediaPlayer should not be playing."
                assertEquals(messagePlayerPlayingOnSearchClick, false, player.isPlaying)
            }

            val controllerUi = mainFragmentContainer.getControllerViews()

            controllerUi.btnPlayPause.clickAndRun(10_000) // play
            controllerUi.btnPlayPause.clickAndRun()  // pause

            controllerUi.seekBar.setProgressAsUser(50) // seek with pause
            shadowLooper.idleFor(Duration.ofMillis(1_000))

            val messageSeekBarChangeAfterPause =
                    "After changing controllerSeekBar progress as user with a paused song " +
                            "the mediaPlayer should update its currentPosition and remain paused."
            player.assertControllerPause(
                    messageSeekBarChangeAfterPause, controllerUi, expectedPosition = 50_000
            )
        }
    }

    @Test
    fun checkMusicEnd() {
        testActivity {
            mainButtonSearch

            mainButtonSearch.clickAndRun()

            if(isPlayerNull().not()) {
                val messagePlayerPlayingOnSearchClick =
                        "After initial click on mainButtonSearch MediaPlayer should not be playing."
                assertEquals(messagePlayerPlayingOnSearchClick, false, player.isPlaying)
            }

            val controllerUi = mainFragmentContainer.getControllerViews()

            controllerUi.seekBar.setProgressAsUser(210) // seek with stop
            controllerUi.btnPlayPause.clickAndRun(10_000)  // play until end

            val messageSeekBarChangeAfterStop = "When a song ends the player should stop playing."
            player.assertControllerStop(messageSeekBarChangeAfterStop, controllerUi)

            mainSongList.assertSingleListItem(0) { itemViewSupplier ->
                val songItemImgBtnPlayPause =
                        itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")

                songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "When the song is finished the image should change to R.drawable.ic_play.",
                        R.drawable.ic_play
                )
            }
        }
    }

    @Test
    fun checkSeekBarChangeAfterMusicEnd() {
        testActivity {
            mainButtonSearch

            mainButtonSearch.clickAndRun()

            if(isPlayerNull().not()) {
                val messagePlayerPlayingOnSearchClick =
                        "After initial click on mainButtonSearch MediaPlayer should not be playing."
                assertEquals(messagePlayerPlayingOnSearchClick, false, player.isPlaying)
            }

            val controllerUi = mainFragmentContainer.getControllerViews()

            controllerUi.seekBar.setProgressAsUser(210)  // seek with stop
            controllerUi.btnPlayPause.clickAndRun(10_400)  // play until end

            val messageSeekBarChangeAfterStop = "When a song ends the player should stop playing."
            player.assertControllerStop(messageSeekBarChangeAfterStop, controllerUi)

            controllerUi.seekBar.setProgressAsUser(200) // seek with stop
            var playingTime = 200_000
            playingTime += controllerUi.btnPlayPause.clickAndRun(10_400) // play
            val messagePlayAfterSeekBarChangeAfterMusicEnd =
                    "It should be possible to change controllerSeekBar progress as user " +
                            "after a music ends and resume playing the song."
            player.assertControllerPlay(
                    messagePlayAfterSeekBarChangeAfterMusicEnd, controllerUi, expectedPosition = playingTime
            )
        }
    }

    @Test
    fun checkPlayAfterMusicEnd() {
        testActivity {
            mainButtonSearch

            mainButtonSearch.clickAndRun()

            if(isPlayerNull().not()) {
                val messagePlayerPlayingOnSearchClick =
                        "After initial click on mainButtonSearch MediaPlayer should not be playing."
                assertEquals(messagePlayerPlayingOnSearchClick, false, player.isPlaying)
            }

            val controllerUi = mainFragmentContainer.getControllerViews()

            controllerUi.seekBar.setProgressAsUser(210) // seek with stop
            controllerUi.btnPlayPause.clickAndRun(10_000) // play until end

            val messageSeekBarChangeAfterStop = "When a song ends the player should stop playing."
            player.assertControllerStop(messageSeekBarChangeAfterStop, controllerUi)

            val playingTime = controllerUi.btnPlayPause.clickAndRun(10_400) // play
            val messagePlayAfterSeekBarChangeAfterMusicEnd =
                    "It should be possible to play again a song after song end."
            player.assertControllerPlay(
                    messagePlayAfterSeekBarChangeAfterMusicEnd, controllerUi, expectedPosition = playingTime
            )
        }
    }

    @Test
    fun checkImgButtonPlayAfterMusicEnd() {
        testActivity {
            mainButtonSearch
            mainSongList

            mainButtonSearch.clickAndRun()

            if(isPlayerNull().not()) {
                val messagePlayerPlayingOnSearchClick =
                        "After initial click on mainButtonSearch MediaPlayer should not be playing."
                assertEquals(messagePlayerPlayingOnSearchClick, false, player.isPlaying)
            }

            val selectedItemIndex = 2
            CustomMediaPlayerShadow.setFakeSong(songFakeList[selectedItemIndex])
            mainSongList.assertSingleListItem(selectedItemIndex){ itemViewSupplier ->
                var songItemImgBtnPlayPause =
                        itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                val controllerUi = mainFragmentContainer.getControllerViews()

                songItemImgBtnPlayPause.clickAndRun() // play
                controllerUi.seekBar.setProgressAsUser(210)  // seek with play
                shadowLooper.idleFor(Duration.ofMillis(20_000)) // play until end

                val messageSeekBarChangeAfterStop = "When a song ends the player should stop playing."
                player.assertControllerStop(messageSeekBarChangeAfterStop, controllerUi)

                songItemImgBtnPlayPause =
                        itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                val playingTime = songItemImgBtnPlayPause.clickAndRun(10_400)  // play
                val messagePlayAfterSeekBarChangeAfterMusicEnd =
                        "It should be possible to play again a song after song end."
                player.assertControllerPlay(
                        messagePlayAfterSeekBarChangeAfterMusicEnd, controllerUi, expectedPosition = playingTime
                )
            }
        }
    }


    @Test
    fun checkSongChange() {

        testActivity {
            mainButtonSearch
            mainSongList

            mainButtonSearch.clickAndRun()

            if(isPlayerNull().not()) {
                val messagePlayerPlayingOnSearchClick =
                        "After initial click on mainButtonSearch MediaPlayer should not be playing."
                assertEquals(messagePlayerPlayingOnSearchClick, false, player.isPlaying)
            }

            val controllerUi = mainFragmentContainer.getControllerViews()

            CustomMediaPlayerShadow.wasResetOrRecreated = true  // consider first as already created
            mainSongList.assertListItems(songFakeList) { itemViewSupplier, position, song ->
                CustomMediaPlayerShadow.setFakeSong(song)
                val songItemImgBtnPlayPause =
                        itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                val playingTime = songItemImgBtnPlayPause.clickAndRun(2_200)  // play

                val messageSongChange =
                        "After click on songItemImgBtnPlayPause on a different songItem the current song should change."
                assertTrue(messageSongChange, CustomMediaPlayerShadow.wasResetOrRecreated)

                val messagePlaySongItem = "After first click on songItemImgBtnPlayPause that song should play."
                player.assertControllerPlay(messagePlaySongItem, controllerUi, expectedPosition = playingTime)

                controllerUi.btnPlayPause.clickAndRun()  // pause

                val messagePauseSongItem =
                        "After click on controllerBtnPlayPause with a playing song that song should be paused."
                player.assertControllerPause(messagePauseSongItem, controllerUi, expectedPosition = playingTime)

                controllerUi.btnStop.clickAndRun()  // stop

                val messageStopSongItem =
                        "After click on controllerBtnStop the song should be stopped"
                player.assertControllerStop(messageStopSongItem, controllerUi)

                CustomMediaPlayerShadow.wasResetOrRecreated = false
            }
        }
    }

    @Test
    fun checkCancelAddPlaylistKeepsPlayingCurrentSelectedSong() {

        testActivity {
            mainButtonSearch
            mainSongList

            val testedItemsZeroBasedIndexes = listOf(1, 3, 6)
            val selectedSongZeroIndex = testedItemsZeroBasedIndexes[1]

            mainButtonSearch.clickAndRun()

            var playingTime = 0

            CustomMediaPlayerShadow.setFakeSong(songFakeList[selectedSongZeroIndex])
            mainSongList.assertSingleListItem(selectedSongZeroIndex) { itemViewSupplier ->

                val songItemImgBtnPlayPause =
                        itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                val controllerUi = mainFragmentContainer.getControllerViews()


                playingTime += songItemImgBtnPlayPause.clickAndRun(3200) // play

                player.assertControllerPlay(
                        "A song should start playing after click on songItemImgBtnPlayPause",
                        controllerUi,
                        expectedPosition = playingTime
                )
            }

            playingTime += activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist, millis = 1_000)

            playingTime += mainFragmentContainer
                    .findViewByString<Button>("addPlaylistBtnCancel")
                    .clickAndRun(1_000)

            // give time to controller components to update values
            shadowLooper.idleFor(Duration.ofMillis(1_100))
            playingTime += 1_100
            //

            // check item keeps selected item state after list add
            mainSongList.assertListItems(songFakeList) { itemViewSupplier, position, item, elapsedTime ->
                // invoking itemViewSupplier might increase clock

                val timeBefore = SystemClock.currentGnssTimeClock().millis()
                var songItemImgBtnPlayPause =
                        itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                val timeAfter = SystemClock.currentGnssTimeClock().millis()
                playingTime += elapsedTime + (timeAfter - timeBefore).toInt()

                if (item.id == selectedSongZeroIndex + 1) {
                    val controllerUi = mainFragmentContainer.getControllerViews()

                    playingTime += adjustPlayerPositionToAvoidSyncIssues()

                    player.assertControllerPlay(
                            "A song should remain playing after list load if present on the loaded list.",
                            controllerUi, expectedPosition = playingTime
                    )

                    controllerUi.btnPlayPause.clickAndRun(2_000)  // pause
                    player.assertControllerPause(
                            "A selected song item should remain responding to controllerBtnPlayPause after list loaded.",
                            controllerUi, expectedPosition = playingTime
                    )
                    songItemImgBtnPlayPause =
                            itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                    playingTime += songItemImgBtnPlayPause.clickAndRun(1_200) // play
                    player.assertControllerPlay(
                            "The selected song should remain responding to songItemImgBtnPlayPause clicks after adding a playlist",
                            controllerUi, expectedPosition = playingTime
                    )

                    controllerUi.btnStop.clickAndRun() // stop
                    player.assertControllerStop(
                            "The selected song should remain responding to controllerBtnStop clicks after adding a playlist",
                            controllerUi
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
    fun checkLoadPlaylistKeepsPlayingCurrentSelectedSong() {

        testActivity {
            mainButtonSearch
            mainSongList

            val testedItemsZeroBasedIndexes = listOf(1, 3, 6)
            val selectedSongZeroIndex = testedItemsZeroBasedIndexes[1]

            mainButtonSearch.clickAndRun()

            var playingTime = 0
            CustomMediaPlayerShadow.setFakeSong(songFakeList[selectedSongZeroIndex])
            mainSongList.assertSingleListItem(selectedSongZeroIndex) { itemViewSupplier ->
                val songItemImgBtnPlayPause =
                        itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                val controllerUi = mainFragmentContainer.getControllerViews()
                playingTime += songItemImgBtnPlayPause.clickAndRun(3200)   // play

                player.assertControllerPlay(
                        "A song should start playing after click on songItemImgBtnPlayPause",
                        controllerUi,
                        expectedPosition = playingTime
                )
            }

            playingTime += activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist, millis = 3_000)

            val playlistName = "My Playlist"

            playingTime += addPlaylist(
                    playlistName = playlistName,
                    selectedItemsIndex = testedItemsZeroBasedIndexes,
                    songListView = mainSongList,
                    fragmentContainer = mainFragmentContainer
            )

            // give time to controller components to update values
            shadowLooper.idleFor(Duration.ofMillis(1_100))
            playingTime += 1_100
            //

            // check item keeps selected item state after list add
            mainSongList.assertListItems(songFakeList) { itemViewSupplier, position, item, elapsedTime ->
                playingTime += elapsedTime

                val timeBefore = SystemClock.currentGnssTimeClock().millis()
                var songItemImgBtnPlayPause =
                        itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                val timeAfter = SystemClock.currentGnssTimeClock().millis()
                playingTime += (timeAfter - timeBefore).toInt()

                if (item.id == selectedSongZeroIndex + 1) {
                    val controllerUi = mainFragmentContainer.getControllerViews()

                    playingTime += adjustPlayerPositionToAvoidSyncIssues()

                    player.assertControllerPlay(
                            "A song should remain playing after list load if present on the loaded list.",
                            controllerUi, expectedPosition = playingTime
                    )

                    controllerUi.btnPlayPause.clickAndRun(2_000) // pause
                    player.assertControllerPause(
                            "A selected song item should remain responding to controllerBtnPlayPause after list loaded.",
                            controllerUi, expectedPosition = playingTime
                    )

                    songItemImgBtnPlayPause =
                            itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                    playingTime += songItemImgBtnPlayPause.clickAndRun(1_200)  // play
                    player.assertControllerPlay(
                            "The selected song should remain responding to songItemImgBtnPlayPause clicks after adding a playlist",
                            controllerUi, expectedPosition = playingTime
                    )

                    controllerUi.btnStop.clickAndRun()  // stop
                    player.assertControllerStop(
                            "The selected song should remain responding to controllerBtnStop clicks after adding a playlist",
                            controllerUi
                    )

                    songItemImgBtnPlayPause =
                            itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                    playingTime = songItemImgBtnPlayPause.clickAndRun(1_200)  // play
                    player.assertControllerPlay(
                            "The selected song should remain responding to songItemImgBtnPlayPause clicks after adding a playlist",
                            controllerUi, expectedPosition = playingTime
                    )

                } else {
                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                            "A unselected song should remain unselected after loading a playlist",
                            R.drawable.ic_play
                    )
                }
            }
            //


            playingTime += loadPlaylist(
                    menuItemIdLoadPlaylist = mainMenuItemIdLoadPlaylist,
                    expectedPlaylistNameList = listOf("All Songs", playlistName),
                    playlistToLoadIndex = 1
            )

            // give time to controller components to update values
            shadowLooper.idleFor(Duration.ofMillis(1_100))
            playingTime += 1_100
            //

            // check item keeps selected item state after list load
            mainSongList.assertListItems(
                    testedItemsZeroBasedIndexes.map { songFakeList[it] }) { itemViewSupplier, position, item, elapsedTime ->

                playingTime += elapsedTime

                val timeBefore = SystemClock.currentGnssTimeClock().millis()
                var songItemImgBtnPlayPause =
                        itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                val timeAfter = SystemClock.currentGnssTimeClock().millis()
                playingTime += (timeAfter - timeBefore).toInt()

                if (item.id == selectedSongZeroIndex + 1) {
                    val controllerUi = mainFragmentContainer.getControllerViews()
                    playingTime += adjustPlayerPositionToAvoidSyncIssues()

                    player.assertControllerPlay(
                            "A song should remain playing after list load if present on the loaded list.",
                            controllerUi, expectedPosition = playingTime
                    )

                    controllerUi.btnPlayPause.clickAndRun(2_000)  // pause
                    player.assertControllerPause(
                            "A selected song item should remain responding to controllerBtnPlayPause after playlist loaded.",
                            controllerUi, expectedPosition = playingTime
                    )

                    songItemImgBtnPlayPause =
                            itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                    playingTime += songItemImgBtnPlayPause.clickAndRun(1_200)  // play
                    player.assertControllerPlay(
                            "The selected song should remain responding to songItemImgBtnPlayPause clicks after playlist loaded.",
                            controllerUi, expectedPosition = playingTime
                    )

                    controllerUi.btnStop.clickAndRun()  // stop
                    player.assertControllerStop(
                            "The selected song should remain responding to controllerBtnStop clicks after playlist loaded.",
                            controllerUi
                    )

                    songItemImgBtnPlayPause =
                            itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                    playingTime = songItemImgBtnPlayPause.clickAndRun(1_200)  // play
                    player.assertControllerPlay(
                            "The selected song should remain responding to songItemImgBtnPlayPause clicks after playlist loaded.",
                            controllerUi, expectedPosition = playingTime
                    )

                } else {
                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                            "A unselected song should remain unselected after playlist loaded.",
                            R.drawable.ic_play
                    )
                }
            }
            //
        }
    }

    @Test
    fun checkLoadPlaylistChangesSongIfCurrentSelectedSongNotInPlaylist() {

        testActivity {
            mainButtonSearch
            mainSongList

            val testedItemsZeroBasedIndexes = listOf(1, 3, 6)
            val selectedSongZeroIndex = 8

            mainButtonSearch.clickAndRun()

            var playingTime = 0
            CustomMediaPlayerShadow.setFakeSong(songFakeList[selectedSongZeroIndex])
            mainSongList.assertSingleListItem(selectedSongZeroIndex) { itemViewSupplier ->
                val timeBefore = SystemClock.currentGnssTimeClock().millis()
                val songItemImgBtnPlayPause =
                        itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                val controllerUi = mainFragmentContainer.getControllerViews()
                val timeAfter = SystemClock.currentGnssTimeClock().millis()
                playingTime += songItemImgBtnPlayPause.clickAndRun(3200) + (timeAfter - timeBefore).toInt()  // play

                player.assertControllerPlay(
                        "A song should start playing after click on songItemImgBtnPlayPause.",
                        controllerUi,
                        expectedPosition = playingTime
                )
            }

            playingTime += activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist, millis = 3_000)

            val playlistName = "Yes Playlist"

            playingTime += addPlaylist(
                    playlistName = playlistName,
                    selectedItemsIndex = testedItemsZeroBasedIndexes,
                    songListView = mainSongList,
                    fragmentContainer = mainFragmentContainer
            )

            // give time to controller components to update values
            shadowLooper.idleFor(Duration.ofMillis(1_100))
            playingTime += 1_100
            //

            // check item keeps selected item state after list add
            mainSongList.assertListItems(songFakeList) { itemViewSupplier, position, item, elapsedTime ->
                playingTime += elapsedTime

                val timeBefore = SystemClock.currentGnssTimeClock().millis()
                var songItemImgBtnPlayPause =
                        itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                val timeAfter = SystemClock.currentGnssTimeClock().millis()
                playingTime += (timeAfter - timeBefore).toInt()

                if (item.id == selectedSongZeroIndex + 1) {
                    val controllerUi = mainFragmentContainer.getControllerViews()
                    playingTime += adjustPlayerPositionToAvoidSyncIssues()

                    player.assertControllerPlay(
                            "A song should remain playing after adding playlist.",
                            controllerUi, expectedPosition = playingTime
                    )

                    controllerUi.btnPlayPause.clickAndRun(2_000)  // pause
                    player.assertControllerPause(
                            "A selected song item should remain responding to controllerBtnPlayPause after playlist added.",
                            controllerUi, expectedPosition = playingTime
                    )
                    songItemImgBtnPlayPause =
                            itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                    playingTime += songItemImgBtnPlayPause.clickAndRun(1_200)  // play
                    player.assertControllerPlay(
                            "The selected song should remain responding to songItemImgBtnPlayPause clicks after adding a playlist.",
                            controllerUi, expectedPosition = playingTime
                    )

                    controllerUi.btnStop.clickAndRun()  // stop
                    player.assertControllerStop(
                            "The selected song should remain responding to controllerBtnStop clicks after adding a playlist.",
                            controllerUi
                    )

                    songItemImgBtnPlayPause =
                            itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                    playingTime = songItemImgBtnPlayPause.clickAndRun(3_100)  // play
                    player.assertControllerPlay(
                            "The selected song should remain responding to songItemImgBtnPlayPause clicks after adding a playlist.",
                            controllerUi, expectedPosition = playingTime
                    )

                } else {
                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                            "A unselected song should remain unselected after adding a playlist.",
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
            ) // stop currentTrack because new list does not contain currentTrack

            // give time to controller components to update values
            shadowLooper.idleFor(Duration.ofMillis(1_100))
            //


            // check item keeps selected item state after list load
            mainSongList.assertListItems(
                    testedItemsZeroBasedIndexes.map { songFakeList[it] }) { itemViewSupplier, position, item, elapsedTime ->

                var songItemImgBtnPlayPause =
                        itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")

                if (position == 0) {
                    val controllerUi = mainFragmentContainer.getControllerViews()

                    controllerUi.assertControllerState(
                            "After loading a playlist without the current selected song" +
                                    " the first item of the loaded list should be selected.",
                            item, 0
                    )

                    playingTime = controllerUi.btnPlayPause.clickAndRun(1_100)  // play

                    player.assertControllerPlay(
                            "If the selected song is not present in the playlist loaded " +
                                    "the first item of the list should be selected and " +
                                    "react to clicks on controllerBtnPlayPause",
                            controllerUi, expectedPosition = playingTime
                    )

                    val timeBefore = SystemClock.currentGnssTimeClock().millis()
                    songItemImgBtnPlayPause =
                            itemViewSupplier().findViewByString<ImageButton>("songItemImgBtnPlayPause")
                    val timeAfter = SystemClock.currentGnssTimeClock().millis()
                    playingTime += (timeAfter - timeBefore).toInt()

                    songItemImgBtnPlayPause.clickAndRun(2_000)  // pause
                    player.assertControllerPause(
                            "The selected song item should respond to " +
                                    "songItemImgBtnPlayPause clicks after playlist loaded.",
                            controllerUi, expectedPosition = playingTime
                    )

                    controllerUi.btnStop.clickAndRun()  // stop
                    player.assertControllerStop(
                            "The selected song should remain responding to" +
                                    " controllerBtnStop clicks after playlist loaded",
                            controllerUi
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
    fun checkControllerKeepsStateAfterCancelAddPlaylist() {

        testActivity {
            mainButtonSearch.clickAndRun()
            var playingTime = 0

            mainFragmentContainer.also {
                val controllerUi = it.getControllerViews()

                controllerUi.seekBar.setProgressAsUser(100)  // seek with stop
                playingTime = 100_000
                playingTime += controllerUi.btnPlayPause.clickAndRun(1_100) // play

                val messageWrongStateAfterPlay =
                        "Wrong state of controller view after click on controllerBtnPlayPause after controllerSeekBar change."
                controllerUi.assertControllerState(
                        messageWrongStateAfterPlay, songFakeList[0], playingTime
                )
            }

            playingTime += activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist, 3_000)

            playingTime += mainFragmentContainer
                    .findViewByString<Button>("addPlaylistBtnCancel")
                    .clickAndRun(1_000)

            mainFragmentContainer.also {
                val controllerUi = it.getControllerViews()

                playingTime += adjustPlayerPositionToAvoidSyncIssues()

                val messageWrongStateAfterPlay =
                        "Wrong state of controller view after click on addPlaylistBtnCancel"
                controllerUi.assertControllerState(
                        messageWrongStateAfterPlay, songFakeList[0], playingTime
                )
            }
        }
    }
}