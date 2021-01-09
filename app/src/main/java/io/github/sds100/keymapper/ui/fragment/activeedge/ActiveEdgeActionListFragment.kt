package io.github.sds100.keymapper.ui.fragment.activeedge

import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.data.model.options.ActiveEdgeActionOptions
import io.github.sds100.keymapper.data.viewmodel.ActionListViewModel
import io.github.sds100.keymapper.data.viewmodel.ConfigActiveEdgeViewModel
import io.github.sds100.keymapper.ui.fragment.ActionListFragment
import io.github.sds100.keymapper.util.InjectorUtils

/**
 * Created by sds100 on 22/11/20.
 */

class ActiveEdgeActionListFragment : ActionListFragment<ActiveEdgeActionOptions>() {
    private val mViewModel: ConfigActiveEdgeViewModel
        by navGraphViewModels(R.id.nav_config_active_edge) {
            InjectorUtils.provideConfigActiveEdgeViewModel(requireContext())
        }

    override val actionListViewModel: ActionListViewModel<ActiveEdgeActionOptions>
        get() = mViewModel.actionListViewModel

    override fun openActionOptionsFragment(options: ActiveEdgeActionOptions) {
        val direction = ConfigActiveEdgeFragmentDirections
            .actionConfigActiveEdgeFragmentToActionOptionsFragment(options)

        findNavController().navigate(direction)
    }
}