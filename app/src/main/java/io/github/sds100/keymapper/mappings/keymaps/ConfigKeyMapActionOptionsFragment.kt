package io.github.sds100.keymapper.mappings.keymaps

import androidx.navigation.navGraphViewModels
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.domain.mappings.keymap.KeyMap
import io.github.sds100.keymapper.domain.mappings.keymap.KeyMapAction
import io.github.sds100.keymapper.mappings.common.ConfigActionOptionsFragment
import io.github.sds100.keymapper.mappings.common.ConfigActionOptionsViewModel
import io.github.sds100.keymapper.ui.mappings.keymap.ConfigKeyMapViewModel
import io.github.sds100.keymapper.util.InjectorUtils

/**
 * Created by sds100 on 12/04/2021.
 */
class ConfigKeyMapActionOptionsFragment :
    ConfigActionOptionsFragment<KeyMap, KeyMapAction>() {

    private val configKeyMapViewModel: ConfigKeyMapViewModel by navGraphViewModels(R.id.nav_config_keymap) {
        InjectorUtils.provideConfigKeyMapViewModel(requireContext())
    }

    override val viewModel: ConfigActionOptionsViewModel<KeyMap, KeyMapAction>
        get() = configKeyMapViewModel.configActionOptionsViewModel
}