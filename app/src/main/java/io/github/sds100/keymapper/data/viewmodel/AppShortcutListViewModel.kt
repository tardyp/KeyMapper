package io.github.sds100.keymapper.data.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.data.model.AppShortcutListItem
import io.github.sds100.keymapper.domain.shortcuts.GetAppShortcutsUseCase
import io.github.sds100.keymapper.domain.utils.State
import io.github.sds100.keymapper.framework.adapters.AppShortcutUiAdapter
import io.github.sds100.keymapper.framework.adapters.AppUiAdapter
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.ui.DialogViewModel
import io.github.sds100.keymapper.ui.DialogViewModelImpl
import io.github.sds100.keymapper.ui.ListUiState
import io.github.sds100.keymapper.ui.dialogs.DialogUi
import io.github.sds100.keymapper.ui.shortcuts.ChooseAppShortcutResult
import io.github.sds100.keymapper.ui.showDialog
import io.github.sds100.keymapper.util.filterByQuery
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*

/**
 * Created by sds100 on 27/01/2020.
 */
class AppShortcutListViewModel internal constructor(
    private val getAppShortcuts: GetAppShortcutsUseCase,
    private val appShortcutUiAdapter: AppShortcutUiAdapter,
    private val appUiAdapter: AppUiAdapter,
    resourceProvider: ResourceProvider
) : ViewModel(), DialogViewModel by DialogViewModelImpl(), ResourceProvider by resourceProvider {

    val searchQuery = MutableStateFlow<String?>(null)

    private val _state = MutableStateFlow<ListUiState<AppShortcutListItem>>(ListUiState.Loading)
    val state = _state.asStateFlow()

    private val _returnResult = MutableSharedFlow<ChooseAppShortcutResult>()
    val returnResult = _returnResult.asSharedFlow()

    private val rebuildUiState = MutableSharedFlow<Unit>()

    private var createAppShortcutJob: Job? = null

    init {
        viewModelScope.launch {
            combine(
                searchQuery,
                getAppShortcuts.shortcuts,
                rebuildUiState
            ) { query, shortcuts, _ ->
                Pair(query, shortcuts)
            }.collectLatest { pair ->
                val (query, shortcuts) = pair

                when (shortcuts) {
                    is State.Data -> {
                        shortcuts.data
                            .map {
                                AppShortcutListItem(
                                    shortcutInfo = it,
                                    appShortcutUiAdapter.getName(it).single(),
                                    appShortcutUiAdapter.getIcon(it).single()
                                )
                            }
                            .sortedBy { it.label.toLowerCase(Locale.getDefault()) }
                            .filterByQuery(query)
                            .collect {
                                _state.value = it
                            }
                    }
                    State.Loading -> _state.value = ListUiState.Loading
                }
            }
        }
    }

    fun onConfigureShortcutResult(intent: Intent) {
        createAppShortcutJob?.cancel()

        createAppShortcutJob = viewModelScope.launch {
            val uri: String

            //the shortcut intents seem to be returned in 2 different formats.
            @Suppress("DEPRECATION")
            if (intent.extras != null &&
                intent.extras!!.containsKey(Intent.EXTRA_SHORTCUT_INTENT)
            ) {
                //get intent from selected shortcut
                val shortcutIntent =
                    intent.extras!!.get(Intent.EXTRA_SHORTCUT_INTENT) as Intent
                uri = shortcutIntent.toUri(0)

            } else {
                uri = intent.toUri(0)
            }

            val packageName = Intent.parseUri(uri, 0).`package`
                ?: intent.component?.packageName
                ?: Intent.parseUri(uri, 0).component?.packageName

            val appName = packageName?.let { appUiAdapter.getAppName(it).single() }

            val shortcutName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME).let {
                if (it != null) return@let it

                val name = showDialog(
                    "create_shortcut_name", DialogUi.Text(
                        hint = getString(R.string.hint_shortcut_name),
                        allowEmpty = false
                    )
                ).text

                if (appName == null) {
                    name
                } else {
                    "$appName: $name"
                }
            }

            _returnResult.emit(
                ChooseAppShortcutResult(
                    packageName = packageName,
                    shortcutName = shortcutName,
                    uri = uri
                )
            )
        }
    }

    fun rebuildUiState() {
        runBlocking { rebuildUiState.emit(Unit) }
    }

    override fun onCleared() {
        createAppShortcutJob?.cancel()
        createAppShortcutJob = null
        super.onCleared()
    }

    class Factory(
        private val getAppShortcutsUseCase: GetAppShortcutsUseCase,
        private val appShortcutUiAdapter: AppShortcutUiAdapter,
        private val appUiAdapter: AppUiAdapter,
        private val resourceProvider: ResourceProvider
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>) =
            AppShortcutListViewModel(
                getAppShortcutsUseCase,
                appShortcutUiAdapter,
                appUiAdapter,
                resourceProvider
            ) as T
    }
}
