package io.github.sds100.keymapper.domain.settings

import io.github.sds100.keymapper.domain.preferences.Keys
import io.github.sds100.keymapper.domain.repositories.PreferenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Created by sds100 on 22/03/2021.
 */

class GetSettingsUseCaseImpl(val repository: PreferenceRepository) : GetSettingsUseCase {
    override val hideHomeScreenAlerts: Flow<Boolean> =
        repository.get(Keys.hideHomeScreenAlerts).map {
            it ?: false
        }
}

interface GetSettingsUseCase {
    val hideHomeScreenAlerts: Flow<Boolean>
}