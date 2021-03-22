package io.github.sds100.keymapper.ui.fragment.keymap

import android.content.ClipData
import androidx.navigation.navGraphViewModels
import com.airbnb.epoxy.EpoxyRecyclerView
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.databinding.FragmentSimpleRecyclerviewBinding
import io.github.sds100.keymapper.triggerFromOtherApps
import io.github.sds100.keymapper.ui.*
import io.github.sds100.keymapper.ui.fragment.SimpleRecyclerViewFragment
import io.github.sds100.keymapper.ui.mappings.keymap.ConfigKeymapViewModel
import io.github.sds100.keymapper.ui.mappings.keymap.TriggerOptionsViewModel
import io.github.sds100.keymapper.ui.utils.configuredCheckBox
import io.github.sds100.keymapper.ui.utils.configuredSlider
import io.github.sds100.keymapper.util.FragmentInfo
import io.github.sds100.keymapper.util.InjectorUtils
import io.github.sds100.keymapper.util.UrlUtils
import io.github.sds100.keymapper.util.str
import splitties.systemservices.clipboardManager
import splitties.toast.toast

/**
 * Created by sds100 on 29/11/20.
 */
class TriggerOptionsFragment : SimpleRecyclerViewFragment<ListItem>() {

    class Info : FragmentInfo(
        R.string.option_list_header,
        R.string.url_trigger_options_guide,
        { TriggerOptionsFragment() }
    )

    private val configKeymapViewModel: ConfigKeymapViewModel by navGraphViewModels(R.id.nav_config_keymap) {
        InjectorUtils.provideConfigKeymapViewModel(requireContext())
    }

    private val viewModel: TriggerOptionsViewModel
        get() = configKeymapViewModel.triggerViewModel.optionsViewModel

    override var isAppBarVisible = false

    override val stateProducer: UiStateProducer<ListState<ListItem>>
        get() = viewModel

    override fun subscribeUi(binding: FragmentSimpleRecyclerviewBinding) {

    }

    override fun populateRecyclerView(recyclerView: EpoxyRecyclerView, list: List<ListItem>) {
        recyclerView.withModels {
            list.forEach { listItem ->
                if (listItem is CheckBoxListItem) {
                    configuredCheckBox(this@TriggerOptionsFragment, listItem) { isChecked ->
                        viewModel.setCheckboxValue(listItem.id, isChecked)
                    }
                }

                if (listItem is SliderListItem) {
                    configuredSlider(this@TriggerOptionsFragment, listItem) { newValue ->
                        viewModel.setSliderValue(listItem.id, newValue)
                    }
                }

                if (listItem is TriggerFromOtherAppsListItem) {
                    triggerFromOtherApps {
                        id(listItem.id)

                        model(listItem)

                        onCheckedChange { buttonView, isChecked ->
                            viewModel.setCheckboxValue(listItem.id, isChecked)
                        }

                        onCopyClick { _ ->
                            val clipData = ClipData.newPlainText(
                                str(R.string.clipboard_label_keymap_uid),
                                listItem.keymapUid
                            )

                            clipboardManager.setPrimaryClip(clipData)

                            toast(R.string.toast_copied_keymap_uid_to_clipboard)
                        }

                        onCreateLauncherShortcutClick { _ ->
                            configKeymapViewModel.createLauncherShortcut()
                        }

                        openIntentGuide { _ ->
                            UrlUtils.openUrl(
                                requireContext(),
                                str(R.string.url_trigger_by_intent_guide)
                            )
                        }
                    }
                }
            }
        }
    }
}

