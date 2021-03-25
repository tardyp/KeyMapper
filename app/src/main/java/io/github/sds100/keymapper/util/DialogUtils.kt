package io.github.sds100.keymapper.util

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import io.github.sds100.keymapper.data.model.SeekBarListItemModel
import io.github.sds100.keymapper.databinding.DialogEdittextNumberBinding
import io.github.sds100.keymapper.databinding.DialogEdittextStringBinding
import io.github.sds100.keymapper.databinding.DialogSeekbarListBinding
import io.github.sds100.keymapper.ui.dialogs.DialogResponse
import io.github.sds100.keymapper.ui.dialogs.DialogUi
import io.github.sds100.keymapper.util.result.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.suspendCancellableCoroutine
import splitties.alertdialog.appcompat.*
import splitties.alertdialog.material.materialAlertDialog
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Created by sds100 on 30/03/2020.
 */

suspend fun DialogUi<*>.show(fragment: Fragment): DialogResponse {
    val lifecycleOwner = fragment.viewLifecycleOwner
    val ctx = fragment.requireContext()

    return show(lifecycleOwner, ctx)
}

suspend fun DialogUi<*>.show(lifecycleOwner: LifecycleOwner, ctx: Context): DialogResponse {
    return when (this) {
        is DialogUi.OkMessage -> ctx.okDialog(lifecycleOwner, this.message)
        is DialogUi.Text ->
            ctx.editTextStringAlertDialogFlow(
                lifecycleOwner,
                this.hint,
                this.allowEmpty
            )
        is DialogUi.MultiChoice<*> -> ctx.multiChoiceDialog(lifecycleOwner, this.items)
        is DialogUi.SingleChoice<*> -> ctx.singleChoiceDialog(lifecycleOwner, this.items)
    }
}

suspend fun <ID> Context.multiChoiceDialog(
    lifecycleOwner: LifecycleOwner,
    items: List<Pair<ID, String>>
) = suspendCancellableCoroutine<DialogUi.MultiChoiceResponse<ID>> { continuation ->
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

            continuation.resume(DialogUi.MultiChoiceResponse(checkedItemIds))
        }

        show().apply {
            dismissOnDestroy(lifecycleOwner)
        }
    }
}

suspend fun <ID> Context.singleChoiceDialog(
    lifecycleOwner: LifecycleOwner,
    items: List<Pair<ID, String>>
) = suspendCancellableCoroutine<DialogUi.SingleChoiceResponse<ID>> { continuation ->
    materialAlertDialog {
        setItems(
            items.map { it.second }.toTypedArray(),
        ) { _, position ->
            continuation.resume(DialogUi.SingleChoiceResponse(items[position].first))
        }

        show().apply {
            dismissOnDestroy(lifecycleOwner)
        }
    }
}

//TODO delete
suspend fun Context.editTextStringAlertDialog(
    lifecycleOwner: LifecycleOwner,
    hint: String,
    allowEmpty: Boolean = false
) = suspendCoroutine<String?> { continuation ->
    alertDialog {
        val inflater = LayoutInflater.from(this@editTextStringAlertDialog)

        DialogEdittextStringBinding.inflate(inflater).apply {
            val text = MutableStateFlow("")

            setHint(hint)
            setText(text)
            setAllowEmpty(allowEmpty)

            setView(this.root)

            okButton {
                continuation.resume(text.value)
            }

            cancelButton()

            show().apply {
                lifecycleOwner.lifecycle.coroutineScope.launchWhenResumed {
                    text.collectLatest {
                        getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
                            if (allowEmpty) {
                                true
                            } else {
                                it.isNotBlank()
                            }
                    }
                }
            }

            onDismiss {
                continuation.resume(null)
            }

            setOnCancelListener {
                continuation.resume(null)
            }
        }
    }
}

suspend fun Context.editTextStringAlertDialogFlow(
    lifecycleOwner: LifecycleOwner,
    hint: String,
    allowEmpty: Boolean = false
) = suspendCancellableCoroutine<DialogUi.TextResponse> { continuation ->

    val text = MutableStateFlow("")

    val alertDialog = materialAlertDialog {
        val inflater = LayoutInflater.from(this@editTextStringAlertDialogFlow)

        DialogEdittextStringBinding.inflate(inflater).apply {
            setHint(hint)
            setText(text)
            setAllowEmpty(allowEmpty)

            setView(this.root)
        }

        okButton {
            continuation.resume(DialogUi.TextResponse(text.value))
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
    alertDialog.dismissOnDestroy(lifecycleOwner)
}

suspend fun Context.editTextNumberAlertDialog(
    lifecycleOwner: LifecycleOwner,
    hint: String,
    min: Int? = null,
    max: Int? = null
) = suspendCoroutine<Int> {

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

    alertDialog {
        val inflater = LayoutInflater.from(this@editTextNumberAlertDialog)
        DialogEdittextNumberBinding.inflate(inflater).apply {
            val text = MutableLiveData("")

            setHint(hint)
            setText(text)

            setView(this.root)

            okButton { _ ->
                isValid(text.value).onSuccess { num ->
                    it.resume(num)
                }
            }

            cancelButton()

            show().apply {
                text.observe(lifecycleOwner, {
                    val result = isValid(it)

                    getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = result.isSuccess

                    textInputLayout.error = result.errorOrNull()?.getFullMessage(context)
                })
            }
        }
    }
}

suspend fun Context.okDialog(
    lifecycleOwner: LifecycleOwner,
    message: String
) = suspendCancellableCoroutine<DialogUi.OkResponse> { continuation ->

    val alertDialog = materialAlertDialog {

        setMessage(message)

        okButton {
            continuation.resume(DialogUi.OkResponse)
        }
    }

    alertDialog.show()

    //this prevents window leak
    alertDialog.dismissOnDestroy(lifecycleOwner)
}

fun Dialog.dismissOnDestroy(lifecycleOwner: LifecycleOwner) {
    lifecycleOwner.lifecycle.addObserver(object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            dismiss()
        }
    })
}

suspend fun Context.seekBarAlertDialog(
    lifecycleOwner: LifecycleOwner,
    seekBarListItemModel: SeekBarListItemModel
) = suspendCoroutine<Int> {
    alertDialog {
        val inflater = LayoutInflater.from(this@seekBarAlertDialog)
        DialogSeekbarListBinding.inflate(inflater).apply {

            var result = seekBarListItemModel.initialValue

            model = seekBarListItemModel

            onChangeListener = object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    result = seekBarListItemModel.calculateValue(progress)

                    textViewValue.text = result.toString()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            }

            okButton { _ ->
                it.resume(result)
            }


            setView(this.root)
        }

        cancelButton()

        show()
    }
}