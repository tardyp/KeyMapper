package io.github.sds100.keymapper.data.viewmodel

import android.view.KeyEvent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.sds100.keymapper.ui.ListUiState
import io.github.sds100.keymapper.ui.keyevent.KeyCodeListItem
import io.github.sds100.keymapper.util.KeyEventUtils
import io.github.sds100.keymapper.util.filterByQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * Created by sds100 on 31/03/2020.
 */

class KeyCodeListViewModel : ViewModel() {

    val searchQuery = MutableStateFlow<String?>(null)

    private val _state = MutableStateFlow<ListUiState<KeyCodeListItem>>(ListUiState.Loading)
    val state = _state.asStateFlow()

    private val allListItems = flow {
        withContext(Dispatchers.Default) {
            KeyEventUtils.getKeyCodes().sorted().map { keyCode ->
                KeyCodeListItem(keyCode, "$keyCode \t\t ${KeyEvent.keyCodeToString(keyCode)}")
            }
        }.let { emit(it) }
    }

    private val rebuildUiState = MutableSharedFlow<Unit>()

    init {
        viewModelScope.launch {
            combine(
                searchQuery,
                allListItems,
                rebuildUiState
            ) { query, allListItems, _ ->
                Pair(allListItems, query)
            }.collectLatest { pair ->
                val (allListItems, query) = pair

                allListItems.filterByQuery(query).collect {
                    _state.value = it
                }
            }
        }
    }

    fun rebuildUiState() {
        runBlocking { rebuildUiState.emit(Unit) }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return KeyCodeListViewModel() as T
        }
    }
}