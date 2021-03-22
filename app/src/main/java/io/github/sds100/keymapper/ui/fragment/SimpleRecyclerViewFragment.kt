package io.github.sds100.keymapper.ui.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import io.github.sds100.keymapper.databinding.FragmentSimpleRecyclerviewBinding

/**
 * Created by sds100 on 22/02/2020.
 */
abstract class SimpleRecyclerViewFragment<T>
    : RecyclerViewFragment<T, FragmentSimpleRecyclerviewBinding>() {

    override fun bind(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentSimpleRecyclerviewBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
        }

    override fun subscribeUi(binding: FragmentSimpleRecyclerviewBinding) {}

    override fun getProgressBar(binding: FragmentSimpleRecyclerviewBinding) = binding.progressBar
    override fun getRecyclerView(binding: FragmentSimpleRecyclerviewBinding) =
        binding.epoxyRecyclerView

    override fun getEmptyListPlaceHolder(binding: FragmentSimpleRecyclerviewBinding) =
        binding.emptyListPlaceHolder

    override fun getBottomAppBar(binding: FragmentSimpleRecyclerviewBinding) = binding.appBar
}