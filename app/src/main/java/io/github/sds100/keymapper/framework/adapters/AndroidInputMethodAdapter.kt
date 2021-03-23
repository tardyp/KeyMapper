package io.github.sds100.keymapper.framework.adapters

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import io.github.sds100.keymapper.domain.adapter.InputMethodAdapter
import io.github.sds100.keymapper.util.KeyboardUtils
import io.github.sds100.keymapper.util.result.*
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Created by sds100 on 14/02/2021.
 */

//TODO inject root process delegate
class AndroidInputMethodAdapter(context: Context) : InputMethodAdapter {
    companion object {
        private const val SETTINGS_SECURE_SUBTYPE_HISTORY_KEY = "input_methods_subtype_history"
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return
            context ?: return

            when (intent.action) {
                Intent.ACTION_INPUT_METHOD_CHANGED -> {
                    chosenImePackageName.value =
                        KeyboardUtils.getChosenInputMethodPackageName(ctx).valueOrNull()
                }
            }
        }
    }

    private val ctx = context.applicationContext

    override val chosenImePackageName = MutableStateFlow(
        getChosenInputMethodPackageName().valueOrNull()
    )

    private val inputMethodManager: InputMethodManager
        get() = ctx.getSystemService()!!


    init {
        IntentFilter().apply {
            addAction(Intent.ACTION_INPUT_METHOD_CHANGED)

            ctx.registerReceiver(broadcastReceiver, this)
        }
    }

    override fun showImePicker(fromForeground: Boolean) {
        TODO("Not yet implemented")
    }

    override fun isImeEnabled(imeId: String): Boolean {
        return inputMethodManager.enabledInputMethodList.any { it.id == imeId }
    }

    override fun enableIme(imeId: String) {
        //TODO
    }

    override fun isImeChosen(imeId: String): Boolean {
        return getChosenImeId() == imeId
    }

    override fun chooseIme(imeId: String) {
        TODO("Not yet implemented")
    }

    override fun getImeId(packageName: String): Result<String> {
        return inputMethodManager.inputMethodList
            .find { it.packageName == packageName }
            ?.id?.success() ?: Error.ImeNotFoundForPackage(packageName)
    }

    override fun getLabel(imeId: String): Result<String> {
        val label = inputMethodManager.enabledInputMethodList.find { it.id == imeId }
            ?.loadLabel(ctx.packageManager)?.toString() ?: return Error.InputMethodNotFound(imeId)

        return Success(label)
    }

    override fun getImeHistory(): List<String> {
        TODO("Not yet implemented")
    }

    private fun getSubtypeHistoryString(ctx: Context): String {
        return Settings.Secure.getString(
            ctx.contentResolver,
            SETTINGS_SECURE_SUBTYPE_HISTORY_KEY
        )
    }

    /**
     * Example:
     * io.github.sds100.keymapper.inputmethod.latin/.LatinIME;-921088104
     * :com.google.android.inputmethod.latin/com.android.inputmethod.latin.LatinIME;1891618174
     */
    private fun getInputMethodHistoryIds(ctx: Context): List<String> {
        return getSubtypeHistoryString(ctx)
            .split(':')
            .map { it.split(';')[0] }
    }

    private fun getChosenInputMethodPackageName(): Result<String> {
        val chosenImeId = getChosenImeId()

        return getImePackageName(chosenImeId)
    }

    private fun getChosenImeId(): String {
        return Settings.Secure.getString(ctx.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
    }

    private fun getImePackageName(imeId: String): Result<String> {
        val packageName = inputMethodManager.inputMethodList.find { it.id == imeId }?.packageName

        return if (packageName == null) {
            Error.InputMethodNotFound(imeId)
        } else {
            Success(packageName)
        }
    }
}