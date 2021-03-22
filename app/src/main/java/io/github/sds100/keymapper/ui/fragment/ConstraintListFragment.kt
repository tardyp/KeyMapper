package io.github.sds100.keymapper.ui.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import com.airbnb.epoxy.EpoxyRecyclerView
import io.github.sds100.keymapper.constraint
import io.github.sds100.keymapper.data.viewmodel.ConstraintListViewModel
import io.github.sds100.keymapper.databinding.FragmentConstraintListBinding
import io.github.sds100.keymapper.ui.ListUiState
import io.github.sds100.keymapper.ui.constraints.ConstraintListItem
import io.github.sds100.keymapper.util.viewLifecycleScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import splitties.toast.toast

/**
 * Created by sds100 on 29/11/20.
 */
abstract class ConstraintListFragment
    : RecyclerViewFragment<ConstraintListItem, FragmentConstraintListBinding>() {

    companion object {
        const val CHOOSE_CONSTRAINT_REQUEST_KEY = "request_choose_constraint"
    }

    abstract val constraintListViewModel: ConstraintListViewModel

    override val listItems: Flow<ListUiState<ConstraintListItem>>
        get() = constraintListViewModel.state.map { it.constraintList }

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

    override fun populateList(
        recyclerView: EpoxyRecyclerView,
        listItems: List<ConstraintListItem>
    ) {
        recyclerView.withModels {
            listItems.forEach { model ->
                constraint {
                    model(model)
                    onCardClick { _ ->
                        constraintListViewModel.onListItemClick(model.id)
                    }

                    onRemoveClick { _ ->
                        constraintListViewModel.onRemoveConstraintClick(model.id)
                    }
                }
            }
        }
    }

    override fun rebuildUiState() = constraintListViewModel.rebuildUiState()
    override fun getRecyclerView(binding: FragmentConstraintListBinding) = binding.epoxyRecyclerView
    override fun getProgressBar(binding: FragmentConstraintListBinding) = binding.progressBar
    override fun getEmptyListPlaceHolder(binding: FragmentConstraintListBinding) =
        binding.emptyListPlaceHolder
}