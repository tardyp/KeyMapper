package io.github.sds100.keymapper.ui.shortcuts

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.addRepeatingJob
import com.airbnb.epoxy.EpoxyRecyclerView
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.data.model.AppShortcutListItem
import io.github.sds100.keymapper.data.viewmodel.ChooseAppShortcutViewModel
import io.github.sds100.keymapper.databinding.FragmentSimpleRecyclerviewBinding
import io.github.sds100.keymapper.domain.shortcuts.AppShortcutInfo
import io.github.sds100.keymapper.simple
import io.github.sds100.keymapper.ui.ListUiState
import io.github.sds100.keymapper.ui.fragment.SimpleRecyclerViewFragment
import io.github.sds100.keymapper.ui.onDialogResponse
import io.github.sds100.keymapper.util.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import splitties.toast.toast

/**
 * Created by sds100 on 29/03/2020.
 */

class AppShortcutListFragment : SimpleRecyclerViewFragment<AppShortcutListItem>() {

    companion object {
        const val REQUEST_KEY = "request_app_shortcut"
        const val EXTRA_RESULT = "extra_choose_app_shortcut_result"
        const val SEARCH_STATE_KEY = "key_app_shortcut_search_state"
    }

    override var requestKey: String? = REQUEST_KEY
    override var searchStateKey: String? = SEARCH_STATE_KEY

    private val viewModel: ChooseAppShortcutViewModel by activityViewModels {
        InjectorUtils.provideAppShortcutListViewModel(requireContext())
    }

    override val listItems: Flow<ListUiState<AppShortcutListItem>>
        get() = viewModel.state

    private val appShortcutConfigLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result ?: return@registerForActivityResult

            if (result.resultCode == Activity.RESULT_OK) {
                result.data ?: return@registerForActivityResult

                viewModel.onConfigureShortcutResult(result.data!!)
            }
        }

    override fun subscribeUi(binding: FragmentSimpleRecyclerviewBinding) {
        super.subscribeUi(binding)

        viewLifecycleOwner.addRepeatingJob(Lifecycle.State.RESUMED) {
            viewModel.returnResult.collectLatest {
                returnResult(EXTRA_RESULT to Json.encodeToString(it))
            }
        }

        viewLifecycleOwner.addRepeatingJob(Lifecycle.State.RESUMED) {
            viewModel.showDialog.collectLatest {
                viewModel.onDialogResponse(
                    it.key,
                    it.ui.show(this@AppShortcutListFragment)
                )
            }
        }
    }

    override fun populateList(
        recyclerView: EpoxyRecyclerView,
        listItems: List<AppShortcutListItem>
    ) {
        binding.epoxyRecyclerView.withModels {
            listItems.forEach {
                simple {
                    id(it.shortcutInfo.toString())
                    primaryText(it.label)
                    icon(it.icon)

                    onClick { _ ->
                        launchShortcutConfiguration(it.shortcutInfo)
                    }
                }
            }
        }
    }

    override fun rebuildUiState() {
        viewModel.rebuildUiState()
    }

    override fun onSearchQuery(query: String?) {
        viewModel.searchQuery.value = query
    }

    private fun launchShortcutConfiguration(shortcutInfo: AppShortcutInfo) {
        Intent().apply {
            action = Intent.ACTION_CREATE_SHORTCUT
            setClassName(shortcutInfo.packageName, shortcutInfo.activityName)
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 1)

            try {
                appShortcutConfigLauncher.launch(this)
            } catch (e: SecurityException) {
                toast(R.string.error_keymapper_doesnt_have_permission_app_shortcut)
            }
        }
    }
}