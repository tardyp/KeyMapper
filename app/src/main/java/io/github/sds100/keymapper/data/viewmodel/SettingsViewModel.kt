package io.github.sds100.keymapper.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.sds100.keymapper.domain.settings.ConfigSettingsUseCase
import io.github.sds100.keymapper.util.SharedPrefsDataStoreWrapper

/**
 * Created by sds100 on 19/01/21.
 */
class SettingsViewModel(
    private val useCase: ConfigSettingsUseCase
) : ViewModel() {
    val sharedPrefsDataStoreWrapper = SharedPrefsDataStoreWrapper(useCase)

    val automaticBackupLocation = useCase.automaticBackupLocation
    val hasRootPermission = useCase.hasRootPermission

    fun setAutomaticBackupLocation(uri: String) = useCase.setAutomaticBackupLocation(uri)
    fun disableAutomaticBackup() = useCase.disableAutomaticBackup()

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val configSettingsUseCase: ConfigSettingsUseCase
    ) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SettingsViewModel(configSettingsUseCase) as T
        }
    }
}