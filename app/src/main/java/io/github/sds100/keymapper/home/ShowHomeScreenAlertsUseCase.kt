package io.github.sds100.keymapper.home

import io.github.sds100.keymapper.domain.adapter.PermissionAdapter
import io.github.sds100.keymapper.domain.adapter.ServiceAdapter
import io.github.sds100.keymapper.domain.preferences.Keys
import io.github.sds100.keymapper.domain.repositories.PreferenceRepository
import io.github.sds100.keymapper.permissions.Permission
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map

/**
 * Created by sds100 on 04/04/2021.
 */

class ShowHomeScreenAlertsUseCaseImpl(
    private val preferences: PreferenceRepository,
    private val permissions: PermissionAdapter,
    private val serviceAdapter: ServiceAdapter
) : ShowHomeScreenAlertsUseCase {
    override val hideAlerts: Flow<Boolean> =
        preferences.get(Keys.hideHomeScreenAlerts).map { it ?: false }

    override val isBatteryOptimised: Flow<Boolean> = channelFlow {
        send(!permissions.isGranted(Permission.IGNORE_BATTERY_OPTIMISATION))

        permissions.onPermissionsUpdate.collectLatest {
            send(!permissions.isGranted(Permission.IGNORE_BATTERY_OPTIMISATION))
        }
    }

    override fun disableBatteryOptimisation() {
        permissions.request(Permission.IGNORE_BATTERY_OPTIMISATION)
    }

    override val isAccessibilityServiceEnabled: Flow<Boolean> = serviceAdapter.isEnabled
    override fun enableAccessibilityService() {
        serviceAdapter.enableService()
    }
}

interface ShowHomeScreenAlertsUseCase{
    val isAccessibilityServiceEnabled: Flow<Boolean>
    fun enableAccessibilityService()

    val hideAlerts: Flow<Boolean>
    fun disableBatteryOptimisation()
    val isBatteryOptimised: Flow<Boolean>
}