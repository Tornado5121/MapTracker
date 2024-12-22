package com.zhadko.mapsapp.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

open class BaseFragment<VB : ViewBinding>(private val vbFactory: (LayoutInflater, ViewGroup?, Boolean) -> VB) :
    Fragment() {

    private var _binding: VB? = null
    val binding: VB
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = vbFactory(inflater, container, false)
        return binding.root
    }
}