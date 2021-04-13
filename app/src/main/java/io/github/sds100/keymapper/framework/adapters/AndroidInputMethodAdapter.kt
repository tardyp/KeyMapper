package io.github.sds100.keymapper.framework.adapters

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.ServiceLocator
import io.github.sds100.keymapper.domain.adapter.InputMethodAdapter
import io.github.sds100.keymapper.domain.adapter.PermissionAdapter
import io.github.sds100.keymapper.domain.ime.ImeInfo
import io.github.sds100.keymapper.framework.JobSchedulerHelper
import io.github.sds100.keymapper.permissions.Permission
import io.github.sds100.keymapper.util.RootUtils
import io.github.sds100.keymapper.util.result.Error
import io.github.sds100.keymapper.util.result.Result
import io.github.sds100.keymapper.util.result.Success
import io.github.sds100.keymapper.util.result.onSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import splitties.toast.toast
import timber.log.Timber

/**
 * Created by sds100 on 14/02/2021.
 */

//TODO inject root process delegate
class AndroidInputMethodAdapter(context: Context) : InputMethodAdapter {

    companion object {
        private const val SETTINGS_SECURE_SUBTYPE_HISTORY_KEY = "input_methods_subtype_history"
    }

    override val chosenIme by lazy { MutableStateFlow(getChosenIme()) }

    override val enabledInputMethods by lazy { MutableStateFlow(getEnabledInputMethods()) }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return
            context ?: return

            when (intent.action) {
                Intent.ACTION_INPUT_METHOD_CHANGED -> {
                    chosenIme.value = getChosenIme()
                }
            }
        }
    }

    private val ctx = context.applicationContext

    private val inputMethodManager: InputMethodManager
        get() = ctx.getSystemService()!!

    private val permissionAdapter: PermissionAdapter by lazy { ServiceLocator.permissionAdapter(ctx) }

    init {
        //use job scheduler because there is there is a much shorter delay when the app is in the background
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            JobSchedulerHelper.observeEnabledInputMethods(ctx)
        } else {
            val uri = Settings.Secure.getUriFor(Settings.Secure.ENABLED_INPUT_METHODS)
            val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean, uri: Uri?) {
                    super.onChange(selfChange, uri)

                    onEnabledInputMethodsUpdate()
                }
            }

            ctx.contentResolver.registerContentObserver(uri, false, observer)
        }

        IntentFilter().apply {
            addAction(Intent.ACTION_INPUT_METHOD_CHANGED)

            ctx.registerReceiver(broadcastReceiver, this)
        }
    }

    override fun showImePicker(fromForeground: Boolean) {
        TODO("Not yet implemented")
    }

    override fun isImeEnabledById(imeId: String): Boolean {
        return enabledInputMethods.value.any { it.id == imeId }
    }

    override fun isImeEnabledByPackageName(packageName: String): Boolean {
        return enabledInputMethods.value.any { it.packageName == packageName }
    }

    override fun enableImeById(imeId: String) {
        if (permissionAdapter.isGranted(Permission.ROOT)) {
            RootUtils.executeRootCommand("ime enable $imeId")
        } else {
            try {
                val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_TASK

                ctx.startActivity(intent)
            } catch (e: Exception) {
                toast(R.string.error_cant_find_ime_settings)
            }
        }
    }

    override fun enableImeByPackageName(packageName: String) {
        getImeId(packageName).onSuccess {
            enableImeById(it)
        }
    }

    override fun isImeChosenById(imeId: String): Boolean {
        return chosenIme.value.id == imeId
    }

    override fun isImeChosenByPackageName(packageName: String): Boolean {
        return chosenIme.value.packageName == packageName
    }

    override fun chooseImeById(imeId: String) {
        TODO("Not yet implemented")
    }

    override fun chooseImeByPackageName(packageName: String) {
        TODO("Not yet implemented")
    }

    override fun getLabel(imeId: String): Result<String> {
        val label = enabledInputMethods.value.find { it.id == imeId }
            ?.label ?: return Error.InputMethodNotFound(imeId)

        return Success(label)
    }

    override fun getImeHistory(): List<String> {
        TODO("Not yet implemented")
    }

    fun onEnabledInputMethodsUpdate() {
        Timber.e("onupdate")
        enabledInputMethods.value = getEnabledInputMethods()
    }

    private fun getEnabledInputMethods(): List<ImeInfo> {
        return inputMethodManager.enabledInputMethodList.map {
            ImeInfo(it.id, it.packageName, it.loadLabel(ctx.packageManager).toString())
        }
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

    private fun getChosenIme(): ImeInfo {
        val chosenImeId = getChosenImeId()

        return inputMethodManager.inputMethodList
            .single { it.id == chosenImeId }
            .let { ImeInfo(it.id, it.packageName, it.loadLabel(ctx.packageManager).toString()) }
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

    private fun getImeId(packageName: String): Result<String> {
        val imeId = inputMethodManager.inputMethodList.find { it.packageName == packageName }?.id

        return if (imeId == null) {
            Error.InputMethodNotFound(packageName)
        } else {
            Success(imeId)
        }
    }
}