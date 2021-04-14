package io.github.sds100.keymapper.ui

import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.addRepeatingJob
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.ui.dialogs.GetUserResponse
import io.github.sds100.keymapper.ui.dialogs.RequestUserResponseEvent
import io.github.sds100.keymapper.ui.dialogs.UserResponse
import io.github.sds100.keymapper.ui.dialogs.UserResponseEvent
import io.github.sds100.keymapper.ui.utils.SnackBarUtils
import io.github.sds100.keymapper.util.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking

/**
 * Created by sds100 on 23/03/2021.
 */

class UserResponseViewModelImpl : UserResponseViewModel {

    private val _onUserResponse = MutableSharedFlow<UserResponseEvent>()
    override val onUserResponse = _onUserResponse.asSharedFlow()

    private val _getUserResponse = MutableSharedFlow<RequestUserResponseEvent>()
    override val requestUserResponse = _getUserResponse.asSharedFlow()

    override suspend fun getUserResponse(event: RequestUserResponseEvent) {
        _getUserResponse.emit(event)
    }

    override fun onUserResponse(event: UserResponseEvent) {
        runBlocking { _onUserResponse.emit(event) }
    }
}

interface UserResponseViewModel {
    val requestUserResponse: SharedFlow<RequestUserResponseEvent>
    val onUserResponse: SharedFlow<UserResponseEvent>

    suspend fun getUserResponse(event: RequestUserResponseEvent)
    fun onUserResponse(event: UserResponseEvent)
}

fun UserResponseViewModel.onUserResponse(key: String, response: UserResponse?) {
    onUserResponse(UserResponseEvent(key, response))
}

suspend inline fun <reified R : UserResponse> UserResponseViewModel.getUserResponse(
    key: String,
    ui: GetUserResponse<R>
): R? {
    getUserResponse(RequestUserResponseEvent(key, ui))

    /*
    This ensures only one job for a dialog is active at once by cancelling previous jobs when a new
    dialog is shown with the same key
     */
    return merge(
        requestUserResponse.dropWhile { it.key != key }.map { null },
        onUserResponse.dropWhile { it.response !is R? && it.key != key }.map { it.response }
    ).first() as R?
}

fun UserResponseViewModel.showUserResponseRequests(
    fragment: Fragment,
    binding: ViewDataBinding
) {
    val lifecycleOwner = fragment.viewLifecycleOwner
    val ctx = fragment.requireContext()

    lifecycleOwner.addRepeatingJob(Lifecycle.State.RESUMED) {
        requestUserResponse.collectLatest { event ->
            var responded = false

            lifecycleOwner.lifecycle.addObserver(object : LifecycleObserver {
                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                fun onDestroy() {
                    if (!responded) {
                        onUserResponse(event.key, null)
                        responded = true
                    }
                }
            })

            val response = when (event.ui) {
                is GetUserResponse.Ok ->
                    ctx.okDialog(lifecycleOwner, event.ui.message, event.ui.title)

                is GetUserResponse.MultiChoice<*> ->
                    ctx.multiChoiceDialog(lifecycleOwner, event.ui.items)

                is GetUserResponse.SingleChoice<*> ->
                    ctx.singleChoiceDialog(lifecycleOwner, event.ui.items)

                is GetUserResponse.SnackBar ->
                    SnackBarUtils.show(
                        binding.root.findViewById(R.id.coordinatorLayout),
                        event.ui.title,
                        event.ui.actionText,
                        event.ui.long
                    )

                is GetUserResponse.Text -> ctx.editTextStringAlertDialog(
                    lifecycleOwner,
                    event.ui.hint,
                    event.ui.allowEmpty
                )

                is GetUserResponse.Dialog -> ctx.materialAlertDialog(lifecycleOwner, event.ui)
            }

            if (!responded) {
                onUserResponse(event.key, response)
                responded = true
            }
        }
    }
}