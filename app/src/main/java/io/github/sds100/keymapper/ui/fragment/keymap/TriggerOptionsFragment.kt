package io.github.sds100.keymapper.ui.fragment.keymap

import androidx.navigation.navGraphViewModels
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.databinding.FragmentRecyclerviewBinding
import io.github.sds100.keymapper.ui.fragment.DefaultRecyclerViewFragment
import io.github.sds100.keymapper.ui.mappings.keymap.ConfigKeymapViewModel
import io.github.sds100.keymapper.ui.mappings.keymap.TriggerOptionsViewModel
import io.github.sds100.keymapper.ui.models.ListItem
import io.github.sds100.keymapper.util.FragmentInfo
import io.github.sds100.keymapper.util.InjectorUtils
import io.github.sds100.keymapper.util.delegate.ModelState

/**
 * Created by sds100 on 29/11/20.
 */
class TriggerOptionsFragment : DefaultRecyclerViewFragment<List<ListItem>>() {

    class Info : FragmentInfo(
        R.string.option_list_header,
        R.string.url_trigger_options_guide,
        { TriggerOptionsFragment() }
    )

    val configKeymapViewModel: ConfigKeymapViewModel by navGraphViewModels(R.id.nav_config_keymap) {
        InjectorUtils.provideConfigKeymapViewModel(requireContext())
    }

    val viewModel: TriggerOptionsViewModel
        get() = configKeymapViewModel.triggerViewModel.optionsViewModel

    override var isAppBarVisible = false

    override val modelState: ModelState<List<ListItem>>
        get() = viewModel

    override fun populateList(binding: FragmentRecyclerviewBinding, model: List<ListItem>?) {

    }

//    val controller by lazy { TriggerOptionsController() } TODO

    //TODO
//    inner class TriggerOptionsController : OptionsController(viewLifecycleOwner) {
//        var triggerByIntentModel: TriggerFromOtherAppsModel? = null
//            set(value) {
//                field = value
//                requestModelBuild()
//            }
//
//        override val ctx: Context
//            get() = requireContext()
//
//        override val viewModel: BaseOptionsViewModel<*>
//            get() = this@TriggerOptionsFragment.viewModel
//
//        override fun buildModels() {
//            if (triggerByIntentModel != null) {
//                triggerFromOtherApps {
//                    id("trigger_by_intent")
//
//                    model(triggerByIntentModel)
//
//                    onClick { view ->
//                        viewModel.setValue(
//                            TriggerOptions.ID_TRIGGER_FROM_OTHER_APPS, (view as CheckBox).isChecked
//                        )
//                    }
//
//                    onCopyClick { _ ->
//                        triggerByIntentModel ?: return@onCopyClick
//
//                        val clipData = ClipData.newPlainText(
//                            str(R.string.clipboard_label_keymap_uid),
//                            triggerByIntentModel?.uid
//                        )
//
//                        clipboardManager.setPrimaryClip(clipData)
//
//                        toast(R.string.toast_copied_keymap_uid_to_clipboard)
//                    }
//
//                    isCreatingLauncherShortcutsSupported(
//                        ShortcutManagerCompat.isRequestPinShortcutSupported(requireContext())
//                    )
//
//                    onCreateLauncherShortcutClick { _ ->
//                        triggerByIntentModel?.uid?.let {
//                            viewLifecycleScope.launchWhenResumed {
//                                createLauncherShortcut(it)
//                            }
//                        }
//                    }
//
//                    openIntentGuide { _ ->
//                        UrlUtils.openUrl(
//                            requireContext(),
//                            str(R.string.url_trigger_by_intent_guide)
//                        )
//                    }
//                }
//            }
//
//            super.buildModels()
//        }
//    }

    //TODO
//    private suspend fun createLauncherShortcut(uuid: String) {
//        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(requireContext())) return
//
//        val actionList = configKeymapViewModel.actionListViewModel.actionList.value ?: return
//
//        val shortcutInfo = KeymapShortcutUtils.createShortcut(
//            requireContext(),
//            viewLifecycleOwner,
//            uuid,
//            actionList,
//            viewModel.getDeviceInfoList(),
//            viewModel.showDeviceDescriptors
//        )
//
//        ShortcutManagerCompat.requestPinShortcut(requireContext(), shortcutInfo, null)
//    }
}

