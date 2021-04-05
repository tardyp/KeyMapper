package io.github.sds100.keymapper.mappings.keymaps

import android.Manifest
import android.os.Build
import android.view.KeyEvent
import io.github.sds100.keymapper.domain.adapter.PermissionAdapter
import io.github.sds100.keymapper.domain.mappings.keymap.trigger.KeyMapTrigger
import io.github.sds100.keymapper.mappings.common.DisplayActionUseCase
import io.github.sds100.keymapper.mappings.common.DisplayConstraintUseCase
import io.github.sds100.keymapper.mappings.common.DisplaySimpleMappingUseCase

/**
 * Created by sds100 on 04/04/2021.
 */

class DisplayKeyMapUseCaseImpl(
    private val permissionAdapter: PermissionAdapter,
    displaySimpleMappingUseCase: DisplaySimpleMappingUseCase
) : DisplayKeyMapUseCase, DisplaySimpleMappingUseCase by displaySimpleMappingUseCase {
    private companion object {
        val keysThatRequireDndAccess = arrayOf(
            KeyEvent.KEYCODE_VOLUME_DOWN,
            KeyEvent.KEYCODE_VOLUME_UP
        )
    }

    override fun getTriggerError(trigger: KeyMapTrigger): List<KeyMapTriggerError> {
        val errors = mutableListOf<KeyMapTriggerError>()

        if (trigger.keys.any { it.keyCode in keysThatRequireDndAccess }) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !permissionAdapter.isGranted(Manifest.permission.ACCESS_NOTIFICATION_POLICY)
            ) {
                errors.add(KeyMapTriggerError.DND_ACCESS_DENIED)
            }
        }

        return errors
    }
}

interface DisplayKeyMapUseCase : DisplayActionUseCase, DisplayConstraintUseCase {
    fun getTriggerError(trigger: KeyMapTrigger): List<KeyMapTriggerError>
}