package org.hyperskill.musicplayer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.hyperskill.musicplayer.databinding.MainAddPlaylistBinding
import org.hyperskill.musicplayer.databinding.MainPlayerControllerBinding

class MainAddPlaylistFragment : Fragment() {
    private var binding: MainAddPlaylistBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = MainAddPlaylistBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}