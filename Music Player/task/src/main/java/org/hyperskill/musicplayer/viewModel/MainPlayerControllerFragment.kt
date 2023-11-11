package org.hyperskill.musicplayer.viewModel

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import org.hyperskill.musicplayer.databinding.MainPlayerControllerBinding

class MainPlayerControllerFragment : Fragment() {
    private var binding: MainPlayerControllerBinding? = null
    private var onFragmentInteractionListener: OnFragmentInteractionListener? = null
    private val viewModel: MainActivityViewModel by activityViewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onFragmentInteractionListener = context as OnFragmentInteractionListener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = MainPlayerControllerBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val playPauseButton = binding!!.controllerBtnPlayPause.apply {
            setOnClickListener {
                onFragmentInteractionListener?.fragmentPlayPauseButtonClick()
            }
        }
        val stopButton = binding!!.controllerBtnStop.apply {
            setOnClickListener {
                onFragmentInteractionListener?.fragmentStopButtonClick()
            }
        }
        val currentTime = binding!!.controllerTvCurrentTime
        val totalTime = binding!!.controllerTvTotalTime
        val seekBar = binding!!.controllerSeekBar

        viewModel.currentSong.observe(this) { song ->
            totalTime.text = song.durationString
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onDetach() {
        super.onDetach()
        onFragmentInteractionListener = null
    }

    interface OnFragmentInteractionListener {
        fun fragmentPlayPauseButtonClick()

        fun fragmentStopButtonClick()
    }
}