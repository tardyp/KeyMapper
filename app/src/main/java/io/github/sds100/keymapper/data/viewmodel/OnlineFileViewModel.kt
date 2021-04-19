@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.github.sds100.keymapper.data.viewmodel

import androidx.lifecycle.*
import io.github.sds100.keymapper.framework.adapters.FileRepository
import io.github.sds100.keymapper.util.*
import io.github.sds100.keymapper.util.result.Error
import io.github.sds100.keymapper.util.result.handle

/**
 * Created by sds100 on 04/04/2020.
 */

//TODO use FileAdapter
class OnlineFileViewModel(
    private val repository: FileRepository,
    private val fileUrl: String,
    private val alternateUrl: String? = null,
    val header: String) : ViewModel() {

    private val markdownResult = liveData {
        emit(Loading())

        emit(Data(repository.getFile(fileUrl)))
    }

    val markdownText = markdownResult.map { result ->
        result.mapData { data ->
            data.handle(
                onSuccess = {
                    it
                },
                onError = {
                    if (it is Error.SSLHandshakeError) {
                        if (alternateUrl != null) {
                            _eventStream.value = OpenUrl(alternateUrl)
                        }
                    }

                    _eventStream.value = ShowErrorMessage(it)
                    _eventStream.value = CloseDialog()

                    ""
                }
            )
        }
    }

    private val _eventStream = MutableLiveData<Event>()
    val eventStream: LiveData<Event> = _eventStream

    class Factory(
        private val repository: FileRepository,
        private val fileUrl: String,
        private val alternateUrl: String? = null,
        private val header: String
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>) =
            OnlineFileViewModel(repository, fileUrl, alternateUrl, header) as T
    }
}