package io.github.sds100.keymapper.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.data.viewmodel.CreateKeymapShortcutViewModel
import io.github.sds100.keymapper.databinding.FragmentRecyclerviewBinding
import io.github.sds100.keymapper.ui.mappings.keymap.KeymapListItem
import io.github.sds100.keymapper.util.*
import io.github.sds100.keymapper.util.delegate.ModelState
import io.github.sds100.keymapper.util.delegate.RecoverFailureDelegate
import kotlinx.coroutines.launch
import splitties.alertdialog.appcompat.alertDialog
import splitties.alertdialog.appcompat.cancelButton
import splitties.alertdialog.appcompat.messageResource
import splitties.alertdialog.appcompat.positiveButton

/**
 * Created by sds100 on 08/09/20.
 */

class CreateKeymapShortcutFragment : DefaultRecyclerViewFragment<List<KeymapListItem>>() {

    private val viewModel by activityViewModels<CreateKeymapShortcutViewModel> {
        InjectorUtils.provideCreateActionShortcutViewModel(requireContext())
    }

    override val modelState: ModelState<List<KeymapListItem>>
        get() = viewModel

    private lateinit var recoverFailureDelegate: RecoverFailureDelegate

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        recoverFailureDelegate = RecoverFailureDelegate(
            "CreateKeymapShortcutFragment",
            requireActivity().activityResultRegistry,
            viewLifecycleOwner
        ) {

            viewModel.rebuildModels()
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun subscribeUi(binding: FragmentRecyclerviewBinding) {
        super.subscribeUi(binding)

        binding.caption = str(R.string.caption_create_keymap_shortcut)

        viewModel.eventStream.observe(viewLifecycleOwner, { event ->
            when (event) {
                is FixFailure ->
                    TODO()

                is EnableAccessibilityServicePrompt ->
                    binding.coordinatorLayout.showEnableAccessibilityServiceSnackBar()

                is BuildKeymapListModels -> viewLifecycleScope.launchWhenResumed {
                    viewModel.setModelList(buildModelList(event))
                }

                is CreateKeymapShortcutEvent -> viewLifecycleScope.launch {
//                    val shortcutInfo = KeymapShortcutUtils.createShortcut(
//                        requireContext(),
//                        viewLifecycleOwner,
//                        event.uuid,
//                        event.actionList,
//                        event.deviceInfoList,
//                        event.showDeviceDescriptors
//                    )
//
//                    ShortcutManagerCompat.createShortcutResultIntent(requireContext(), shortcutInfo)
//                        .apply {
//                            requireActivity().setResult(Activity.RESULT_OK, this)
//                            requireActivity().finish()
//                        }
                    //TODO
                }
            }
        })
    }

    override fun populateList(
        binding: FragmentRecyclerviewBinding,
        model: List<KeymapListItem>?
    ) {
        binding.epoxyRecyclerView.withModels {
            model?.forEach {
//                keymap {
//                    id(it.id)
//                    model(it)
//                    isSelectable(false)
//
//                    onErrorClick(object : OnChipClickCallback {
//                        override fun onErrorClick(error: Error) {
//                            viewModel.fixError(error)
//                        }
//                    })
//
//                    onClick { _ ->
//                        viewModel.chooseKeymap(it.uid)
//                    }
//                } TODO
            }
        }
    }

    override fun onResume() {
        super.onResume()

        viewModel.rebuildModels()
    }

    private fun buildModelList(payload: BuildKeymapListModels) =
        payload.keymapList.map { keymap ->
            TODO()
        }

    override fun onBackPressed() {
        showOnBackPressedWarning()
    }

    private fun showOnBackPressedWarning() {
        requireContext().alertDialog {
            messageResource = R.string.dialog_message_are_you_sure_want_to_leave_without_saving

            positiveButton(R.string.pos_yes) {
                requireActivity().finish()
            }

            cancelButton()
            show()
        }
    }
}