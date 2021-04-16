package io.github.sds100.keymapper.data.viewmodel

import androidx.lifecycle.*
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.backup.BackupRestoreMappingsUseCase
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.home.ShowHomeScreenAlertsUseCase
import io.github.sds100.keymapper.inputmethod.ShowInputMethodPickerUseCase
import io.github.sds100.keymapper.mappings.PauseMappingsUseCase
import io.github.sds100.keymapper.ui.UserResponseViewModel
import io.github.sds100.keymapper.ui.UserResponseViewModelImpl
import io.github.sds100.keymapper.ui.dialogs.DialogResponse
import io.github.sds100.keymapper.ui.dialogs.GetUserResponse
import io.github.sds100.keymapper.ui.getUserResponse
import io.github.sds100.keymapper.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Created by sds100 on 17/11/20.
 */
class HomeMenuViewModel(
    private val coroutineScope: CoroutineScope,
    private val showAlerts: ShowHomeScreenAlertsUseCase,
    private val pauseMappings: PauseMappingsUseCase,
    private val backupRestore: BackupRestoreMappingsUseCase,
    private val showImePicker: ShowInputMethodPickerUseCase,
    resourceProvider: ResourceProvider
) : ResourceProvider by resourceProvider, UserResponseViewModel by UserResponseViewModelImpl() {

    val toggleMappingsButtonState: StateFlow<ToggleMappingsButtonState?> =
        combine(
            pauseMappings.isPaused,
            showAlerts.isAccessibilityServiceEnabled
        ) { isPaused, isServiceEnabled ->
            val text = when {
                //must be first
                !isServiceEnabled -> getString(R.string.button_enable_accessibility_service)
                isPaused -> getString(R.string.action_tap_to_resume_keymaps)
                else -> getString(R.string.action_tap_to_pause_keymaps)
            }

            val tint = when {
                !isServiceEnabled || !isPaused -> getColor(R.color.red)
                else -> getColor(R.color.green)
            }

            ToggleMappingsButtonState(text, tint)

        }.stateIn(coroutineScope, SharingStarted.Eagerly, null)

    private val _openSettings = MutableSharedFlow<Unit>()
    val openSettings = _openSettings.asSharedFlow()

    private val _openAbout = MutableSharedFlow<Unit>()
    val openAbout = _openAbout.asSharedFlow()

    private val _openUrl = MutableSharedFlow<String>()
    val openUrl = _openUrl.asSharedFlow()

    private val _emailDeveloper = MutableSharedFlow<Unit>()
    val emailDeveloper = _emailDeveloper.asSharedFlow()

    private val _chooseBackupFile = MutableSharedFlow<Unit>()
    val chooseBackupFile = _chooseBackupFile.asSharedFlow()

    private val _chooseRestoreFile = MutableSharedFlow<Unit>()
    val chooseRestoreFile = _chooseRestoreFile.asSharedFlow()

    private val _dismiss = MutableSharedFlow<Unit>()
    val dismiss = _dismiss

    fun onToggleMappingsButtonClick() {
        coroutineScope.launch {
            val areMappingsPaused = pauseMappings.isPaused.first()

            when {
                !showAlerts.isAccessibilityServiceEnabled.first() -> showAlerts.enableAccessibilityService()
                areMappingsPaused -> pauseMappings.resume()
                !areMappingsPaused -> pauseMappings.pause()
            }
        }
    }

    fun onShowInputMethodPickerClick() {
        runBlocking { _dismiss.emit(Unit) }
        showImePicker.show()
    }

    fun onOpenSettingsClick() {
        runBlocking { _dismiss.emit(Unit) }
        runBlocking { _openSettings.emit(Unit) }
    }

    fun onOpenAboutClick() {
        runBlocking { _dismiss.emit(Unit) }
        runBlocking { _openAbout.emit(Unit) }
    }

    fun onBackupAllClick() {
        runBlocking {
            _dismiss.emit(Unit)
            _chooseBackupFile.emit(Unit)
        }
    }

    fun onRestoreClick() {
        runBlocking {
            _dismiss.emit(Unit)
            _chooseRestoreFile.emit(Unit)
        }
    }

    fun onChoseRestoreFile(uri: String) {
        backupRestore.restoreMappings(uri)
    }

    fun onChoseBackupFile(uri: String) {
        backupRestore.backupAllMappings(uri)
    }

    fun onSendFeedbackClick() {
        coroutineScope.launch {
            val dialog = GetUserResponse.Dialog(
                title = getString(R.string.dialog_title_send_feedback),
                message = getString(R.string.dialog_message_view_faq_and_use_discord_over_email),
                positiveButtonText = getString(R.string.pos_faq_page),
                negativeButtonText = getString(R.string.neutral_discord),
                neutralButtonText = getString(R.string.neg_email)
            )

            val response = getUserResponse("send_feedback", dialog) ?: return@launch

            when (response) {
                DialogResponse.POSITIVE -> _openUrl.emit(getString(R.string.url_faq))
                DialogResponse.NEUTRAL -> _emailDeveloper.emit(Unit)
                DialogResponse.NEGATIVE -> _openUrl.emit(getString(R.string.url_discord_server_invite))
            }
        }
    }
}

data class ToggleMappingsButtonState(
    val text: String,
    val tint: Int
)