package io.github.sds100.keymapper.ui.fragment.keymap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.addRepeatingJob
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyRecyclerView
import com.airbnb.epoxy.EpoxyTouchHelper
import com.google.android.material.card.MaterialCardView
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.TriggerKeyBindingModel_
import io.github.sds100.keymapper.databinding.FragmentTriggerBinding
import io.github.sds100.keymapper.domain.mappings.keymap.trigger.TriggerKeyDevice
import io.github.sds100.keymapper.fixError
import io.github.sds100.keymapper.triggerKey
import io.github.sds100.keymapper.ui.ListUiState
import io.github.sds100.keymapper.ui.fragment.RecyclerViewFragment
import io.github.sds100.keymapper.ui.mappings.keymap.ConfigKeyMapViewModel
import io.github.sds100.keymapper.ui.mappings.keymap.ConfigKeyMapTriggerViewModel
import io.github.sds100.keymapper.util.FragmentInfo
import io.github.sds100.keymapper.util.InjectorUtils
import io.github.sds100.keymapper.util.collectWhenStarted
import io.github.sds100.keymapper.util.str
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import splitties.alertdialog.appcompat.alertDialog
import splitties.alertdialog.appcompat.cancelButton
import splitties.alertdialog.appcompat.messageResource
import splitties.alertdialog.appcompat.okButton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Created by sds100 on 25/11/20.
 */

class TriggerFragment : RecyclerViewFragment<TriggerKeyListItem, FragmentTriggerBinding>() {

    class Info : FragmentInfo(
        R.string.trigger_header,
        R.string.url_trigger_guide,
        { TriggerFragment() }
    )

    private val configKeyMapTriggerViewModel: ConfigKeyMapTriggerViewModel by lazy {
        navGraphViewModels<ConfigKeyMapViewModel>(R.id.nav_config_keymap) {
            InjectorUtils.provideConfigKeyMapViewModel(requireContext())
        }.value.triggerViewModel
    }

    private val triggerKeyController = TriggerKeyController()

    override val listItems: Flow<ListUiState<TriggerKeyListItem>>
        get() = configKeyMapTriggerViewModel.state.map { it.triggerKeyListItems }

    override fun bind(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentTriggerBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
        }

    private var parallelTriggerOrderDialog: AlertDialog? = null
    private var sequenceTriggerExplanationDialog: AlertDialog? = null

    override fun subscribeUi(binding: FragmentTriggerBinding) {
        binding.viewModel = configKeyMapTriggerViewModel

        binding.recyclerViewTriggerKeys.adapter = triggerKeyController.adapter

        configKeyMapTriggerViewModel.showEnableCapsLockKeyboardLayoutPrompt
            .collectWhenStarted(viewLifecycleOwner) {
                requireContext().alertDialog {
                    messageResource =
                        R.string.dialog_message_enable_physical_keyboard_caps_lock_a_keyboard_layout

                    okButton()

                    show()
                }
            }

        viewLifecycleOwner.addRepeatingJob(Lifecycle.State.RESUMED) {
            configKeyMapTriggerViewModel.state.collectLatest { state ->
                if (state.showParallelTriggerOrderExplanation
                    && parallelTriggerOrderDialog != null
                ) {
                    parallelTriggerOrderDialog = requireContext().alertDialog {
                        messageResource = R.string.dialog_message_parallel_trigger_order

                        okButton {
                            configKeyMapTriggerViewModel.approvedParallelTriggerOrderExplanation()
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
                            configKeyMapTriggerViewModel.approvedSequenceTriggerExplanation()
                        }

                        show()
                    }
                } else {
                    sequenceTriggerExplanationDialog?.dismiss()
                    sequenceTriggerExplanationDialog = null
                }

                binding.enableTriggerKeyDragging(triggerKeyController)

                binding.recyclerViewError.withModels {
                    state.errorListItems.forEach {
                        fixError {
                            id(it.id)
                            model(it)

                            onFixClick { _ ->
                                configKeyMapTriggerViewModel.fixError(it.id)
                            }
                        }
                    }
                }
            }
        }

        configKeyMapTriggerViewModel.showChooseDeviceDialog.collectWhenStarted(viewLifecycleOwner) { model ->

        }
    }

    override fun populateList(
        recyclerView: EpoxyRecyclerView,
        listItems: List<TriggerKeyListItem>
    ) {
        triggerKeyController.modelList = listItems
    }

    override fun getRecyclerView(binding: FragmentTriggerBinding) = binding.recyclerViewTriggerKeys
    override fun getProgressBar(binding: FragmentTriggerBinding) = binding.progressBar
    override fun getEmptyListPlaceHolder(binding: FragmentTriggerBinding) =
        binding.emptyListPlaceHolder

    override fun onPause() {
        super.onPause()

        configKeyMapTriggerViewModel.stopRecordingTrigger()
    }

    override fun onDestroyView() {
        parallelTriggerOrderDialog?.dismiss()
        parallelTriggerOrderDialog = null

        sequenceTriggerExplanationDialog?.dismiss()
        sequenceTriggerExplanationDialog = null

        super.onDestroyView()
    }

    //TODO
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
            .withRecyclerView(recyclerViewTriggerKeys)
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
                    configKeyMapTriggerViewModel.onMoveTriggerKey(fromPosition, toPosition)
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
        var modelList: List<TriggerKeyListItem> = listOf()
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
                        configKeyMapTriggerViewModel.onRemoveKeyClick(model.id)
                    }

                    onMoreClick { _ ->
                        configKeyMapTriggerViewModel.onTriggerKeyOptionsClick(model.id)
                    }

                    onDeviceClick { _ ->
                        configKeyMapTriggerViewModel.onChooseDeviceClick(model.id)
                    }
                }
            }
        }
    }
}