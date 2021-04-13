package io.github.sds100.keymapper.util

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.*
import io.github.sds100.keymapper.databinding.DialogEdittextNumberBinding
import io.github.sds100.keymapper.databinding.DialogEdittextStringBinding
import io.github.sds100.keymapper.ui.dialogs.DialogResponse
import io.github.sds100.keymapper.ui.dialogs.GetUserResponse
import io.github.sds100.keymapper.util.result.*
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.suspendCancellableCoroutine
import splitties.alertdialog.appcompat.*
import splitties.alertdialog.material.materialAlertDialog
import kotlin.coroutines.resume

/**
 * Created by sds100 on 30/03/2020.
 */

suspend fun Context.materialAlertDialog(
    lifecycleOwner: LifecycleOwner,
    model: GetUserResponse.Dialog
) = suspendCancellableCoroutine<DialogResponse?> { continuation ->

    materialAlertDialog {
        title = model.title
        message = model.message

        setPositiveButton(model.positiveButtonText) { _, _ ->
            continuation.resume(DialogResponse.POSITIVE)
        }

        setNeutralButton(model.neutralButtonText) { _, _ ->
            continuation.resume(DialogResponse.NEUTRAL)
        }

        setNegativeButton(model.negativeButtonText) { _, _ ->
            continuation.resume(DialogResponse.NEGATIVE)
        }

        show().apply {
            resumeNullOnDismiss(continuation)
            dismissOnDestroy(lifecycleOwner)
        }
    }
}

suspend fun <ID> Context.multiChoiceDialog(
    lifecycleOwner: LifecycleOwner,
    items: List<Pair<ID, String>>
) = suspendCancellableCoroutine<GetUserResponse.MultiChoiceResponse<ID>?> { continuation ->
    materialAlertDialog {
        val checkedItems = BooleanArray(items.size) { false }

        setMultiChoiceItems(
            items.map { it.second }.toTypedArray(),
            checkedItems
        ) { _, which, checked ->
            checkedItems[which] = checked
        }

        cancelButton()

        okButton {
            val checkedItemIds = sequence {
                checkedItems.forEachIndexed { index, checked ->
                    if (checked) {
                        yield(items[index].first)
                    }
                }
            }.toList()

            continuation.resume(GetUserResponse.MultiChoiceResponse(checkedItemIds))
        }

        show().apply {
            resumeNullOnDismiss(continuation)
            dismissOnDestroy(lifecycleOwner)
        }
    }
}

suspend fun <ID> Context.singleChoiceDialog(
    lifecycleOwner: LifecycleOwner,
    items: List<Pair<ID, String>>
) = suspendCancellableCoroutine<GetUserResponse.SingleChoiceResponse<ID>?> { continuation ->
    materialAlertDialog {
        setItems(
            items.map { it.second }.toTypedArray(),
        ) { _, position ->
            continuation.resume(GetUserResponse.SingleChoiceResponse(items[position].first))
        }

        show().apply {
            resumeNullOnDismiss(continuation)
            dismissOnDestroy(lifecycleOwner)
        }
    }
}

suspend fun Context.editTextStringAlertDialog(
    lifecycleOwner: LifecycleOwner,
    hint: String,
    allowEmpty: Boolean = false
) = suspendCancellableCoroutine<GetUserResponse.TextResponse?> { continuation ->

    val text = MutableStateFlow("")

    val alertDialog = materialAlertDialog {
        val inflater = LayoutInflater.from(this@editTextStringAlertDialog)

        DialogEdittextStringBinding.inflate(inflater).apply {
            setHint(hint)
            setText(text)
            setAllowEmpty(allowEmpty)

            setView(this.root)
        }

        okButton {
            continuation.resume(GetUserResponse.TextResponse(text.value))
        }

        cancelButton()
    }

    alertDialog.show()

    lifecycleOwner.addRepeatingJob(Lifecycle.State.RESUMED) {
        text.collectLatest {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
                if (allowEmpty) {
                    true
                } else {
                    it.isNotBlank()
                }
        }
    }

    //this prevents window leak
    alertDialog.resumeNullOnDismiss(continuation)
    alertDialog.dismissOnDestroy(lifecycleOwner)
}

suspend fun Context.editTextNumberAlertDialog(
    lifecycleOwner: LifecycleOwner,
    hint: String,
    min: Int? = null,
    max: Int? = null
) = suspendCancellableCoroutine<Int?> { continuation ->

    fun isValid(text: String?): Result<Int> {
        if (text.isNullOrBlank()) {
            return Error.InvalidNumber
        }

        return try {
            val num = text.toInt()

            min?.let {
                if (num < min) {
                    return Error.NumberTooSmall(min)
                }
            }

            max?.let {
                if (num > max) {
                    return Error.NumberTooBig(max)
                }
            }

            Success(num)
        } catch (e: NumberFormatException) {
            Error.InvalidNumber
        }
    }

    val text = MutableStateFlow("")

    materialAlertDialog {
        val inflater = LayoutInflater.from(this@editTextNumberAlertDialog)
        DialogEdittextNumberBinding.inflate(inflater).apply {

            setHint(hint)
            setText(text)

            setView(this.root)

            okButton {
                isValid(text.value).onSuccess { num ->
                    continuation.resume(num)
                }
            }

            cancelButton()

            val alertDialog = show()

            alertDialog.resumeNullOnDismiss(continuation)
            alertDialog.dismissOnDestroy(lifecycleOwner)

            lifecycleOwner.addRepeatingJob(Lifecycle.State.RESUMED) {
                text.map { isValid(it) }
                    .collectLatest { isValid ->
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
                            isValid.isSuccess
                        textInputLayout.error = isValid.errorOrNull()?.getFullMessage(context)
                    }
            }
        }
    }
}

suspend fun Context.okDialog(
    lifecycleOwner: LifecycleOwner,
    message: String
) = suspendCancellableCoroutine<GetUserResponse.OkResponse?> { continuation ->

    val alertDialog = materialAlertDialog {

        setMessage(message)

        okButton {
            continuation.resume(GetUserResponse.OkResponse)
        }
    }

    alertDialog.show()

    //this prevents window leak
    alertDialog.resumeNullOnDismiss(continuation)
    alertDialog.dismissOnDestroy(lifecycleOwner)
}

fun <T> Dialog.resumeNullOnDismiss(continuation: CancellableContinuation<T?>) {
    setOnDismissListener {
        if (!continuation.isCompleted) {
            continuation.resume(null)
        }
    }
}

fun Dialog.dismissOnDestroy(lifecycleOwner: LifecycleOwner) {
    lifecycleOwner.lifecycle.addObserver(object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            dismiss()
        }
    })
}