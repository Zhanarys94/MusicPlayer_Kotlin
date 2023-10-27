package org.hyperskill.musicplayer

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import org.hyperskill.musicplayer.databinding.MainPlayerControllerBinding
import org.hyperskill.musicplayer.viewModel.SongViewModel

class MainPlayerControllerFragment : Fragment() {
    private var binding: MainPlayerControllerBinding? = null
    private var onFragmentInteractionListener: OnFragmentInteractionListener? = null
    private val currentSongViewModel: SongViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = MainPlayerControllerBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onFragmentInteractionListener = context as OnFragmentInteractionListener
    }

    override fun onDetach() {
        super.onDetach()
        onFragmentInteractionListener = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val playPauseButton = binding!!.controllerBtnPlayPause.apply {
            isClickable = true
            setOnClickListener {
                onFragmentInteractionListener?.onPlayPauseButtonClick()
            }
        }
        val stopButton = binding!!.controllerBtnStop.apply {
            isClickable = true
            setOnClickListener {
                onFragmentInteractionListener?.onStopButtonClick()
            }
        }
        val currentTime = binding!!.controllerTvCurrentTime
        val totalTime = binding!!.controllerTvTotalTime
        val seekBar = binding!!.controllerSeekBar

        currentSongViewModel.currentSong.observe(this) { song ->
            totalTime.text = song.durationString
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    interface OnFragmentInteractionListener {
        fun onPlayPauseButtonClick()

        fun onStopButtonClick()
    }
}