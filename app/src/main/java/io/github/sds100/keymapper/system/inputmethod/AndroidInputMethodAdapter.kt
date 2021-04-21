package io.github.sds100.keymapper.system.inputmethod

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import io.github.sds100.keymapper.ServiceLocator
import io.github.sds100.keymapper.system.JobSchedulerHelper
import io.github.sds100.keymapper.system.SettingsUtils
import io.github.sds100.keymapper.system.accessibility.ServiceAdapter
import io.github.sds100.keymapper.system.permissions.Permission
import io.github.sds100.keymapper.system.permissions.PermissionAdapter
import io.github.sds100.keymapper.system.root.RootUtils
import io.github.sds100.keymapper.util.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Created by sds100 on 14/02/2021.
 */

class AndroidInputMethodAdapter(
    context: Context,
    private val serviceAdapter: ServiceAdapter,
    permissionAdapter: PermissionAdapter
) : InputMethodAdapter {

    companion object {
        const val SETTINGS_SECURE_SUBTYPE_HISTORY_KEY = "input_methods_subtype_history"
    }

    override val chosenIme by lazy { MutableStateFlow(getChosenIme()) }

    override val inputMethodHistory by lazy { MutableStateFlow(getInputMethods()) }
    override val inputMethods by lazy { MutableStateFlow(getInputMethods()) }

    override val isUserInputRequiredToChangeIme: Flow<Boolean> = channelFlow {
        suspend fun invalidate() {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
                    && serviceAdapter.isEnabled.first() -> send(true)

                permissionAdapter.isGranted(Permission.WRITE_SECURE_SETTINGS) -> send(true)

                else -> send(false)
            }
        }

        invalidate()

        launch {
            permissionAdapter.onPermissionsUpdate.collectLatest {
                invalidate()
            }
        }

        launch {
            serviceAdapter.isEnabled.collectLatest {
                invalidate()
            }
        }
    }

    val broadcastReceiver = object : BroadcastReceiver() {
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
            val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean, uri: Uri?) {
                    super.onChange(selfChange, uri)

                    onInputMethodsUpdate()
                }
            }

            ctx.contentResolver.registerContentObserver(
                Settings.Secure.getUriFor(Settings.Secure.ENABLED_INPUT_METHODS),
                false,
                observer
            )

            ctx.contentResolver.registerContentObserver(
                Settings.Secure.getUriFor(
                    SETTINGS_SECURE_SUBTYPE_HISTORY_KEY
                ), false, observer
            )
        }

        IntentFilter().apply {
            addAction(Intent.ACTION_INPUT_METHOD_CHANGED)

            ctx.registerReceiver(broadcastReceiver, this)
        }
    }

    override fun showImePicker(fromForeground: Boolean): Result<Unit> {
        when {
            fromForeground || Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1 -> {
                inputMethodManager.showInputMethodPicker()
                return Success(Unit)
            }

            (Build.VERSION_CODES.O_MR1..Build.VERSION_CODES.P).contains(Build.VERSION.SDK_INT) -> {
                val command =
                    "am broadcast -a com.android.server.InputMethodManagerService.SHOW_INPUT_METHOD_PICKER"
                RootUtils.executeRootCommand(command)

                return Success(Unit)
            }

            else -> return Error.CantShowImePickerInBackground
        }
    }

    override fun enableIme(imeId: String): Result<Unit> {
        return enableImeWithoutUserInput(imeId).then {
            try {
                val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_TASK

                ctx.startActivity(intent)
                Success(Unit)
            } catch (e: Exception) {
                Error.CantFindImeSettings
            }
        }
    }

    private fun enableImeWithoutUserInput(imeId: String): Result<Unit> {
        if (permissionAdapter.isGranted(Permission.ROOT)) {
            RootUtils.executeRootCommand("ime enable $imeId")
            return Success(Unit)
        } else {
            return Error.PermissionDenied(Permission.ROOT)
        }
    }

    override suspend fun chooseIme(imeId: String, fromForeground: Boolean): Result<ImeInfo> {
        getInfoById(imeId).onFailure {
            return it
        }

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && serviceAdapter.isEnabled.value -> {
                runBlocking { serviceAdapter.send(ChangeIme(imeId)) }.onFailure {
                    return it
                }
            }

            permissionAdapter.isGranted(Permission.WRITE_SECURE_SETTINGS) -> {
                SettingsUtils.putSecureSetting(
                    ctx,
                    Settings.Secure.DEFAULT_INPUT_METHOD,
                    imeId
                )
            }

            else -> showImePicker(fromForeground).onFailure { return it }
        }

        //wait for the ime to change and then return the info of the ime
        return Success(chosenIme.first { it.id == imeId })
    }

    override fun getInfoById(imeId: String): Result<ImeInfo> {
        val info =
            inputMethods.value.find { it.id == imeId } ?: return Error.InputMethodNotFound(imeId)

        return Success(info)
    }

    override fun getInfoByPackageName(packageName: String): Result<ImeInfo> {
        return getImeId(packageName).then { getInfoById(it) }
    }

    /**
     * Example:
     * io.github.sds100.keymapper.system.inputmethod.latin/.LatinIME;-921088104
     * :com.google.android.inputmethod.latin/com.android.inputmethod.latin.LatinIME;1891618174
     */
    private fun getImeHistory(): List<String> {
        val ids = getSubtypeHistoryString(ctx)
            .split(':')
            .map { it.split(';')[0] }

        return ids
    }

    fun onInputMethodsUpdate() {
        inputMethods.value = getInputMethods()
        inputMethodHistory.value = getImeHistory().mapNotNull { getInfoById(it).valueOrNull() }
    }

    private fun getInputMethods(): List<ImeInfo> {

        val chosenImeId = getChosenImeId()

        val enabledInputMethods = inputMethodManager.enabledInputMethodList

        return inputMethodManager.inputMethodList.map { inputMethodInfo ->
            ImeInfo(
                inputMethodInfo.id,
                inputMethodInfo.packageName,
                inputMethodInfo.loadLabel(ctx.packageManager).toString(),
                isChosen = inputMethodInfo.id == chosenImeId,
                isEnabled = enabledInputMethods.any { it.id == inputMethodInfo.id }
            )
        }
    }

    private fun getSubtypeHistoryString(ctx: Context): String {
        return Settings.Secure.getString(
            ctx.contentResolver,
            SETTINGS_SECURE_SUBTYPE_HISTORY_KEY
        )
    }

    private fun getChosenIme(): ImeInfo {
        val chosenImeId = getChosenImeId()

        return getInfoById(chosenImeId).valueOrNull()!!
    }

    private fun getChosenImeId(): String {
        return Settings.Secure.getString(ctx.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
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