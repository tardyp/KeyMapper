package io.github.sds100.keymapper.util

import androidx.lifecycle.MediatorLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Created by sds100 on 13/01/21.
 */

class FilteredListLiveData<T : ISearchable> : MediatorLiveData<OldDataState<List<T>>>() {
    init {
        value = Loading()
    }

    //TODO remove
    fun filter(models: OldDataState<List<T>>, query: String?) {
        value = Loading()

        value = when (models) {
            is Data -> {
                if (query == null) {
                    models
                } else {

                    val filteredList = models.data.filter { model ->
                        model.getSearchableString().toLowerCase(Locale.getDefault()).contains(query)
                    }

                    filteredList.getDataState()
                }
            }

            is Empty -> Empty()
            is Loading -> Loading()
        }
    }

    //TODO remove
    suspend fun filterSuspend(models: OldDataState<List<T>>, query: String?) =
        withContext(Dispatchers.Default) {
            postValue(Loading())

            val filteredModels = when (models) {
                is Data -> {
                    if (query == null) {
                        models
                    } else {

                        val filteredList = models.data.filter { model ->
                            model.getSearchableString().toLowerCase(Locale.getDefault())
                                .contains(query)
                        }

                        filteredList.getDataState()
                    }
                }

                is Empty -> Empty()
                is Loading -> Loading()
            }

            postValue(filteredModels)
        }
}
