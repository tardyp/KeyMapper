package io.github.sds100.keymapper.ui.fragment

import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.airbnb.epoxy.EpoxyRecyclerView
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.data.model.OptionType
import io.github.sds100.keymapper.data.model.SystemActionDef
import io.github.sds100.keymapper.data.model.SystemActionListItem
import io.github.sds100.keymapper.data.model.SystemActionOption
import io.github.sds100.keymapper.data.viewmodel.SystemActionListViewModel
import io.github.sds100.keymapper.databinding.FragmentSimpleRecyclerviewBinding
import io.github.sds100.keymapper.sectionHeader
import io.github.sds100.keymapper.simple
import io.github.sds100.keymapper.ui.*
import io.github.sds100.keymapper.util.*
import io.github.sds100.keymapper.util.result.getFullMessage
import io.github.sds100.keymapper.util.result.handle
import io.github.sds100.keymapper.util.result.onSuccess
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import splitties.alertdialog.appcompat.*
import splitties.alertdialog.appcompat.coroutines.showAndAwaitOkOrDismiss
import splitties.experimental.ExperimentalSplittiesApi
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Created by sds100 on 31/03/2020.
 */
class SystemActionListFragment : SimpleRecyclerViewFragment<ListItem>() {

    companion object {
        const val REQUEST_KEY = "request_system_action"
        const val EXTRA_SYSTEM_ACTION = "extra_system_action"
        const val SEARCH_STATE_KEY = "key_system_action_search_state"
    }

    private val viewModel: SystemActionListViewModel by activityViewModels {
        InjectorUtils.provideSystemActionListViewModel(requireContext())
    }

    override var searchStateKey: String? = SEARCH_STATE_KEY
    override var requestKey: String? = REQUEST_KEY

    override val listItems: Flow<ListUiState<ListItem>>
        get() = viewModel.state.map { it.listItems }

    override fun subscribeUi(binding: FragmentSimpleRecyclerviewBinding) {
        super.subscribeUi(binding)

        viewLifecycleScope.launchWhenResumed {
            viewModel.state.collectLatest { state ->
                binding.caption = if (state.showUnsupportedActionsMessage) {
                    str(R.string.your_device_doesnt_support_some_actions)
                } else {
                    null
                }
            }
        }

        viewLifecycleScope.launchWhenResumed {
            viewModel.showDialog.collectLatest {
                viewModel.onDialogResponse(
                    it.key,
                    it.ui.show(this@SystemActionListFragment)
                )
            }
        }

        viewLifecycleScope.launchWhenResumed {
            viewModel.returnResult.collectLatest {
                returnResult(EXTRA_SYSTEM_ACTION to Json.encodeToString(it))
            }
        }
    }

    override fun populateList(recyclerView: EpoxyRecyclerView, listItems: List<ListItem>) {
        recyclerView.withModels {
            listItems.forEach { listItem ->
                if (listItem is SectionHeaderListItem) {
                    sectionHeader {
                        id(listItem.id)
                        header(listItem.text)
                    }
                }

                if (listItem is SystemActionListItem) {
                    simple {
                        id(listItem.id)
                        primaryText(listItem.title)
                        icon(listItem.icon)
                        tintType(TintType.ON_SURFACE)

                        isSecondaryTextAnError(listItem.showRequiresRootMessage)

                        if (listItem.showRequiresRootMessage) {
                            secondaryText(str(R.string.requires_root))
                        } else {
                            secondaryText(null)
                        }

                        onClick { _ ->
                            viewModel.onSystemActionClick(listItem.systemActionId)
                        }
                    }
                }
            }
        }
    }

    override fun onSearchQuery(query: String?) {
        viewModel.searchQuery.value = query
    }

    override fun rebuildUiState() {
        viewModel.rebuildUiState()
    }

    @ExperimentalSplittiesApi
    private suspend fun onSystemActionClick(systemActionDef: SystemActionDef) =
        withContext(lifecycleScope.coroutineContext) {

            var selectedOptionData: String? = null

            if (systemActionDef.messageOnSelection != null) {
                requireContext().alertDialog {
                    titleResource = systemActionDef.descriptionRes
                    messageResource = systemActionDef.messageOnSelection
                }.showAndAwaitOkOrDismiss()
            }

            systemActionDef.getOptions(requireContext()).onSuccess { options ->
                val optionLabels = options.map { optionId ->
                    SystemActionOption.getOptionLabel(
                        requireContext(),
                        systemActionDef.id,
                        optionId
                    ).handle(
                        onSuccess = { it },
                        onError = { it.getFullMessage(requireContext()) }
                    )
                }

                selectedOptionData = suspendCoroutine<String> {
                    requireContext().alertDialog {

                        when (systemActionDef.optionType) {
                            OptionType.SINGLE -> {
                                setItems(optionLabels.toTypedArray()) { _, which ->
                                    val option = options[which]

                                    it.resume(option)
                                }
                            }

                            OptionType.MULTIPLE -> {
                                val checkedOptions = BooleanArray(optionLabels.size) { false }

                                setMultiChoiceItems(
                                    optionLabels.toTypedArray(),
                                    checkedOptions
                                ) { _, which, checked ->
                                    checkedOptions[which] = checked
                                }

                                okButton { _ ->
                                    val data = SystemActionOption.optionSetToString(
                                        options.filterIndexed { index, _ -> checkedOptions[index] }
                                            .toSet()
                                    )

                                    it.resume(data)
                                }
                            }
                        }

                        cancelButton {
                            cancel()
                        }

                        show()
                    }
                }
            }

            //TODO rework all of this in the view model
//            returnResult(
//
//            )
        }
}