package io.github.sds100.keymapper.ui.fragment.fingerprint

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import com.airbnb.epoxy.EpoxyControllerAdapter
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.data.model.options.FingerprintActionOptions
import io.github.sds100.keymapper.data.viewmodel.FingerprintActionOptionsViewModel
import io.github.sds100.keymapper.databinding.FragmentFingerprintActionOptionsBinding
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.FingerprintMap
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.FingerprintMapAction
import io.github.sds100.keymapper.domain.mappings.keymap.KeyMap
import io.github.sds100.keymapper.domain.mappings.keymap.KeyMapAction
import io.github.sds100.keymapper.mappings.common.ConfigActionOptionsViewModel
import io.github.sds100.keymapper.mappings.common.OptionsBottomSheetFragment
import io.github.sds100.keymapper.mappings.keymaps.ConfigKeyMapActionOptionsFragment
import io.github.sds100.keymapper.ui.fragment.ConfigActionsFragment
import io.github.sds100.keymapper.ui.fragment.OldBaseOptionsDialogFragment
import io.github.sds100.keymapper.ui.mappings.fingerprintmap.ConfigFingerprintMapViewModel
import io.github.sds100.keymapper.ui.mappings.keymap.ConfigKeyMapViewModel
import io.github.sds100.keymapper.util.InjectorUtils
import io.github.sds100.keymapper.util.str

/**
 * Created by sds100 on 27/06/2020.
 */
class ConfigFingerprintMapActionFragment : OptionsBottomSheetFragment() {

    private val configFingerprintMapViewModel: ConfigFingerprintMapViewModel by navGraphViewModels(R.id.nav_config_fingerprint_map) {
        InjectorUtils.provideConfigFingerprintMapViewModel(requireContext())
    }

    override val viewModel: ConfigActionOptionsViewModel<FingerprintMap, FingerprintMapAction>
        get() = configFingerprintMapViewModel.configActionOptionsViewModel

    override val url: String
        get() = str(R.string.url_fingerprint_action_options_guide)
}