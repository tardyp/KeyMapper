package io.github.sds100.keymapper.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.sds100.keymapper.util.SharedPrefsDataStoreWrapper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Created by sds100 on 19/01/21.
 */
class SettingsViewModel(private val config: ConfigSettingsUseCase) : ViewModel() {
    val sharedPrefsDataStoreWrapper = SharedPrefsDataStoreWrapper(config)

    val automaticBackupLocation = config.automaticBackupLocation
    val hasRootPermission = config.isRootGranted
    val showWriteSecureSettingsSection: StateFlow<Boolean> =
        config.isWriteSecureSettingsGranted.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun setAutomaticBackupLocation(uri: String) = config.setAutomaticBackupLocation(uri)
    fun disableAutomaticBackup() = config.disableAutomaticBackup()

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val configSettingsUseCase: ConfigSettingsUseCase,
    ) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SettingsViewModel(
                configSettingsUseCase
            ) as T
        }
    }
}