package io.github.sds100.keymapper.ui.fragment.keymap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyTouchHelper
import com.google.android.material.card.MaterialCardView
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.TriggerKeyBindingModel_
import io.github.sds100.keymapper.databinding.FragmentTriggerBinding
import io.github.sds100.keymapper.domain.mappings.keymap.trigger.TriggerKeyDevice
import io.github.sds100.keymapper.triggerKey
import io.github.sds100.keymapper.ui.ListState
import io.github.sds100.keymapper.ui.mappings.keymap.ConfigKeymapViewModel
import io.github.sds100.keymapper.ui.mappings.keymap.TriggerViewModel
import io.github.sds100.keymapper.util.*
import kotlinx.coroutines.flow.collectLatest
import splitties.alertdialog.appcompat.alertDialog
import splitties.alertdialog.appcompat.cancelButton
import splitties.alertdialog.appcompat.messageResource
import splitties.alertdialog.appcompat.okButton
import splitties.experimental.ExperimentalSplittiesApi
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Created by sds100 on 25/11/20.
 */

class TriggerFragment : Fragment() {

    class Info : FragmentInfo(
        R.string.trigger_header,
        R.string.url_trigger_guide,
        { TriggerFragment() }
    )

    private lateinit var binding: FragmentTriggerBinding

    private val triggerViewModel: TriggerViewModel by lazy {
        navGraphViewModels<ConfigKeymapViewModel>(R.id.nav_config_keymap) {
            InjectorUtils.provideConfigKeymapViewModel(requireContext())
        }.value.triggerViewModel
    }

    private val triggerKeyController = TriggerKeyController()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        FragmentTriggerBinding.inflate(inflater, container, false).apply {
            binding = this
            lifecycleOwner = viewLifecycleOwner

            return this.root
        }
    }

    private var parallelTriggerOrderDialog: AlertDialog? = null
    private var sequenceTriggerExplanationDialog: AlertDialog? = null

    @ExperimentalSplittiesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = triggerViewModel

        binding.epoxyRecyclerViewTriggers.adapter = triggerKeyController.adapter

        triggerViewModel.showEnableCapsLockKeyboardLayoutPrompt
            .collectWhenStarted(viewLifecycleOwner) {
                requireContext().alertDialog {
                    messageResource =
                        R.string.dialog_message_enable_physical_keyboard_caps_lock_a_keyboard_layout

                    okButton()

                    show()
                }
            }

        viewLifecycleScope.launchWhenResumed {
            triggerViewModel.state.collectLatest { state ->
                if (state.showParallelTriggerOrderExplanation
                    && parallelTriggerOrderDialog != null
                ) {
                    parallelTriggerOrderDialog = requireContext().alertDialog {
                        messageResource = R.string.dialog_message_parallel_trigger_order

                        okButton {
                            triggerViewModel.approvedParallelTriggerOrderExplanation()
                        }

                        show()
                    }
                } else {
                    parallelTriggerOrderDialog?.dismiss()
                    parallelTriggerOrderDialog = null
                }

                if (state.showSequenceTriggerExplanation && sequenceTriggerExplanationDialog != null) {
                    sequenceTriggerExplanationDialog = requireContext().alertDialog {
                        messageResource = R.string.dialog_message_sequence_trigger_explanation

                        okButton {
                            triggerViewModel.approvedSequenceTriggerExplanation()
                        }

                        show()
                    }
                } else {
                    sequenceTriggerExplanationDialog?.dismiss()
                    sequenceTriggerExplanationDialog = null
                }

                binding.enableTriggerKeyDragging(triggerKeyController)

                when (state.triggerKeyListModels) {
                    is ListState.Loaded ->
                        triggerKeyController.modelList = state.triggerKeyListModels.data

                    else -> triggerKeyController.modelList = emptyList()
                }
            }
        }

        triggerViewModel.showChooseDeviceDialog.collectWhenStarted(viewLifecycleOwner) { model ->

        }
    }

    override fun onResume() {
        super.onResume()

        triggerViewModel.rebuildUiState()
    }

    override fun onPause() {
        super.onPause()

        triggerViewModel.stopRecordingTrigger()
    }

    override fun onDestroyView() {
        parallelTriggerOrderDialog?.dismiss()
        parallelTriggerOrderDialog = null

        sequenceTriggerExplanationDialog?.dismiss()
        sequenceTriggerExplanationDialog = null

        super.onDestroyView()
    }

    private suspend fun showChooseDeviceDialog(devices: List<TriggerKeyDevice>) =
        suspendCoroutine<String> {
            requireContext().alertDialog {

                val idAny = "any"
                val idInternal = "internal"

                val deviceIds = devices.map { device ->
                    when (device) {
                        TriggerKeyDevice.Any -> idAny
                        TriggerKeyDevice.Internal -> idInternal
                        is TriggerKeyDevice.External -> device.descriptor
                    }
                }

                val deviceLabels = devices.map { device ->
                    when (device) {
                        TriggerKeyDevice.Any -> str(R.string.any_device)
                        TriggerKeyDevice.Internal -> str(R.string.this_device)
                        is TriggerKeyDevice.External -> device.name
                    }
                }.toTypedArray()

                setItems(deviceLabels) { _, index ->
                    val deviceId = deviceIds[index]

                    it.resume(deviceId)
                }

                cancelButton()
                show()
            }
        }

    private fun FragmentTriggerBinding.enableTriggerKeyDragging(controller: EpoxyController): ItemTouchHelper {
        return EpoxyTouchHelper.initDragging(controller)
            .withRecyclerView(epoxyRecyclerViewTriggers)
            .forVerticalList()
            .withTarget(TriggerKeyBindingModel_::class.java)
            .andCallbacks(object : EpoxyTouchHelper.DragCallbacks<TriggerKeyBindingModel_>() {

                override fun isDragEnabledForModel(model: TriggerKeyBindingModel_?): Boolean {
                    return model?.model()?.isDragDropEnabled ?: false
                }

                override fun onModelMoved(
                    fromPosition: Int,
                    toPosition: Int,
                    modelBeingMoved: TriggerKeyBindingModel_?,
                    itemView: View?
                ) {
                    triggerViewModel.onMoveTriggerKey(fromPosition, toPosition)
                }

                override fun onDragStarted(
                    model: TriggerKeyBindingModel_?,
                    itemView: View?,
                    adapterPosition: Int
                ) {
                    itemView?.findViewById<MaterialCardView>(R.id.cardView)?.isDragged = true
                }

                override fun onDragReleased(model: TriggerKeyBindingModel_?, itemView: View?) {
                    itemView?.findViewById<MaterialCardView>(R.id.cardView)?.isDragged = false
                }
            })
    }

    private inner class TriggerKeyController : EpoxyController() {
        var modelList: List<TriggerKeyListItemModel> = listOf()
            set(value) {
                field = value
                requestModelBuild()
            }

        override fun buildModels() {
            modelList.forEach { model ->
                triggerKey {
                    id(model.id)
                    model(model)

                    onRemoveClick { _ ->
                        triggerViewModel.onRemoveKeyClick(model.id)
                    }

                    onMoreClick { _ ->
                        triggerViewModel.onTriggerKeyOptionsClick(model.id)
                    }

                    onDeviceClick { _ ->
                        triggerViewModel.onChooseDeviceClick(model.id)
                    }
                }
            }
        }
    }
}