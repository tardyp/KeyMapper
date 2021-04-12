package io.github.sds100.keymapper.ui.fragment.fingerprint

import android.os.Bundle
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.data.model.options.FingerprintActionOptions
import io.github.sds100.keymapper.domain.actions.ActionData
import io.github.sds100.keymapper.domain.constraints.Constraint
import io.github.sds100.keymapper.ui.fragment.*
import io.github.sds100.keymapper.ui.mappings.fingerprintmap.ConfigFingerprintMapViewModel
import io.github.sds100.keymapper.ui.utils.getJsonSerializable
import io.github.sds100.keymapper.util.FragmentInfo
import io.github.sds100.keymapper.util.InjectorUtils
import io.github.sds100.keymapper.util.int
import io.github.sds100.keymapper.util.intArray
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Created by sds100 on 22/11/20.
 */
class ConfigFingerprintMapFragment : ConfigMappingFragment() {
    private val args by navArgs<ConfigFingerprintMapFragmentArgs>()

    override val viewModel: ConfigFingerprintMapViewModel
        by navGraphViewModels(R.id.nav_config_fingerprint_map) {
            InjectorUtils.provideConfigFingerprintMapViewModel(requireContext())
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //only load the fingerprint map if opening this fragment for the first time
        if (savedInstanceState == null) {
            viewModel.loadFingerprintMap(Json.decodeFromString(args.gestureId))
        }

        setFragmentResultListener(ConfigActionsFragment.CHOOSE_ACTION_REQUEST_KEY) { _, result ->
            result.getJsonSerializable<ActionData>(ChooseActionFragment.EXTRA_ACTION)?.let {
                viewModel.actionListViewModel.addAction(it)
            }
        }

        setFragmentResultListener(ConfigConstraintsFragment.CHOOSE_CONSTRAINT_REQUEST_KEY) { _, result ->
            result.getJsonSerializable<Constraint>(ChooseConstraintFragment.EXTRA_CONSTRAINT)?.let {
                viewModel.constraintListViewModel.onChosenNewConstraint(it)
            }
        }

        setFragmentResultListener(FingerprintActionOptionsFragment.REQUEST_KEY) { _, result ->
            result.getParcelable<FingerprintActionOptions>(OldBaseOptionsDialogFragment.EXTRA_OPTIONS)
                ?.let {
                    //TODO
//                    viewModel.actionListViewModel.setOptions(it)
                }
        }
    }

    override fun getFragmentInfoList() =
        intArray(R.array.config_fingerprint_map_fragments).map {
            when (it) {
                int(R.integer.fragment_id_action_list) ->
                    it to FingerprintConfigActionsFragment.Info()

                int(R.integer.fragment_id_fingerprint_map_options) ->
                    it to FingerprintMapOptionsFragment.Info()

                int(R.integer.fragment_id_constraint_list) ->
                    it to FingerprintConfigConstraintsFragment.Info()

                int(R.integer.fragment_id_constraints_and_options) ->
                    it to FragmentInfo(R.string.tab_constraints_and_more) {
                        ConstraintsAndOptionsFragment()
                    }

                else -> throw Exception("Don't know how to create FragmentInfo for this fragment $it")
            }
        }

    class ConstraintsAndOptionsFragment : TwoFragments(
        FingerprintMapOptionsFragment.Info(),
        FingerprintConfigConstraintsFragment.Info()
    )
}