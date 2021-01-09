package io.github.sds100.keymapper.ui.fragment.activeedge

import androidx.navigation.navGraphViewModels
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.data.viewmodel.ConfigActiveEdgeViewModel
import io.github.sds100.keymapper.data.viewmodel.ConfigFingerprintMapViewModel
import io.github.sds100.keymapper.data.viewmodel.ConstraintListViewModel
import io.github.sds100.keymapper.ui.fragment.ConstraintListFragment
import io.github.sds100.keymapper.util.InjectorUtils

/**
 * Created by sds100 on 30/11/20.
 */
class ActiveEdgeConstraintListFragment : ConstraintListFragment() {

    private val mViewModel: ConfigActiveEdgeViewModel
        by navGraphViewModels(R.id.nav_config_active_edge) {
            InjectorUtils.provideConfigActiveEdgeViewModel(requireContext())
        }

    override val constraintListViewModel: ConstraintListViewModel
        get() = mViewModel.constraintListViewModel
}