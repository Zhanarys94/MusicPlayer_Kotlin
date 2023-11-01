package org.hyperskill.musicplayer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import org.hyperskill.musicplayer.model.song.Song
import org.hyperskill.musicplayer.model.song.SongSelector
import org.hyperskill.musicplayer.model.song.SongState
import org.hyperskill.musicplayer.model.song.SongType
import java.util.EnumSet

class RecyclerAdapterSong : ListAdapter<SongType, RecyclerAdapterSong.SongViewHolder>(
    DataTypeDiffCallbackObj
) {
    private var onItemClickListener: OnItemClickListener? = null
    private var onItemLongClickListener: OnItemLongClickListener? = null
    private var onButtonPlayPauseClickListener: OnButtonPlayPauseClickListener? = null

    companion object {
        private const val VIEW_TYPE_SONG = 1
        private const val VIEW_TYPE_SONG_SELECTOR = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val holder = when (viewType) {
            VIEW_TYPE_SONG -> {
                val songView = inflater.inflate(R.layout.list_item_song, parent, false)
                SongViewHolder(songView)
            }
            VIEW_TYPE_SONG_SELECTOR -> {
                val songSelectorView = inflater
                    .inflate(R.layout.list_item_song_selector, parent, false)
                SongViewHolder(songSelectorView)
            }
            else -> throw UnsupportedOperationException("Unknown view type!")
        }
        return holder
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        throw UnsupportedOperationException()
    }

    override fun onBindViewHolder(
        holder: SongViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        when (getItem(position)) {
            is Song -> {
                holder.itemView.setOnLongClickListener {
                    onItemLongClickListener?.onLongClick(getItem(position) as Song, position)
                    true
                }
                holder.bind(
                    getItem(position),
                    onItemClickListener,
                    onButtonPlayPauseClickListener,
                    payloads
                )
            }

            is SongSelector -> {
                holder.bind(
                    getItem(position),
                    onItemClickListener,
                    onButtonPlayPauseClickListener,
                    payloads
                )
            }
        }
    }

     override fun getItemViewType(position: Int): Int {
         return when (getItem(position)) {
             is Song -> VIEW_TYPE_SONG
             is SongSelector -> VIEW_TYPE_SONG_SELECTOR
         }
    }

    inner class SongViewHolder(private val view: View) : ViewHolder(view) {
        fun bind(
            item: SongType,
            onItemClickListener: OnItemClickListener? = null,
            onButtonPlayPauseClick: OnButtonPlayPauseClickListener? = null,
            payloads: MutableList<Any>
        ) {
            when (item) {
                is Song -> bindCurrentPlaylist(
                    item, onButtonPlayPauseClick, payloads
                )
                is SongSelector -> {
                    bindLoadedPlaylist(
                        item, onItemClickListener, payloads
                    )
                }
            }
        }

        private fun bindCurrentPlaylist(
            item: Song,
            onButtonPlayPauseClick: OnButtonPlayPauseClickListener?,
            payloads: MutableList<Any>
        ) {
            val button = view.findViewById<ImageButton>(R.id.songItemImgBtnPlayPause)
            val artist = view.findViewById<TextView>(R.id.songItemTvArtist)
            val title = view.findViewById<TextView>(R.id.songItemTvTitle)
            val duration = view.findViewById<TextView>(R.id.songItemTvDuration)

            button.setOnClickListener {
                onButtonPlayPauseClick?.onClick(item, adapterPosition)
            }

            button.setImageResource(
                if (item.songState == SongState.PLAYING) R.drawable.ic_pause
                else R.drawable.ic_play
            )

            val changes = if (payloads.isEmpty()) {
                emptySet<ChangeField>()
            } else {
                EnumSet.noneOf(ChangeField::class.java).also { changes ->
                    payloads.forEach { payload ->
                        (payload as? Collection<*>)?.filterIsInstanceTo(changes)
                    }
                }
            }

            if (changes.isEmpty()) {
                artist.text = item.artist
                title.text = item.title
                duration.text = item.durationString
            }

            if (ChangeField.ARTIST in changes) {
                artist.text = item.artist
            }
            if (ChangeField.TITLE in changes) {
                title.text = item.title
            }
            if (ChangeField.DURATION in changes) {
                duration.text = item.durationString
            }
            if (ChangeField.SONG_STATE in changes) {
                if (item.songState == SongState.PLAYING) {
                    button.setImageResource(R.drawable.ic_pause)
                } else {
                    button.setImageResource(R.drawable.ic_play)
                }
            }
        }

        private fun bindLoadedPlaylist(
            item: SongSelector,
            onItemClickListener: OnItemClickListener?,
            payloads: MutableList<Any>
        ) {
            val checkBox = view.findViewById<CheckBox>(R.id.songSelectorItemCheckBox)
            val artist = view.findViewById<TextView>(R.id.songSelectorItemTvArtist)
            val title = view.findViewById<TextView>(R.id.songSelectorItemTvTitle)
            val duration = view.findViewById<TextView>(R.id.songSelectorItemTvDuration)

            itemView.setOnClickListener {
                onItemClickListener?.onClick(item, adapterPosition)
            }

            val changes = if (payloads.isEmpty()) {
                emptySet<ChangeField>()
            } else {
                EnumSet.noneOf(ChangeField::class.java).also { changes ->
                    payloads.forEach { payload ->
                        (payload as? Collection<*>)?.filterIsInstanceTo(changes)
                    }
                }
            }

            checkBox.isChecked = item.isSelected

            if (changes.isEmpty()) {
                artist.text = item.song.artist
                title.text = item.song.title
                duration.text = item.song.durationString
            }

            if (ChangeField.ARTIST in changes) {
                artist.text = item.song.artist
            }
            if (ChangeField.TITLE in changes) {
                title.text = item.song.title
            }
            if (ChangeField.DURATION in changes) {
                duration.text = item.song.durationString
            }
            if (ChangeField.IS_SELECTED in changes) {
                checkBox.isChecked = item.isSelected
            }
        }
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    fun setOnItemLongClickListener(onItemLongClickListener: OnItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener
    }

    fun setOnButtonPlayPauseClickListener(onButtonPlayPauseClickListener: OnButtonPlayPauseClickListener) {
        this.onButtonPlayPauseClickListener = onButtonPlayPauseClickListener
    }

/*    fun changeState(viewState: ViewState) {
        currentState = viewState
    }*/

    interface OnItemClickListener {
        fun onClick(songSelected: SongSelector, position: Int)
    }

    interface OnItemLongClickListener {
        fun onLongClick(songSelected: Song, position: Int)
    }

    interface OnButtonPlayPauseClickListener {
        fun onClick(song: Song, position: Int)
    }
}

object DataTypeDiffCallbackObj : DiffUtil.ItemCallback<SongType>() {

    override fun areItemsTheSame(
        oldItem: SongType,
        newItem: SongType
    ): Boolean {
        return when {
            oldItem is Song && newItem is Song -> oldItem.id == newItem.id
            oldItem is SongSelector && newItem is SongSelector ->
                oldItem.song.id == newItem.song.id
            else -> false
        }
    }

    override fun areContentsTheSame(
        oldItem: SongType,
        newItem: SongType
    ): Boolean {
        return when {
            oldItem is Song && newItem is Song -> oldItem == newItem
            oldItem is SongSelector && newItem is SongSelector -> oldItem == newItem
            else -> false
        }
    }

    override fun getChangePayload(oldItem: SongType, newItem: SongType): Any? {
        return when {
            oldItem is Song && newItem is Song -> listOfNotNull(
                ChangeField.ARTIST.takeIf { oldItem.artist != newItem.artist },
                ChangeField.TITLE.takeIf { oldItem.title != newItem.title },
                ChangeField.DURATION.takeIf { oldItem.duration != newItem.duration },
                ChangeField.SONG_STATE.takeIf { oldItem.songState != newItem.songState }
            )
            oldItem is SongSelector && newItem is SongSelector -> listOfNotNull(
                ChangeField.ARTIST.takeIf { oldItem.song.artist != newItem.song.artist },
                ChangeField.TITLE.takeIf { oldItem.song.title != newItem.song.title },
                ChangeField.DURATION.takeIf { oldItem.song.duration != newItem.song.duration },
                ChangeField.IS_SELECTED.takeIf { oldItem.isSelected != newItem.isSelected }
            )
            else -> null
        }
    }
}

enum class ChangeField {
    ARTIST, TITLE, DURATION, IS_SELECTED, SONG_STATE
}