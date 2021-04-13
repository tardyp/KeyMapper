package io.github.sds100.keymapper.home

import android.os.Build
import io.github.sds100.keymapper.data.repository.FingerprintMapRepository
import io.github.sds100.keymapper.domain.adapter.InputMethodAdapter
import io.github.sds100.keymapper.domain.adapter.PermissionAdapter
import io.github.sds100.keymapper.domain.adapter.ServiceAdapter
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.FingerprintMapEntityMapper
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.FingerprintMapId
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.FingerprintMapIdEntityMapper
import io.github.sds100.keymapper.domain.mappings.keymap.KeyMap
import io.github.sds100.keymapper.domain.mappings.keymap.KeyMapEntityMapper
import io.github.sds100.keymapper.domain.mappings.keymap.KeyMapRepository
import io.github.sds100.keymapper.domain.preferences.Keys
import io.github.sds100.keymapper.domain.repositories.PreferenceRepository
import io.github.sds100.keymapper.domain.utils.State
import io.github.sds100.keymapper.domain.utils.mapData
import io.github.sds100.keymapper.mappings.common.DisplaySimpleMappingUseCase
import io.github.sds100.keymapper.mappings.fingerprintmaps.FingerprintMapGroup
import io.github.sds100.keymapper.mappings.keymaps.DisplayKeyMapUseCase
import io.github.sds100.keymapper.permissions.Permission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

/**
 * Created by sds100 on 04/04/2021.
 */

class HomeScreenUseCaseImpl(
    private val keyMapRepository: KeyMapRepository,
    private val fingerprintMapRepository: FingerprintMapRepository,
    private val preferenceRepository: PreferenceRepository,
    private val serviceAdapter: ServiceAdapter,
    private val permissionAdapter: PermissionAdapter,
    private val inputMethodAdapter: InputMethodAdapter,
    displayKeyMapUseCase: DisplayKeyMapUseCase,
    displaySimpleMappingUseCase: DisplaySimpleMappingUseCase
) : HomeScreenUseCase,
    DisplayKeyMapUseCase by displayKeyMapUseCase {

    override val keyMapList: Flow<State<List<KeyMap>>> = keyMapRepository.keyMapList
        .map { state ->
            state.mapData { keymapList ->
                keymapList.map {
                    KeyMapEntityMapper.fromEntity(it)
                }
            }
        }.flowOn(Dispatchers.Default)

    override val fingerprintMaps: Flow<FingerprintMapGroup> =
        fingerprintMapRepository.fingerprintMaps
            .map { entityGroup ->
                FingerprintMapGroup(
                    swipeDown = FingerprintMapEntityMapper.fromEntity(entityGroup.swipeDown),
                    swipeUp = FingerprintMapEntityMapper.fromEntity(entityGroup.swipeUp),
                    swipeLeft = FingerprintMapEntityMapper.fromEntity(entityGroup.swipeLeft),
                    swipeRight = FingerprintMapEntityMapper.fromEntity(entityGroup.swipeRight),
                )
            }.flowOn(Dispatchers.Default)

    override fun deleteKeyMap(vararg uid: String) {
        keyMapRepository.delete(*uid)
    }

    override fun enableKeyMap(vararg uid: String) {
        keyMapRepository.enableById(*uid)
    }

    override fun disableKeyMap(vararg uid: String) {
        keyMapRepository.disableById(*uid)
    }

    override fun duplicateKeyMap(vararg uid: String) {
        keyMapRepository.duplicate(*uid)
    }

    override fun resetFingerprintMaps() {
        fingerprintMapRepository.reset()
    }

    override fun enableFingerprintMap(id: FingerprintMapId) {
        val entityId = FingerprintMapIdEntityMapper.toEntity(id)
        fingerprintMapRepository.enableFingerprintMap(entityId)
    }

    override fun disableFingerprintMap(id: FingerprintMapId) {
        val entityId = FingerprintMapIdEntityMapper.toEntity(id)
        fingerprintMapRepository.disableFingerprintMap(entityId)
    }

    override fun enableAllMappings() {
        keyMapRepository.enableAll()
        fingerprintMapRepository.enableAll()
    }

    override fun disableAllMappings() {
        keyMapRepository.disableAll()
        fingerprintMapRepository.disableAll()
    }

    override fun backupAllMappings(uri: String) {
        TODO("Not yet implemented")
    }

    override fun restoreMappings(uri: String) {
        TODO("Not yet implemented")
    }

    override fun showInputMethodPicker() {
        inputMethodAdapter.showImePicker(fromForeground = true)
    }

    override fun isBatteryOptimised(): Boolean {
        return !permissionAdapter.isGranted(Permission.IGNORE_BATTERY_OPTIMISATION)
    }

    override val isAccessibilityServiceEnabled: Flow<Boolean> = serviceAdapter.isEnabled

    override val areFingerprintGesturesSupported: Flow<Boolean> =
        preferenceRepository.get(Keys.fingerprintGesturesAvailable).map {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return@map false

            it ?: false
        }

    override val areMappingsPaused: Flow<Boolean> =
        preferenceRepository.get(Keys.mappingsPaused).map { it ?: false }

    override fun pauseMappings() {
        preferenceRepository.set(Keys.mappingsPaused, true)
    }

    override fun resumeMappings() {
        preferenceRepository.set(Keys.mappingsPaused, false)
    }

    override val hideHomeScreenAlerts: Flow<Boolean> =
        preferenceRepository.get(Keys.hideHomeScreenAlerts).map { it ?: false }

    override fun ignoreBatteryOptimisation() {
        permissionAdapter.request(Permission.IGNORE_BATTERY_OPTIMISATION)
    }

    override fun enableAccessibilityService() {
        serviceAdapter.enableService()
    }
}

interface HomeScreenUseCase : DisplayKeyMapUseCase, DisplaySimpleMappingUseCase {
    val keyMapList: Flow<State<List<KeyMap>>>
    fun deleteKeyMap(vararg uid: String)
    fun enableKeyMap(vararg uid: String)
    fun disableKeyMap(vararg uid: String)
    fun duplicateKeyMap(vararg uid: String)

    val fingerprintMaps: Flow<FingerprintMapGroup>
    fun enableFingerprintMap(id: FingerprintMapId)
    fun disableFingerprintMap(id: FingerprintMapId)
    fun resetFingerprintMaps()

    fun enableAllMappings()
    fun disableAllMappings()
    fun backupAllMappings(uri: String)
    fun restoreMappings(uri: String)

    fun showInputMethodPicker()

    val isAccessibilityServiceEnabled: Flow<Boolean>
    fun enableAccessibilityService()

    fun ignoreBatteryOptimisation()
    fun isBatteryOptimised(): Boolean

    val areFingerprintGesturesSupported: Flow<Boolean>

    val areMappingsPaused: Flow<Boolean>
    fun pauseMappings()
    fun resumeMappings()

    val hideHomeScreenAlerts: Flow<Boolean>
}