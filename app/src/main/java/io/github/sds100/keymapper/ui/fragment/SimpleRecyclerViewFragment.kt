package io.github.sds100.keymapper.ui.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import com.airbnb.epoxy.EpoxyRecyclerView
import io.github.sds100.keymapper.databinding.FragmentSimpleRecyclerviewBinding
import io.github.sds100.keymapper.ui.ListState
import io.github.sds100.keymapper.util.viewLifecycleScope

/**
 * Created by sds100 on 22/02/2020.
 */
abstract class SimpleRecyclerViewFragment<T>
    : RecyclerViewFragment<ListState<T>, FragmentSimpleRecyclerviewBinding>() {

    override fun bind(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentSimpleRecyclerviewBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
        }

    override fun updateUi(binding: FragmentSimpleRecyclerviewBinding, state: ListState<T>) {
        when (state) {
            is ListState.Loaded -> {
                //set the state to loading until the recyclerview has finished populating
                binding.state = ListState.Loading<T>()

                viewLifecycleScope.launchWhenResumed {
                    populateRecyclerView(binding.epoxyRecyclerView, state.data)
                    binding.state = state
                }
            }

            else -> binding.state = state
        }
    }

    abstract fun populateRecyclerView(recyclerView: EpoxyRecyclerView, list: List<T>)

    override fun getBottomAppBar(binding: FragmentSimpleRecyclerviewBinding) = binding.appBar
}