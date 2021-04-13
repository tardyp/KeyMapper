package io.github.sds100.keymapper.data.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.data.model.AppShortcutListItem
import io.github.sds100.keymapper.domain.utils.State
import io.github.sds100.keymapper.domain.utils.mapData
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.packages.ChooseAppShortcutResult
import io.github.sds100.keymapper.packages.DisplayAppShortcutsUseCase
import io.github.sds100.keymapper.ui.ListUiState
import io.github.sds100.keymapper.ui.UserResponseViewModel
import io.github.sds100.keymapper.ui.UserResponseViewModelImpl
import io.github.sds100.keymapper.ui.dialogs.GetUserResponse
import io.github.sds100.keymapper.ui.getUserResponse
import io.github.sds100.keymapper.util.filterByQuery
import io.github.sds100.keymapper.util.result.valueOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

/**
 * Created by sds100 on 27/01/2020.
 */
class ChooseAppShortcutViewModel internal constructor(
    private val useCase: DisplayAppShortcutsUseCase,
    resourceProvider: ResourceProvider
) : ViewModel(), UserResponseViewModel by UserResponseViewModelImpl(),
    ResourceProvider by resourceProvider {

    val searchQuery = MutableStateFlow<String?>(null)

    private val _state = MutableStateFlow<ListUiState<AppShortcutListItem>>(ListUiState.Loading)
    val state = _state.asStateFlow()

    private val _returnResult = MutableSharedFlow<ChooseAppShortcutResult>()
    val returnResult = _returnResult.asSharedFlow()

    private val listItems = useCase.shortcuts.map { state ->
        state.mapData { appShortcuts ->
            appShortcuts
                .mapNotNull {
                    val name = useCase.getShortcutName(it).valueOrNull()
                        ?: return@mapNotNull null

                    val icon = useCase.getShortcutIcon(it).valueOrNull()
                        ?: return@mapNotNull null

                    AppShortcutListItem(shortcutInfo = it, name, icon)
                }
                .sortedBy { it.label.toLowerCase(Locale.getDefault()) }
        }
    }.flowOn(Dispatchers.Default)

    init {
        combine(
            searchQuery,
            listItems
        ) { query, listItems ->
            when (listItems) {
                is State.Data -> {
                    listItems.data.filterByQuery(query).collect { _state.value = it }
                }
                State.Loading -> _state.value = ListUiState.Loading
            }
        }.launchIn(viewModelScope)
    }

    fun onConfigureShortcutResult(intent: Intent) {
        viewModelScope.launch {
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

            val intentShortcutName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME)

            val shortcutName: String = if (intentShortcutName != null) {
                intentShortcutName
            } else {
                val response = getUserResponse(
                    "create_shortcut_name",
                    GetUserResponse.Text(
                        hint = getString(R.string.hint_shortcut_name),
                        allowEmpty = false
                    )
                ) ?: return@launch

                response.text
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

    class Factory(
        private val useCase: DisplayAppShortcutsUseCase,
        private val resourceProvider: ResourceProvider
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>) =
            ChooseAppShortcutViewModel(
                useCase,
                resourceProvider
            ) as T
    }
}
