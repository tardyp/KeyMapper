package io.github.sds100.keymapper.data.viewmodel

import androidx.lifecycle.*
import io.github.sds100.keymapper.data.model.AppShortcutListItemModel
import io.github.sds100.keymapper.data.repository.AppRepository
import io.github.sds100.keymapper.util.*
import io.github.sds100.keymapper.util.delegate.ModelState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Created by sds100 on 27/01/2020.
 */
class AppShortcutListViewModel internal constructor(
    private val repository: AppRepository
) : ViewModel(), ModelState<List<AppShortcutListItemModel>> {

    val searchQuery: MutableLiveData<String> = MutableLiveData("")

    private val appShortcutModelList = liveData {
        emit(Loading())

        val appShortcutList = withContext(viewModelScope.coroutineContext + Dispatchers.Default) {
            repository.getAppShortcutList().map {
                //only include it if it has a configuration screen

                val name = repository.getIntentLabel(it)
                val icon = repository.getIntentIcon(it)

                AppShortcutListItemModel(it.activityInfo, name, icon)
            }.sortedBy { it.label.toLowerCase(Locale.getDefault()) }.getDataState()
        }

        emit(appShortcutList)
    }

    private val _model = FilteredListLiveData<AppShortcutListItemModel>().apply {
        addSource(searchQuery) { query ->
            filter(appShortcutModelList.value ?: Empty(), query)
        }

        addSource(appShortcutModelList) {
            value = it

            filter(appShortcutModelList.value ?: Empty(), searchQuery.value)
        }
    }

    override val model = _model
    override val viewState = MutableLiveData<ViewState>(ViewLoading())

    class Factory(
        private val repository: AppRepository
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>) =
            AppShortcutListViewModel(repository) as T
    }
}
