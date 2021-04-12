package io.github.sds100.keymapper.mappings.common

import io.github.sds100.keymapper.domain.actions.Action
import io.github.sds100.keymapper.domain.utils.State
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.ui.ListItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

/**
 * Created by sds100 on 12/04/2021.
 */

abstract class ConfigActionOptionsViewModel<M : Mapping<A>, A : Action>(
     coroutineScope: CoroutineScope,
    config: ConfigMappingUseCase<A, M>
) : OptionsViewModel {
    val actionUid = MutableStateFlow<String?>(null)

    override val state = combine(config.mapping, actionUid) { mapping, actionUid ->

        when {
            mapping is State.Data && actionUid != null -> {
                val action = mapping.data.actionList.single { it.uid == actionUid }

                OptionsUiState(
                    listItems = createListItems(mapping.data, action),

                    showProgressBar = false
                )
            }

            else -> OptionsUiState(showProgressBar = true)
        }
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            coroutineScope,
            SharingStarted.Eagerly,
            OptionsUiState(showProgressBar = true)
        )

    fun setActionToConfigure(uid: String) {
        actionUid.value = uid
    }

    abstract fun createListItems(mapping: M, action: A): List<ListItem>
}