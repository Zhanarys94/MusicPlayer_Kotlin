package org.hyperskill.musicplayer.playlist

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.hyperskill.musicplayer.databinding.MainAddPlaylistBinding

class MainAddPlaylistFragment : Fragment() {
    private var binding: MainAddPlaylistBinding? = null
    private var interactionListener: AddPlaylistFragmentInteractionListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        interactionListener = context as AddPlaylistFragmentInteractionListener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = MainAddPlaylistBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val playlistNameTextField = binding!!.addPlaylistEtPlaylistName
        val okButton = binding!!.addPlaylistBtnOk.apply {
            setOnClickListener {
                requestFocus()
                interactionListener!!.onOkButtonClick(playlistNameTextField.text.toString())
            }
        }
        val cancelButton = binding!!.addPlaylistBtnCancel.apply {
            setOnClickListener {
                interactionListener!!.onCancelButtonClick()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onDetach() {
        super.onDetach()
        interactionListener = null
    }

    interface AddPlaylistFragmentInteractionListener {
        fun onCancelButtonClick()

        fun onOkButtonClick(name: String)
    }
}