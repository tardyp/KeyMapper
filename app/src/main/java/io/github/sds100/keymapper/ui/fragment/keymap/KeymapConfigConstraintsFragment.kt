package io.github.sds100.keymapper.ui.fragment.keymap

import androidx.navigation.navGraphViewModels
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.ui.mappings.keymap.ConfigKeyMapViewModel
import io.github.sds100.keymapper.data.viewmodel.ConfigConstraintsViewModel
import io.github.sds100.keymapper.ui.fragment.ConfigConstraintsFragment
import io.github.sds100.keymapper.util.FragmentInfo
import io.github.sds100.keymapper.util.InjectorUtils

/**
 * Created by sds100 on 30/11/20.
 */
class KeymapConfigConstraintsFragment : ConfigConstraintsFragment() {
    class Info : FragmentInfo(
        R.string.constraint_list_header,
        R.string.url_constraints_guide,
        { KeymapConfigConstraintsFragment() }
    )

    override var isAppBarVisible = false

    private val configKeyMapViewModel: ConfigKeyMapViewModel
        by navGraphViewModels(R.id.nav_config_keymap) {
            InjectorUtils.provideConfigKeyMapViewModel(requireContext())
        }

    override val configConstraintsViewModel: ConfigConstraintsViewModel
        get() = configKeyMapViewModel.constraintListViewModel
}