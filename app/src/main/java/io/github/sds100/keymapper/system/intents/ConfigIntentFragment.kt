package io.github.sds100.keymapper.system.intents

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.addRepeatingJob
import androidx.navigation.fragment.findNavController
import com.airbnb.epoxy.EpoxyController
import io.github.sds100.keymapper.databinding.FragmentIntentActionTypeBinding
import io.github.sds100.keymapper.databinding.ListItemIntentExtraBoolBinding
import io.github.sds100.keymapper.intentExtraBool
import io.github.sds100.keymapper.intentExtraGeneric
import io.github.sds100.keymapper.system.intents.*
import io.github.sds100.keymapper.util.*
import io.github.sds100.keymapper.util.ui.showPopups
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import splitties.alertdialog.appcompat.alertDialog
import splitties.alertdialog.appcompat.message
import splitties.alertdialog.appcompat.okButton

/**
 * Created by sds100 on 30/03/2020.
 */

class ConfigIntentFragment : Fragment() {
    companion object {
        const val REQUEST_KEY = "request_intent"
        const val EXTRA_RESULT = "extra_config_intent_result"
    }

    private val viewModel: ConfigIntentViewModel by activityViewModels {
        Inject.configIntentViewModel(requireContext())
    }

    /**
     * Scoped to the lifecycle of the fragment's view (between onCreateView and onDestroyView)
     */
    private var _binding: FragmentIntentActionTypeBinding? = null
    val binding: FragmentIntentActionTypeBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        FragmentIntentActionTypeBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            _binding = this

            return this.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel

        viewModel.showPopups(this, binding)

        viewLifecycleOwner.addRepeatingJob(Lifecycle.State.RESUMED) {
            viewModel.returnResult.collectLatest { result ->
                setFragmentResult(
                    REQUEST_KEY,
                    bundleOf(EXTRA_RESULT to Json.encodeToString(result))
                )

                findNavController().navigateUp()
            }
        }

        viewLifecycleOwner.addRepeatingJob(Lifecycle.State.RESUMED) {
            viewModel.extraListItems.collectLatest { listItems ->
                binding.epoxyRecyclerViewExtras.withModels {
                    listItems.forEach {
                        bindExtra(it)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun EpoxyController.bindExtra(model: IntentExtraListItem) {
        when (model) {
            is GenericIntentExtraListItem -> intentExtraGeneric {

                id(model.uid)

                model(model)

                onRemoveClick { _ ->
                    viewModel.removeExtra(model.uid)
                }

                onShowExampleClick { _ ->
                   viewModel.onShowExtraExampleClick(model)
                }

                valueTextWatcher(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {

                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                    }

                    override fun afterTextChanged(s: Editable?) {
                        viewModel.setExtraValue(model.uid, s.toString())
                    }
                })

                nameTextWatcher(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {

                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                    }

                    override fun afterTextChanged(s: Editable?) {
                        viewModel.setExtraName(model.uid, s.toString())
                    }
                })
            }

            is BoolIntentExtraListItem -> intentExtraBool {
                id(model.uid)

                model(model)

                onRemoveClick { _ ->
                    viewModel.removeExtra(model.uid)
                }

                onBind { model, view, _ ->
                    (view.dataBinding as ListItemIntentExtraBoolBinding).apply {
                        radioButtonTrue.setOnCheckedChangeListener { _, isChecked ->
                            if (isChecked) viewModel.setExtraValue(model.model().uid, "true")
                        }

                        radioButtonFalse.setOnCheckedChangeListener { _, isChecked ->
                            if (isChecked) viewModel.setExtraValue(model.model().uid, "false")
                        }
                    }
                }
            }
        }
    }

}