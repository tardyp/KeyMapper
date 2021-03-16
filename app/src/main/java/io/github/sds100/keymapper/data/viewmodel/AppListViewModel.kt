package io.github.sds100.keymapper.data.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.sds100.keymapper.data.model.AppListItemModel
import io.github.sds100.keymapper.domain.packages.PackageInfo
import io.github.sds100.keymapper.domain.packages.PackageManagerAdapter
import io.github.sds100.keymapper.framework.adapters.AppInfoAdapter
import io.github.sds100.keymapper.util.*
import io.github.sds100.keymapper.util.delegate.ModelState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

/**
 * Created by sds100 on 27/01/2020.
 */
class AppListViewModel internal constructor(
    private val appInfoAdapter: AppInfoAdapter,
    packageManager: PackageManagerAdapter
) : ViewModel(), ModelState<List<AppListItemModel>> {

    private val launchableAppModelList =
        packageManager.installedPackages
            .map { state ->
                state.mapDataSuspend { list ->
                    list.filter { it.canBeLaunched }.buildModels()
                }
            }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.Eagerly, Loading())

    private val allAppModelList = packageManager.installedPackages
        .map { state -> state.mapDataSuspend { it.buildModels() } }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Eagerly, Loading())

    val searchQuery = MutableStateFlow<String?>(null)

    val showHiddenApps = MutableStateFlow(false)

    val showHiddenAppsButton = launchableAppModelList.map {
        if (it is Data) {
            it.data.isNotEmpty()
        } else {
            false
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    override val model = FilteredListLiveData<AppListItemModel>().apply {

        viewModelScope.launch {
            combine(
                allAppModelList,
                launchableAppModelList,
                showHiddenApps,
                searchQuery
            ) { allModels, launchableModels, showHiddenApps, query ->

                val modelsToFilter = if (showHiddenApps) {
                    allModels
                } else {
                    launchableModels
                }

                Pair(modelsToFilter, query)

            }.collectLatest {
                filterSuspend(it.first, it.second)
            }
        }
    }

    override val viewState = MutableLiveData<ViewState>(ViewLoading())

    override fun rebuildModels() {}//leave blank

    private suspend fun List<PackageInfo>.buildModels(): List<AppListItemModel> = flow {
        forEach {
            combine(
                appInfoAdapter.getAppName(it.packageName),
                appInfoAdapter.getAppIcon(it.packageName)
            ) { name, icon ->
                val model = AppListItemModel(
                    packageName = it.packageName,
                    appName = name,
                    icon = icon
                )

                emit(model)
            }.catch { }.collect()
        }
    }.toList().sortedBy { it.appName.toLowerCase(Locale.getDefault()) }

    class Factory(
        private val appInfoAdapter: AppInfoAdapter,
        private val packageManager: PackageManagerAdapter
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>) =
            AppListViewModel(appInfoAdapter, packageManager) as T
    }
}
