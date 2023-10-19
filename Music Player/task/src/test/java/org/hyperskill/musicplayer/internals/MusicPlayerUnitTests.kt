package org.hyperskill.musicplayer.internals


import android.app.Activity
import android.app.AlertDialog
import android.content.pm.ProviderInfo
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.SystemClock
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.annotation.ColorInt
import androidx.fragment.app.FragmentContainerView
import androidx.recyclerview.widget.RecyclerView
import org.junit.Assert.*
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.shadow.api.Shadow
import org.robolectric.shadows.ShadowAlertDialog
import org.robolectric.shadows.ShadowMediaPlayer
import java.time.Duration
import java.util.Collections.max
import java.util.concurrent.TimeUnit
import kotlin.math.abs

// version 1.3
open class MusicPlayerUnitTests<T : Activity>(clazz: Class<T>): AbstractUnitTest<T>(clazz) {


    init {
        CustomMediaPlayerShadow.setCreateListener(::onMediaPlayerCreated)
    }

    private var playerPrivate: MediaPlayer? = null
    private var shadowPlayerPrivate: ShadowMediaPlayer? = null

    protected var player: MediaPlayer
        get() {
            assertNotNull("No MediaPlayer was found", playerPrivate)
            return this.playerPrivate!!
        }
        set(_) {}

    protected var shadowPlayer: ShadowMediaPlayer
        get() {
            assertNotNull("No MediaPlayer was found", playerPrivate)
            shadowPlayer.invalidStateBehavior = ShadowMediaPlayer.InvalidStateBehavior.ASSERT
            return this.shadowPlayerPrivate!!
        }
        set(_) {}

    fun isPlayerNull(): Boolean {
        return playerPrivate == null
    }

    private fun onMediaPlayerCreated(player: MediaPlayer, shadow: ShadowMediaPlayer) {
        playerPrivate = player
        shadowPlayerPrivate = shadow
    }

    fun setupContentProvider(fakeSongResult: List<SongFake>){
        val info = ProviderInfo().apply {
            authority = MediaStore.AUTHORITY
        }
        Robolectric.buildContentProvider(FakeContentProvider::class.java).create(info)
        FakeContentProvider.fakeSongResult = fakeSongResult
    }

    /**
     * Use this method to perform clicks on menu items.
     *
     * It will assert the existence of the identifier. If the identifier exists but is not
     * a menu item then the assertion will succeed, but no click will be performed.
     *
     * Will also advance the clock millis milliseconds and run
     * enqueued Runnable scheduled to run on main looper in that timeframe.
     * Default value for millis is 500
     *
     */
    fun Activity.clickMenuItemAndRun(idString: String, millis: Long = 500): Int {
        val clock = SystemClock.currentGnssTimeClock()
        val timeBeforeClick = clock.millis()
        val identifier = resources.getIdentifier(idString, "id", packageName)

        assertTrue(
            "The identifier with idString \"$idString\" was not found",
            identifier != 0
        )

        shadowActivity.clickMenuItem(identifier)
        val timeAfterClick = clock.millis()

        shadowLooper.idleFor(Duration.ofMillis(millis- (timeAfterClick - timeBeforeClick)))
        val timeAfterIdle = clock.millis()
        return (timeAfterIdle - timeBeforeClick).toInt()
    }

    /**
     *  Retrieve last shown AlertDialog.
     *
     *  Will only find android.app.AlertDialog and not androidx.appcompat.app.AlertDialog
     *
     *  Returns the AlertDialog instance paired with its shadow
     */
    fun getLastAlertDialogWithShadow(errorMessageNotFound: String) : Pair<AlertDialog, ShadowAlertDialog> {
        val latestDialog:  AlertDialog? = ShadowAlertDialog.getLatestAlertDialog()

        assertNotNull("$errorMessageNotFound$ Make sure you are using android.app.AlertDialog", latestDialog)

        return latestDialog!! to Shadow.extract(latestDialog)
    }

    /**
     *  Makes assertions on the contents of the RecyclerView.
     *
     *  Asserts that the size matches the size of fakeResultList and then
     *  calls assertItems for each item of the list with the itemViewSupplier
     *  so that it is possible to make assertions on that itemView.
     *
     *  Take attention to refresh references to views coming from itemView since RecyclerView
     *  can change the instance of View for a determinate list item after an update to the list.
     */
    fun <T> RecyclerView.assertListItems(
        fakeResultList: List<T>,
        assertItems: (itemViewSupplier: () -> View, position: Int, item: T) -> Unit
    ) : Unit {

        assertNotNull("Your recycler view adapter should not be null", this.adapter)

        val expectedSize = fakeResultList.size

        val actualSize = this.adapter!!.itemCount
        assertEquals("Incorrect number of list items", expectedSize, actualSize)

        if(expectedSize == 0) {
            return
        } else if(expectedSize > 0) {
            val firstItemViewHolder = (0 until expectedSize)
                .asSequence()
                .mapNotNull {  this.findViewHolderForAdapterPosition(it) }
                .firstOrNull()
                ?: throw AssertionError("No item is being displayed on songList RecyclerView, is it big enough to display one item?")

            val listHeight = firstItemViewHolder.itemView.height * (expectedSize + 1)

            for((i, song) in fakeResultList.withIndex()) {
                // setting height to ensure that all items are inflated. Height might change after assertItems, keep statement inside loop.
                this.layout(0,0, this.width, listHeight)  // may increase clock time

                val itemViewSupplier = {
                    scrollToPosition(i)
                    findViewHolderForAdapterPosition(i)?.itemView
                        ?: throw AssertionError("Could not find list item with index $i")
                }
                assertItems(itemViewSupplier, i, song)
            }

        } else {
            throw IllegalStateException("size assertion was not effective")
        }
    }

    /**
     *  Makes assertions on the contents of the RecyclerView.
     *
     *  Asserts that the size matches the size of fakeResultList and then
     *  calls assertItems for each item of the list with the itemViewSupplier
     *  so that it is possible to make assertions on that itemView.
     *
     *  Take attention to refresh references to views coming from itemView since RecyclerView
     *  can change the instance of View for a determinate list item after an update to the list.
     *
     *  This version also includes elapsedTime on the callBack to help keep track of time
     *  since the clock might advance
     */
    fun <T> RecyclerView.assertListItems(
        fakeResultList: List<T>,
        assertItems: (itemViewSupplier: () -> View, position: Int, item: T, elapsedTime: Int) -> Unit
    ) : Unit {

        assertNotNull("Your recycler view adapter should not be null", this.adapter)

        val expectedSize = fakeResultList.size

        val actualSize = this.adapter!!.itemCount
        assertEquals("Incorrect number of list items", expectedSize, actualSize)

        if(expectedSize > 0) {
            val firstItemViewHolder = (0 until expectedSize)
                .asSequence()
                .mapNotNull {  this.findViewHolderForAdapterPosition(it) }
                .firstOrNull()
                ?: throw AssertionError("No item is being displayed on songList RecyclerView, is it big enough to display one item?")

            val listHeight = firstItemViewHolder.itemView.height * (expectedSize + 1)

            for((i, song) in fakeResultList.withIndex()) {
                val timeBefore = SystemClock.currentGnssTimeClock().millis()
                // setting height to ensure that all items are inflated. Height might change after assertItems, keep statement inside loop.
                this.layout(0,0, this.width, listHeight)  // may increase clock time

                val itemViewSupplier = {
                    scrollToPosition(i)
                    findViewHolderForAdapterPosition(i)?.itemView
                        ?: throw AssertionError("Could not find list item with index $i")
                }
                val timeAfter = SystemClock.currentGnssTimeClock().millis()
                assertItems(itemViewSupplier, i, song, (timeAfter -  timeBefore).toInt())
            }

        } else {
            throw IllegalStateException("size assertion was not effective")
        }
    }

    /**
     *  Makes assertions on the contents of one item of the RecyclerView.
     *
     *  Asserts that the the size of the list is at least itemIndex + 1.
     *
     *  Calls assertItem with the itemViewSupplier so that it is possible to make assertions on that itemView.
     *  Take attention to refresh references to views coming from itemView since RecyclerView
     *  can change the instance of View for a determinate list item after an update to the list.
     */
    fun RecyclerView.assertSingleListItem(itemIndex: Int, assertItem: (itemViewSupplier: () -> View) -> Unit) {

        assertNotNull("Your recycler view adapter should not be null", this.adapter)

        val expectedMinSize = itemIndex + 1

        val actualSize = this.adapter!!.itemCount
        assertTrue(
            "RecyclerView was expected to contain item with index $itemIndex, but its size was $actualSize",
            actualSize >= expectedMinSize
        )

        if(actualSize >= expectedMinSize) {
            val firstItemViewHolder = (0 until actualSize)
                .asSequence()
                .mapNotNull {  this.findViewHolderForAdapterPosition(it) }
                .firstOrNull()
                ?: throw AssertionError("No item is being displayed on songList RecyclerView, is it big enough to display one item?")

            val listHeight = firstItemViewHolder.itemView.height * (expectedMinSize + 1)
            this.layout(0,0, this.width, listHeight)  // may increase clock time

            val itemViewSupplier = {
                this.scrollToPosition(itemIndex)
                val itemView = (this.findViewHolderForAdapterPosition(itemIndex)?.itemView
                    ?: throw AssertionError("Could not find list item with index $itemIndex"))
                itemView

            }

            assertItem(itemViewSupplier)

        } else {
            throw IllegalStateException("size assertion was not effective")
        }
    }


    fun ShadowAlertDialog.clickAndRunOnItem(itemIndex: Int, millis: Long = 500): Int {
        val timeBeforeClick = SystemClock.currentGnssTimeClock().millis()
        this.clickOnItem(itemIndex) // might or might not increase clock time
        val timeAfterClick = SystemClock.currentGnssTimeClock().millis()
        shadowLooper.idleFor(Duration.ofMillis(millis - (timeAfterClick - timeBeforeClick)))
        val timeAfterIdle = SystemClock.currentGnssTimeClock().millis()

        assertTrue(
            "After click on AlertDialog item the dialog should be dismissed",
            this.hasBeenDismissed()
        )

        return (timeAfterIdle - timeBeforeClick).toInt()
    }


    fun Int.timeString(): String {
        return "%02d:%02d".format(this / 60_000, this % 60_000 / 1000)
    }

    fun View.assertBackgroundColor(errorMessage:String, @ColorInt expectedBackgroundColor: Int, ) {

        assertTrue("Expected background to be ColorDrawable but was not. $errorMessage", this.background is ColorDrawable)

        val actualBackgroundColor = (this.background as ColorDrawable).color

        assertTrue(errorMessage, expectedBackgroundColor == actualBackgroundColor)
    }

    fun Drawable.assertCreatedFromResourceId(errorMessage: String, expectedId:  Int) {
        val actualId = Shadows.shadowOf(this).createdFromResId
        assertTrue(errorMessage, expectedId == actualId)
    }

    fun List<Int>.clickSongSelectorListItems(listView: RecyclerView): Int {
        val timeBefore = SystemClock.currentGnssTimeClock().millis()

        val maxIndex = max(this)

        assertNotNull("Your recycler view adapter should not be null", listView.adapter)

        val expectedMinSize = maxIndex + 1

        val actualSize = listView.adapter!!.itemCount
        assertTrue(
            "RecyclerView was expected to contain item with index $maxIndex, but its size was $actualSize",
            actualSize >= expectedMinSize
        )

        if(actualSize >= expectedMinSize) {

            val firstItemViewHolder = (0 until actualSize)
                .asSequence()
                .mapNotNull {  listView.findViewHolderForAdapterPosition(it) }
                .firstOrNull()
                ?: throw AssertionError("No item is being displayed on songList RecyclerView, is it big enough to display one item?")

            this.forEach { i ->
                // setting height to ensure that all items are inflated
                listView.layout(0,0, listView.width, firstItemViewHolder.itemView.height * (expectedMinSize + 1))
                listView.scrollToPosition(i)
                shadowLooper.idleFor(5, TimeUnit.MILLISECONDS)

                var itemView = listView.findViewHolderForAdapterPosition(i)!!.itemView

                var checkBox =
                    itemView.findViewByString<CheckBox>("songSelectorItemCheckBox")

                assertEquals(
                    "songSelectorItemCheckBox should not be checked after clicks on mainMenuItemIdAddPlaylist",
                    false,
                    checkBox.isChecked
                )

                itemView.clickAndRun(5)

                itemView = listView.findViewHolderForAdapterPosition(i)!!.itemView
                checkBox = itemView.findViewByString<CheckBox>("songSelectorItemCheckBox")

                assertEquals(
                    "songSelectorItemCheckBox should be checked after clicks on the list item",
                    true,
                    checkBox.isChecked
                )

                itemView.assertBackgroundColor(
                    "SongSelector list items should change color to Color.LTGRAY when item is selected",
                    Color.LTGRAY
                )
            }

            val timeAfter = SystemClock.currentGnssTimeClock().millis()
            return (timeAfter - timeBefore).toInt()

        } else {
            throw IllegalStateException("size assertion was not effective")
        }
    }

    fun assertSongItem(errorMessage: String, itemView: View, song: SongFake) {
        val songItemTvArtist = itemView.findViewByString<TextView>("songItemTvArtist")
        val songItemTvTitle = itemView.findViewByString<TextView>("songItemTvTitle")
        val songItemTvDuration = itemView.findViewByString<TextView>("songItemTvDuration")

        assertEquals(errorMessage, song.artist, songItemTvArtist.text.toString())
        assertEquals(errorMessage, song.title, songItemTvTitle.text.toString())
        assertEquals(errorMessage, song.duration.timeString(), songItemTvDuration.text.toString())
    }

    fun assertViewStateIsPlayMusicState(
        songList: RecyclerView,
        fragmentContainer: FragmentContainerView
    ) {

        songList.assertSingleListItem(0){ itemView ->
            itemView().findViewByString<ImageButton>("songItemImgBtnPlayPause")
        }
        fragmentContainer.findViewByString<Button>("controllerBtnPlayPause")
    }

    fun addPlaylist(
        playlistName: String,
        selectedItemsIndex: List<Int>,
        songListView: RecyclerView,
        fragmentContainer: FragmentContainerView,
        testEmptyName: Boolean = false
    ): Int {
        val timeBefore = SystemClock.currentGnssTimeClock().millis()
        selectedItemsIndex.clickSongSelectorListItems(songListView) // might or might not increase clock time

        val addPlaylistButtonOk =
            fragmentContainer.findViewByString<Button>("addPlaylistBtnOk")

        if(testEmptyName) {
            addPlaylistButtonOk.clickAndRun(0) // might or might not increase clock time

            assertLastToastMessageEquals(
                errorMessage = "When addPlaylistEtPlaylistName is empty a toast message is expected after click on addPlaylistBtnOk",
                expectedMessage = "Add a name to your playlist"
            )
        }

        val addPlaylistEtPlaylistName =
            fragmentContainer.findViewByString<EditText>("addPlaylistEtPlaylistName")
        addPlaylistEtPlaylistName.setText(playlistName)

        addPlaylistButtonOk.clickAndRun(0) // might or might not increase clock time
        assertViewStateIsPlayMusicState(songListView, fragmentContainer)
        val timeAfter = SystemClock.currentGnssTimeClock().millis()
        return (timeAfter - timeBefore).toInt()
    }

    fun loadPlaylist(menuItemIdLoadPlaylist: String, expectedPlaylistNameList: List<String>, playlistToLoadIndex: Int): Int {
        val timeBefore = SystemClock.currentGnssTimeClock().millis()
        activity.clickMenuItemAndRun(menuItemIdLoadPlaylist) // might or might not increase clock time

        getLastAlertDialogWithShadow(
            "An AlertDialog should be displayed after click on mainMenuItemLoadPlaylist"
        ).also { (dialog, shadowDialog) ->
            val dialogItems = shadowDialog.items.map { it.toString() }

            assertEquals("Wrong list displayed on AlertDialog after click on mainMenuItemLoadPlaylist",
                expectedPlaylistNameList,
                dialogItems
            )
            shadowDialog.clickAndRunOnItem(playlistToLoadIndex, millis = 0) // might or might not increase clock time
        }
        val timeAfter = SystemClock.currentGnssTimeClock().millis()
        return (timeAfter - timeBefore).toInt()
    }

    inner class ControllerViews(
        val currentTv: TextView,
        val totalTv: TextView,
        val seekBar: SeekBar,
        val btnPlayPause: Button,
        val btnStop: Button
    ) {
        fun assertControllerState(errorMessage: String, songFake: SongFake, expectedPosition: Int) {

            val messageTotalTimeTv = "$errorMessage On controllerTvTotalTime text"
            assertEquals(messageTotalTimeTv, songFake.duration.timeString(), this.totalTv.text.toString())

            val messageSeekBar = "$errorMessage On controllerSeekBar progress"
            assertEquals(messageSeekBar, expectedPosition / 1000, this.seekBar.progress)

            val messageCurrentTimeTv = "$errorMessage On controllerTvCurrentTime text"
            assertEquals(messageCurrentTimeTv, expectedPosition.timeString(), this.currentTv.text.toString())
        }
    }

    fun FragmentContainerView.getControllerViews(): ControllerViews {

        return ControllerViews(
            currentTv = findViewByString("controllerTvCurrentTime"),
            totalTv=  findViewByString("controllerTvTotalTime"),
            seekBar = findViewByString("controllerSeekBar"),
            btnPlayPause = findViewByString("controllerBtnPlayPause"),
            btnStop = findViewByString("controllerBtnStop")
        )
    }

    fun MediaPlayer.assertControllerPlay(errorMessage: String, controllerViews: ControllerViews, expectedPosition: Int) {
        assertController(errorMessage, controllerViews, expectedPosition, expectedIsPlaying = true)
    }

    fun MediaPlayer.assertControllerPause(errorMessage: String, controllerViews: ControllerViews, expectedPosition: Int) {
        assertController(errorMessage, controllerViews, expectedPosition, expectedIsPlaying = false)
    }

    fun MediaPlayer.assertControllerStop(errorMessage: String, controllerViews: ControllerViews) {
        assertController(errorMessage, controllerViews, expectedPosition = 0, expectedIsPlaying = false)
    }

    private fun MediaPlayer.assertController(
        errorMessage: String, controllerViews: ControllerViews, expectedPosition: Int, expectedIsPlaying: Boolean) {

        assertEquals("$errorMessage On mediaPlayer isPlaying", expectedIsPlaying, isPlaying)

        val messageCurrentPosition = "$errorMessage On mediaPlayer currentPosition expected: $expectedPosition found: $currentPosition"
        assertTrue(messageCurrentPosition, abs(expectedPosition - currentPosition) < 100)

        val messageSeekBar = "$errorMessage On controllerSeekBar progress"
        assertEquals(messageSeekBar, expectedPosition / 1000, controllerViews.seekBar.progress)

        val messageCurrentTimeTv = "$errorMessage On controllerTvCurrentTime text"
        assertEquals(messageCurrentTimeTv, expectedPosition.timeString(), controllerViews.currentTv.text.toString())
    }

    fun adjustPlayerPositionToAvoidSyncIssues(): Int {
        // tests can have sync problems with solutions depending on which position the player is paused
        // to avoid issues we adjust player position before pausing if the player position can be in inconvenient position
        // this is only needed if playingTime had some change without hardcoded values

        val syncAdjustment = 1000 - (player.currentPosition % 1000) + 200
        shadowLooper.idleFor(Duration.ofMillis(syncAdjustment.toLong()))
        return syncAdjustment
    }
}