package io.github.sds100.keymapper.ui.fragment.fingerprint

import androidx.navigation.navGraphViewModels
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.data.viewmodel.ConfigConstraintsViewModel
import io.github.sds100.keymapper.ui.fragment.ConfigConstraintsFragment
import io.github.sds100.keymapper.ui.mappings.fingerprintmap.ConfigFingerprintMapViewModel
import io.github.sds100.keymapper.util.FragmentInfo
import io.github.sds100.keymapper.util.InjectorUtils

/**
 * Created by sds100 on 30/11/20.
 */
class FingerprintConfigConstraintsFragment : ConfigConstraintsFragment() {

    class Info : FragmentInfo(
        R.string.constraint_list_header,
        R.string.url_constraints_guide,
        { FingerprintConfigConstraintsFragment() }
    )

    override var isAppBarVisible = false

    private val viewModel: ConfigFingerprintMapViewModel
        by navGraphViewModels(R.id.nav_config_fingerprint_map) {
            InjectorUtils.provideConfigFingerprintMapViewModel(requireContext())
        }

    override val configConstraintsViewModel: ConfigConstraintsViewModel
        get() = viewModel.configConstraintsViewModel
}