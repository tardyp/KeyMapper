package io.github.sds100.keymapper.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.sds100.keymapper.domain.settings.ConfigSettingsUseCase
import io.github.sds100.keymapper.permissions.CheckRootPermissionUseCase
import io.github.sds100.keymapper.util.SharedPrefsDataStoreWrapper

/**
 * Created by sds100 on 19/01/21.
 */
class SettingsViewModel(
    private val config: ConfigSettingsUseCase,
    private val checkRootPermission: CheckRootPermissionUseCase
) : ViewModel() {
    val sharedPrefsDataStoreWrapper = SharedPrefsDataStoreWrapper(config)

    val automaticBackupLocation = config.automaticBackupLocation
    val hasRootPermission = checkRootPermission.isGranted

    fun setAutomaticBackupLocation(uri: String) = config.setAutomaticBackupLocation(uri)
    fun disableAutomaticBackup() = config.disableAutomaticBackup()

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val configSettingsUseCase: ConfigSettingsUseCase,
        private val checkRootPermission: CheckRootPermissionUseCase
    ) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SettingsViewModel(
                configSettingsUseCase,
                checkRootPermission
            ) as T
        }
    }
}