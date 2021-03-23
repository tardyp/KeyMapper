package io.github.sds100.keymapper.util

import android.content.Context
import android.view.LayoutInflater
import android.widget.SeekBar
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.*
import io.github.sds100.keymapper.data.model.SeekBarListItemModel
import io.github.sds100.keymapper.databinding.DialogEdittextNumberBinding
import io.github.sds100.keymapper.databinding.DialogEdittextStringBinding
import io.github.sds100.keymapper.databinding.DialogSeekbarListBinding
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
) = suspendCancellableCoroutine<String?> { continuation ->

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
            continuation.resume(text.value)
        }

        cancelButton()
    }

    alertDialog.show()

    lifecycleOwner.lifecycleScope.launchWhenResumed {
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
    lifecycleOwner.lifecycle.addObserver(object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            alertDialog.dismiss()
        }
    })
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

/**
 * @return whether the ok button was pressed
 */
suspend fun Context.okDialog(@StringRes messageRes: Int) =
    suspendCoroutine<Boolean> { continuation ->
        alertDialog {
            message = str(messageRes)

            okButton {
                continuation.resume(true)
            }
        }
    }