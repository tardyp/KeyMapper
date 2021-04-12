package io.github.sds100.keymapper.mappings.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.addRepeatingJob
import androidx.navigation.navGraphViewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.data.viewmodel.ConfigKeyMapActionOptionsViewModel
import io.github.sds100.keymapper.databinding.FragmentOptionsBinding
import io.github.sds100.keymapper.divider
import io.github.sds100.keymapper.domain.actions.Action
import io.github.sds100.keymapper.ui.*
import io.github.sds100.keymapper.ui.mappings.keymap.ConfigKeyMapViewModel
import io.github.sds100.keymapper.ui.utils.configuredCheckBox
import io.github.sds100.keymapper.ui.utils.configuredRadioButtonPair
import io.github.sds100.keymapper.ui.utils.configuredSlider
import io.github.sds100.keymapper.util.InjectorUtils
import io.github.sds100.keymapper.util.UrlUtils
import io.github.sds100.keymapper.util.str
import kotlinx.coroutines.flow.collectLatest

/**
 * Created by sds100 on 27/06/2020.
 */
abstract class ConfigActionOptionsFragment<M : Mapping<A>, A : Action> : BottomSheetDialogFragment() {

    companion object {
        const val REQUEST_KEY = "request_choose_action_options"
    }

    abstract val viewModel: ConfigActionOptionsViewModel<M, A>

    /**
     * Scoped to the lifecycle of the fragment's view (between onCreateView and onDestroyView)
     */
    private var _binding: FragmentOptionsBinding? = null
    private val binding: FragmentOptionsBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        FragmentOptionsBinding.inflate(inflater).apply {
            lifecycleOwner = viewLifecycleOwner
            _binding = this

            return this.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dialog = requireDialog() as BottomSheetDialog
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

        viewLifecycleOwner.addRepeatingJob(Lifecycle.State.RESUMED) {
            viewModel.state.collectLatest { state ->
                binding.showProgressBar = state.showProgressBar
                populateList(state.listItems)
            }
        }

        binding.setOnHelpClick {
            UrlUtils.openUrl(requireContext(), str(R.string.url_keymap_action_options_guide))
        }

        binding.setOnDoneClick {
            dismiss()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun populateList(listItems: List<ListItem>) {
        binding.epoxyRecyclerView.withModels {
            listItems.forEach { model ->
                if (model is RadioButtonPairListItem) {
                    configuredRadioButtonPair(
                        this@ConfigActionOptionsFragment,
                        model
                    ) { id, isChecked ->
                        viewModel.setRadioButtonValue(id, isChecked)
                    }
                }

                if (model is CheckBoxListItem) {
                    configuredCheckBox(this@ConfigActionOptionsFragment, model) {
                        viewModel.setCheckboxValue(model.id, it)
                    }
                }

                if (model is SliderListItem) {
                    configuredSlider(this@ConfigActionOptionsFragment, model) {
                        viewModel.setSliderValue(model.id, it)
                    }
                }

                if (model is DividerListItem) {
                    divider { id(model.id) }
                }
            }
        }
    }
}