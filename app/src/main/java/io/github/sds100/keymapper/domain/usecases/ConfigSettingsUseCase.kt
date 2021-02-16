package io.github.sds100.keymapper.domain.usecases

import androidx.datastore.preferences.core.Preferences
import io.github.sds100.keymapper.domain.preferences.Keys
import io.github.sds100.keymapper.domain.repositories.PreferenceRepository
import io.github.sds100.keymapper.domain.utils.FlowPrefDelegate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Created by sds100 on 14/02/2021.
 */
class ConfigSettingsUseCaseImpl(private val preferenceRepository: PreferenceRepository) :
    PreferenceRepository by preferenceRepository, ConfigSettingsUseCase {

    override val hasRootPermission by FlowPrefDelegate(Keys.hasRootPermission, false)

    override fun <T> getPreference(key: Preferences.Key<T>) =
        preferenceRepository.get(key)

    override fun <T> setPreference(key: Preferences.Key<T>, value: T?) =
        preferenceRepository.set(key, value)

    override val automaticBackupLocation =
        preferenceRepository.get(Keys.automaticBackupLocation).map { it ?: "" }

    override fun setAutomaticBackupLocation(uri: String) {
        preferenceRepository.set(Keys.automaticBackupLocation, uri)
    }

    override fun disableAutomaticBackup() {
        preferenceRepository.set(Keys.automaticBackupLocation, null)
    }
}

interface ConfigSettingsUseCase {
    val hasRootPermission: Flow<Boolean>
    fun <T> getPreference(key: Preferences.Key<T>): Flow<T?>
    fun <T> setPreference(key: Preferences.Key<T>, value: T?)
    val automaticBackupLocation: Flow<String>
    fun setAutomaticBackupLocation(uri: String)
    fun disableAutomaticBackup()
}