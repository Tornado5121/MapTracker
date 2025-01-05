package com.zhadko.mapsapp.screens.resultScreen

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zhadko.mapsapp.R
import com.zhadko.mapsapp.databinding.FragmentResultBinding

class ResultFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentResultBinding? = null
    val binding: FragmentResultBinding
        get() = _binding!!

    private val args by navArgs<ResultFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupView()
    }

    private fun setupView() {
        with(binding) {
            distanceValueTextView.text = args.result.distance.format(getString(R.string.result))
            timeValueTextView.text = args.result.time
            shareButton.setOnClickListener { shareResults() }
        }
    }

    private fun shareResults() {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "I went ${args.result.distance}km ${args.result.time}!")
        }
        startActivity(shareIntent)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}