package io.github.sds100.keymapper.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.sds100.keymapper.data.model.AppListItem
import io.github.sds100.keymapper.domain.packages.GetPackagesUseCase
import io.github.sds100.keymapper.domain.packages.PackageInfo
import io.github.sds100.keymapper.domain.utils.State
import io.github.sds100.keymapper.domain.utils.mapData
import io.github.sds100.keymapper.framework.adapters.AppUiAdapter
import io.github.sds100.keymapper.ui.ListUiState
import io.github.sds100.keymapper.util.filterByQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Created by sds100 on 27/01/2020.
 */
class AppListViewModel internal constructor(
    private val appUiAdapter: AppUiAdapter,
    getPackages: GetPackagesUseCase
) : ViewModel() {

    val searchQuery = MutableStateFlow<String?>(null)

    private val showHiddenApps = MutableStateFlow(false)

    private val _state = MutableStateFlow(
        AppListState(
            ListUiState.Loading,
            showHiddenAppsButton = false,
            isHiddenAppsChecked = false
        )
    )
    val state = _state.asStateFlow()

    private val rebuildUiState = MutableSharedFlow<Unit>()

    init {
        viewModelScope.launch {
            combine(
                getPackages.installedPackages,
                showHiddenApps,
                searchQuery,
                rebuildUiState
            ) { packageInfoList, showHiddenApps, query, _ ->

                Triple(packageInfoList, query, showHiddenApps)

            }.collectLatest { pair ->
                val packageInfoList = pair.first
                val query = pair.second
                val showHiddenApps = pair.third

                val packagesToFilter = if (showHiddenApps) {
                    packageInfoList
                } else {
                    withContext(Dispatchers.Default) {
                        packageInfoList.mapData { list -> list.filter { it.canBeLaunched } }
                    }
                }

                when (val modelList = packagesToFilter.mapData { it.buildListItems() }) {
                    is State.Data ->
                        modelList.data.filterByQuery(query)
                            .flowOn(Dispatchers.Default)
                            .collect { modelListState ->
                                _state.value = AppListState(
                                    modelListState,
                                    showHiddenAppsButton = true,
                                    isHiddenAppsChecked = showHiddenApps
                                )
                            }

                    is State.Loading -> _state.value =
                        AppListState(
                            ListUiState.Loading,
                            showHiddenAppsButton = true,
                            isHiddenAppsChecked = showHiddenApps
                        )
                }
            }
        }
    }

    fun onHiddenAppsCheckedChange(checked: Boolean) {
        showHiddenApps.value = checked
    }

    fun rebuildUiState() {
        runBlocking { rebuildUiState.emit(Unit) }
    }

    private suspend fun List<PackageInfo>.buildListItems(): List<AppListItem> =flow {
        forEach {
            combine(
                appUiAdapter.getAppName(it.packageName),
                appUiAdapter.getAppIcon(it.packageName)
            ) { name, icon ->
                val model = AppListItem(
                    packageName = it.packageName,
                    appName = name,
                    icon = icon
                )

                emit(model)
            }.catch { }.collect()
        }
    }.flowOn(Dispatchers.Default)
        .toList()
        .sortedBy { it.appName.toLowerCase(Locale.getDefault()) }

    class Factory(
        private val appUiAdapter: AppUiAdapter,
        private val getPackages: GetPackagesUseCase
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>) =
            AppListViewModel(appUiAdapter, getPackages) as T
    }
}

data class AppListState(
    val listItems: ListUiState<AppListItem>,
    val showHiddenAppsButton: Boolean,
    val isHiddenAppsChecked: Boolean
)
