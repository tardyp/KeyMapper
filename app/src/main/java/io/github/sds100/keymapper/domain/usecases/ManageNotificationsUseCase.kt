package io.github.sds100.keymapper.domain.usecases

import io.github.sds100.keymapper.domain.preferences.Keys
import io.github.sds100.keymapper.domain.repositories.PreferenceRepository
import io.github.sds100.keymapper.domain.utils.FlowPrefDelegate

/**
 * Created by sds100 on 14/02/21.
 */
class ManageNotificationsUseCase(
    preferenceRepository: PreferenceRepository
) : PreferenceRepository by preferenceRepository {

    val keymapsPaused by FlowPrefDelegate(Keys.keymapsPaused, false)
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
        false
    )
}