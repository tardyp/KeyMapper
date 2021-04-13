package io.github.sds100.keymapper.ui.fragment.keymap

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.addRepeatingJob
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.domain.constraints.Constraint
import io.github.sds100.keymapper.ui.fragment.*
import io.github.sds100.keymapper.ui.mappings.keymap.ConfigKeyMapViewModel
import io.github.sds100.keymapper.ui.showUserResponseRequests
import io.github.sds100.keymapper.ui.utils.getJsonSerializable
import io.github.sds100.keymapper.util.FragmentInfo
import io.github.sds100.keymapper.util.InjectorUtils
import io.github.sds100.keymapper.util.int
import io.github.sds100.keymapper.util.intArray
import kotlinx.coroutines.flow.collectLatest

/**
 * Created by sds100 on 22/11/20.
 */
class ConfigKeymapFragment : ConfigMappingFragment() {

    private val args by navArgs<ConfigKeymapFragmentArgs>()

    override val viewModel: ConfigKeyMapViewModel by navGraphViewModels(R.id.nav_config_keymap) {
        InjectorUtils.provideConfigKeyMapViewModel(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //only load the keymap if opening this fragment for the first time
        if (savedInstanceState == null) {
            args.keymapUid.let {
                if (it == null) {
                    viewModel.loadNewKeymap()
                } else {
                    viewModel.loadKeymap(it)
                }
            }
        }

        setFragmentResultListener(ConfigConstraintsFragment.CHOOSE_CONSTRAINT_REQUEST_KEY) { _, result ->
            result.getJsonSerializable<Constraint>(ChooseConstraintFragment.EXTRA_CONSTRAINT)?.let {
                viewModel.configConstraintsViewModel.onChosenNewConstraint(it)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.addRepeatingJob(Lifecycle.State.RESUMED) {
            viewModel.configActionsViewModel.openEditOptions.collectLatest { actionUid ->
                viewModel.configActionOptionsViewModel.setActionToConfigure(actionUid)
                findNavController().navigate(ConfigKeymapFragmentDirections.actionConfigKeymapFragmentToActionOptionsFragment())
            }
        }

        viewLifecycleOwner.addRepeatingJob(Lifecycle.State.RESUMED) {
            viewModel.configTriggerViewModel.openEditOptions.collectLatest { triggerKeyUid ->
                viewModel.configTriggerKeyViewModel.setTriggerKeyToConfigure(triggerKeyUid)
                findNavController().navigate(ConfigKeymapFragmentDirections.actionTriggerKeyOptionsFragment())
            }
        }

        viewModel.configTriggerViewModel.showUserResponseRequests(this, binding)
        viewModel.configTriggerViewModel.optionsViewModel.showUserResponseRequests(this, binding)
    }

    override fun getFragmentInfoList() = intArray(R.array.config_keymap_fragments).map {
        when (it) {
            int(R.integer.fragment_id_trigger) -> it to TriggerFragment.Info()
            int(R.integer.fragment_id_trigger_options) -> it to ConfigTriggerOptionsFragment.Info()
            int(R.integer.fragment_id_constraint_list) -> it to KeymapConfigConstraintsFragment.Info()
            int(R.integer.fragment_id_action_list) -> it to KeyMapConfigActionsFragment.Info()

            int(R.integer.fragment_id_constraints_and_options) ->
                it to FragmentInfo(R.string.tab_constraints_and_more) {
                    ConstraintsAndOptionsFragment()
                }

            int(R.integer.fragment_id_trigger_and_action_list) ->
                it to FragmentInfo(R.string.tab_trigger_and_actions) { TriggerAndActionsFragment() }

            int(R.integer.fragment_id_config_keymap_all) ->
                it to FragmentInfo { AllFragments() }

            else -> throw Exception("Don't know how to create FragmentInfo for this fragment $it")
        }
    }

    class TriggerAndActionsFragment : TwoFragments(
        TriggerFragment.Info(),
        KeyMapConfigActionsFragment.Info()
    )

    class ConstraintsAndOptionsFragment : TwoFragments(
        ConfigTriggerOptionsFragment.Info(),
        KeymapConfigConstraintsFragment.Info()
    )

    class AllFragments : FourFragments(
        TriggerFragment.Info(),
        ConfigTriggerOptionsFragment.Info(),
        KeyMapConfigActionsFragment.Info(),
        KeymapConfigConstraintsFragment.Info()
    )
}