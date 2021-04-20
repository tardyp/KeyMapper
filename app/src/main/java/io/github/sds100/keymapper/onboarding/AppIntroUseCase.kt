package io.github.sds100.keymapper.onboarding

import android.content.pm.PackageManager
import android.os.Build
import io.github.sds100.keymapper.system.permissions.PermissionAdapter
import io.github.sds100.keymapper.system.accessibility.ServiceAdapter
import io.github.sds100.keymapper.system.permissions.SystemFeatureAdapter
import io.github.sds100.keymapper.mappings.fingerprintmaps.AreFingerprintGesturesSupportedUseCase
import io.github.sds100.keymapper.data.Keys
import io.github.sds100.keymapper.data.repositories.PreferenceRepository
import io.github.sds100.keymapper.system.permissions.Permission
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest

/**
 * Created by sds100 on 14/04/2021.
 */
class AppIntroUseCaseImpl(
    private val permissionAdapter: PermissionAdapter,
    private val serviceAdapter: ServiceAdapter,
    private val systemFeatureAdapter: SystemFeatureAdapter,
    private val preferenceRepository: PreferenceRepository,
    private val fingerprintGesturesSupportedUseCase: AreFingerprintGesturesSupportedUseCase
) : AppIntroUseCase {
    override val isAccessibilityServiceEnabled: Flow<Boolean> = serviceAdapter.isEnabled

    override val hasDndAccessPermission: Flow<Boolean> = channelFlow {
        send(permissionAdapter.isGranted(Permission.ACCESS_NOTIFICATION_POLICY))

        permissionAdapter.onPermissionsUpdate.collectLatest {
            send(permissionAdapter.isGranted(Permission.ACCESS_NOTIFICATION_POLICY))
        }
    }

    override val isBatteryOptimised: Flow<Boolean> = channelFlow {
        send(!permissionAdapter.isGranted(Permission.IGNORE_BATTERY_OPTIMISATION))

        permissionAdapter.onPermissionsUpdate.collectLatest {
            send(!permissionAdapter.isGranted(Permission.IGNORE_BATTERY_OPTIMISATION))
        }
    }

    override val fingerprintGesturesSupported: Flow<Boolean?> =
        fingerprintGesturesSupportedUseCase.isSupported

    override fun deviceHasFingerprintReader(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            systemFeatureAdapter.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
        } else {
            false
        }
    }

    override fun ignoreBatteryOptimisation() {
        permissionAdapter.request(Permission.IGNORE_BATTERY_OPTIMISATION)
    }

    override fun enableAccessibilityService() {
        serviceAdapter.enableService()
    }

    override fun requestDndAccess() {
        permissionAdapter.request(Permission.ACCESS_NOTIFICATION_POLICY)
    }

    override fun shownAppIntro() {
        preferenceRepository.set(Keys.approvedFingerprintFeaturePrompt, true)
        preferenceRepository.set(Keys.shownAppIntro, true)
    }
}

interface AppIntroUseCase {
    val isAccessibilityServiceEnabled: Flow<Boolean>
    val hasDndAccessPermission: Flow<Boolean>
    val isBatteryOptimised: Flow<Boolean>
    val fingerprintGesturesSupported: Flow<Boolean?>

    fun deviceHasFingerprintReader(): Boolean
    fun ignoreBatteryOptimisation()
    fun enableAccessibilityService()
    fun requestDndAccess()

    fun shownAppIntro()
}