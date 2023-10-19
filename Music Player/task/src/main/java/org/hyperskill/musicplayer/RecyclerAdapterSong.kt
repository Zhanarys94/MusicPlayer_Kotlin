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
import org.hyperskill.musicplayer.model.DataType
import org.hyperskill.musicplayer.model.PlaylistType
import org.hyperskill.musicplayer.model.SongState
import org.hyperskill.musicplayer.model.ViewState
import java.util.ArrayList
import java.util.EnumSet

class RecyclerAdapterSong(
    private var currentState: ViewState
) : ListAdapter<DataType, RecyclerAdapterSong.SongViewHolder>(
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
            else -> {
                val emptyView = inflater.inflate(R.layout.list_item_empty, parent, false)
                SongViewHolder(emptyView)
            }
        }
        return holder
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        throw UnsupportedOperationException()
    }

    override fun onBindViewHolder(
        holder: SongViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        when (currentState) {
            ViewState.PLAY_MUSIC -> {
                holder.itemView.setOnLongClickListener {
                    onItemLongClickListener?.onLongClick(getItem(position) as DataType.Song, position)
                    true
                }
                holder.bind(
                    getItem(position),
                    currentState,
                    onItemClickListener,
                    onButtonPlayPauseClickListener,
                    payloads
                )
            }

            ViewState.ADD_PLAYLIST -> {
                holder.bind(
                    getItem(position),
                    currentState,
                    onItemClickListener,
                    onButtonPlayPauseClickListener,
                    payloads
                )
            }
        }
    }

     override fun getItemViewType(position: Int): Int {
         return when (getItem(position)) {
             is DataType.Song -> VIEW_TYPE_SONG
             is DataType.SongSelector -> VIEW_TYPE_SONG_SELECTOR
         }
    }

    override fun submitList(list: MutableList<DataType>?) {
        if (list == currentList) {
            super.submitList(list.let { list.toList() })
        } else {
            super.submitList(list)
        }
    }

    inner class SongViewHolder(private val view: View) : ViewHolder(view) {
        fun bind(
            item: DataType,
            currentState: ViewState,
            onItemClickListener: OnItemClickListener? = null,
            onButtonPlayPauseClick: OnButtonPlayPauseClickListener? = null,
            payloads: MutableList<Any>
        ) {
            when (currentState) {
                ViewState.PLAY_MUSIC -> bindCurrentPlaylist(
                    item as DataType.Song, onButtonPlayPauseClick, payloads
                )
                ViewState.ADD_PLAYLIST -> {
                    bindLoadedPlaylist(
                        item as DataType.SongSelector, onItemClickListener, payloads
                    )
                }
            }
        }

        private fun bindCurrentPlaylist(
            item: DataType.Song,
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


            button.setImageResource(R.drawable.ic_play)
/*            if (item.songState == SongState.PLAYED) {
                button.setImageResource(R.drawable.ic_pause)
            } else {
                button.setImageResource(R.drawable.ic_play)
            }*/

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
                if (item.songState == SongState.PLAYED) {
                    button.setImageResource(R.drawable.ic_pause)
                } else {
                    button.setImageResource(R.drawable.ic_play)
                }
            }
        }

        private fun bindLoadedPlaylist(
            item: DataType.SongSelector,
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

    fun changeState(viewState: ViewState) {
        currentState = viewState
    }

    interface OnItemClickListener {
        fun onClick(songSelected: DataType.SongSelector, position: Int)
    }

    interface OnItemLongClickListener {
        fun onLongClick(songSelected: DataType.Song, position: Int)
    }

    interface OnButtonPlayPauseClickListener {
        fun onClick(song: DataType.Song, position: Int)
    }
}

object DataTypeDiffCallbackObj : DiffUtil.ItemCallback<DataType>() {

    override fun areItemsTheSame(
        oldItem: DataType,
        newItem: DataType
    ): Boolean {
        return when {
            oldItem is DataType.Song && newItem is DataType.Song -> oldItem.id == newItem.id
            oldItem is DataType.SongSelector && newItem is DataType.SongSelector ->
                oldItem.song.id == newItem.song.id
            else -> false
        }
    }

    override fun areContentsTheSame(
        oldItem: DataType,
        newItem: DataType
    ): Boolean {
        return when {
            oldItem is DataType.Song && newItem is DataType.Song -> oldItem == newItem
            oldItem is DataType.SongSelector && newItem is DataType.SongSelector -> oldItem == newItem
            else -> false
        }
    }

    override fun getChangePayload(oldItem: DataType, newItem: DataType): Any? {
        return when {
            oldItem is DataType.Song && newItem is DataType.Song -> listOfNotNull(
                ChangeField.ARTIST.takeIf { oldItem.artist != newItem.artist },
                ChangeField.TITLE.takeIf { oldItem.title != newItem.title },
                ChangeField.DURATION.takeIf { oldItem.duration != newItem.duration },
                ChangeField.SONG_STATE.takeIf { oldItem.songState != newItem.songState }
            )
            oldItem is DataType.SongSelector && newItem is DataType.SongSelector -> listOfNotNull(
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