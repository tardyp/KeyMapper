package io.github.sds100.keymapper.domain.usecases

import io.github.sds100.keymapper.domain.preferences.Keys
import io.github.sds100.keymapper.domain.repositories.PreferenceRepository
import io.github.sds100.keymapper.domain.utils.FlowPrefDelegate

/**
 * Created by sds100 on 14/02/21.
 */

//TODO add isaccessibilityserviceenabled method
class ManageNotificationsUseCase(
    preferenceRepository: PreferenceRepository
) : PreferenceRepository by preferenceRepository {

    val keymapsPaused by FlowPrefDelegate(Keys.mappingsPaused, false)
    val hasRootPermission by FlowPrefDelegate(Keys.hasRootPermission, false)

    val showImePickerNotification by FlowPrefDelegate(
        Keys.showImePickerNotification,
        false
    )

    val showToggleKeyboardNotification by FlowPrefDelegate(
        Keys.showToggleKeyboardNotification,
        false
    )

    val showToggleKeymapsNotification by FlowPrefDelegate(
        Keys.showToggleKeymapsNotification,
        true
    )
}