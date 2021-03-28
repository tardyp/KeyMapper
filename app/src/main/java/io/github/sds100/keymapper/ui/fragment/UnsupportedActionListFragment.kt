package io.github.sds100.keymapper.ui.fragment

import androidx.fragment.app.activityViewModels
import com.airbnb.epoxy.EpoxyRecyclerView
import io.github.sds100.keymapper.data.model.UnsupportedActionListItem
import io.github.sds100.keymapper.data.viewmodel.UnsupportedActionListViewModel
import io.github.sds100.keymapper.simple
import io.github.sds100.keymapper.ui.ListUiState
import io.github.sds100.keymapper.util.InjectorUtils
import io.github.sds100.keymapper.util.TintType
import kotlinx.coroutines.flow.Flow

/**
 * Created by sds100 on 31/03/2020.
 */
class UnsupportedActionListFragment
    : SimpleRecyclerViewFragment<UnsupportedActionListItem>() {

    private val viewModel: UnsupportedActionListViewModel by activityViewModels {
        InjectorUtils.provideUnsupportedActionListViewModel(requireContext())
    }

    override val listItems: Flow<ListUiState<UnsupportedActionListItem>>
        get() = viewModel.state

    override fun populateList(
        recyclerView: EpoxyRecyclerView,
        listItems: List<UnsupportedActionListItem>
    ) {
        binding.epoxyRecyclerView.withModels {
            listItems.forEach { model ->
                simple {
                    id(model.id)
                    icon(model.icon)
                    tintType(TintType.ON_SURFACE)
                    primaryText(model.description)
                    secondaryText(model.reason)
                }
            }
        }
    }

    override fun rebuildUiState() {
        viewModel.rebuildUiState()
    }
}