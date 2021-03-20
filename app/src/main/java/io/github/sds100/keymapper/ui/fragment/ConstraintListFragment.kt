package io.github.sds100.keymapper.ui.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import io.github.sds100.keymapper.data.viewmodel.ConstraintListViewModel
import io.github.sds100.keymapper.data.viewmodel.ConstraintListViewState
import io.github.sds100.keymapper.databinding.FragmentConstraintListBinding
import io.github.sds100.keymapper.ui.ListState
import io.github.sds100.keymapper.ui.UiStateProducer
import io.github.sds100.keymapper.util.viewLifecycleScope
import kotlinx.coroutines.flow.collectLatest
import splitties.toast.toast

/**
 * Created by sds100 on 29/11/20.
 */
abstract class ConstraintListFragment
    : RecyclerViewFragment<ConstraintListViewState, FragmentConstraintListBinding>() {

    companion object {
        const val CHOOSE_CONSTRAINT_REQUEST_KEY = "request_choose_constraint"
    }

    abstract val constraintListViewModel: ConstraintListViewModel

    override val stateProducer: UiStateProducer<ConstraintListViewState> by lazy {
        constraintListViewModel
    }

    override fun bind(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentConstraintListBinding.inflate(inflater, container, false).apply {
        lifecycleOwner = viewLifecycleOwner
    }

    override fun subscribeUi(binding: FragmentConstraintListBinding) {
        binding.viewModel = constraintListViewModel

        binding.setOnAddConstraintClick {
//            val direction = NavAppDirections.actionGlobalChooseConstraint(
//                CHOOSE_CONSTRAINT_REQUEST_KEY,
//                constraintListViewModel.supportedConstraintList.toTypedArray()
//            )
//            findNavController().navigate(direction) //TODO
        }

        viewLifecycleScope.launchWhenResumed {
            constraintListViewModel.showToast.collectLatest {
                toast(it)
            }
        }
    }

    override fun updateUi(
        binding: FragmentConstraintListBinding,
        state: ConstraintListViewState
    ) {
        binding.epoxyRecyclerViewConstraints.withModels {
            if (state.constraintList !is ListState.Loaded) return@withModels

            state.constraintList.data.forEach { model ->

            }
        }
    }
}