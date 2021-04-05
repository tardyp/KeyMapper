package io.github.sds100.keymapper.ui.fragment.keymap

import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.data.model.options.KeymapActionOptions
import io.github.sds100.keymapper.data.viewmodel.ActionListViewModel
import io.github.sds100.keymapper.domain.mappings.keymap.KeyMap
import io.github.sds100.keymapper.domain.mappings.keymap.KeyMapAction
import io.github.sds100.keymapper.ui.fragment.ActionListFragment
import io.github.sds100.keymapper.ui.mappings.keymap.ConfigKeyMapViewModel
import io.github.sds100.keymapper.util.FragmentInfo
import io.github.sds100.keymapper.util.InjectorUtils

/**
 * Created by sds100 on 22/11/20.
 */

class KeymapActionListFragment : ActionListFragment<KeymapActionOptions, KeyMapAction>() {

    class Info : FragmentInfo(
        R.string.action_list_header,
        R.string.url_action_guide,
        { KeymapActionListFragment() }
    )

    override var isAppBarVisible = false

    private val configKeyMapViewModel: ConfigKeyMapViewModel by navGraphViewModels(R.id.nav_config_keymap) {
        InjectorUtils.provideConfigKeyMapViewModel(requireContext())
    }

    override val actionListViewModel: ActionListViewModel<KeyMapAction, KeyMap>
        get() = configKeyMapViewModel.actionListViewModel

    override fun openActionOptionsFragment(options: KeymapActionOptions) {
        val direction = ConfigKeymapFragmentDirections.actionConfigKeymapFragmentToActionOptionsFragment(options)
        findNavController().navigate(direction)
    }
}