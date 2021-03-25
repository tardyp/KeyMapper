package io.github.sds100.keymapper.ui.fragment.keymap

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.addRepeatingJob
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.data.model.options.KeymapActionOptions
import io.github.sds100.keymapper.data.model.options.TriggerKeyOptions
import io.github.sds100.keymapper.domain.constraints.Constraint
import io.github.sds100.keymapper.ui.fragment.*
import io.github.sds100.keymapper.ui.mappings.keymap.ConfigKeymapViewModel
import io.github.sds100.keymapper.ui.utils.getJsonSerializable
import io.github.sds100.keymapper.util.*
import kotlinx.coroutines.flow.collectLatest

/**
 * Created by sds100 on 22/11/20.
 */
class ConfigKeymapFragment : ConfigMappingFragment() {


    private val args by navArgs<ConfigKeymapFragmentArgs>()

    override val viewModel: ConfigKeymapViewModel by navGraphViewModels(R.id.nav_config_keymap) {
        InjectorUtils.provideConfigKeymapViewModel(requireContext())
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

        setFragmentResultListener(ConstraintListFragment.CHOOSE_CONSTRAINT_REQUEST_KEY) { _, result ->
            result.getJsonSerializable<Constraint>(ChooseConstraintFragment.EXTRA_CONSTRAINT)?.let {
                viewModel.constraintListViewModel.addConstraint(it)
            }
        }

        setFragmentResultListener(KeymapActionOptionsFragment.REQUEST_KEY) { _, result ->
            result.getParcelable<KeymapActionOptions>(BaseOptionsDialogFragment.EXTRA_OPTIONS)
                ?.let {
                    //TODO
                }
        }

        setFragmentResultListener(TriggerKeyOptionsFragment.REQUEST_KEY) { _, result ->
            result.getParcelable<TriggerKeyOptions>(BaseOptionsDialogFragment.EXTRA_OPTIONS)?.let {
                viewModel.triggerViewModel.setTriggerKeyOptions(it)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.addRepeatingJob(Lifecycle.State.RESUMED) {
            viewModel.createLauncherShortcutLabel.collectLatest {
                val label = requireContext().editTextStringAlertDialog(
                    viewLifecycleOwner,
                    hint = str(R.string.hint_shortcut_name),
                    allowEmpty = false
                ) ?: return@collectLatest

                viewModel.createLauncherShortcut(label)
            }
        }
    }

    override fun getFragmentInfoList() = intArray(R.array.config_keymap_fragments).map {
        when (it) {
            int(R.integer.fragment_id_trigger) -> it to TriggerFragment.Info()
            int(R.integer.fragment_id_trigger_options) -> it to TriggerOptionsFragment.Info()
            int(R.integer.fragment_id_constraint_list) -> it to KeymapConstraintListFragment.Info()
            int(R.integer.fragment_id_action_list) -> it to KeymapActionListFragment.Info()

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
        KeymapActionListFragment.Info()
    )

    class ConstraintsAndOptionsFragment : TwoFragments(
        TriggerOptionsFragment.Info(),
        KeymapConstraintListFragment.Info()
    )

    class AllFragments : FourFragments(
        TriggerFragment.Info(),
        TriggerOptionsFragment.Info(),
        KeymapActionListFragment.Info(),
        KeymapConstraintListFragment.Info()
    )
}