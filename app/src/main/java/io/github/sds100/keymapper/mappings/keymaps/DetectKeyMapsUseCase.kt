package io.github.sds100.keymapper.mappings.keymaps

import android.os.SystemClock
import io.github.sds100.keymapper.system.display.DisplayAdapter
import io.github.sds100.keymapper.mappings.DetectMappingUseCase
import io.github.sds100.keymapper.data.Keys
import io.github.sds100.keymapper.data.PreferenceDefaults
import io.github.sds100.keymapper.data.repositories.PreferenceRepository
import io.github.sds100.keymapper.util.State
import io.github.sds100.keymapper.system.permissions.CheckRootPermissionUseCase
import io.github.sds100.keymapper.system.accessibility.IAccessibilityService
import io.github.sds100.keymapper.util.InputEventType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

/**
 * Created by sds100 on 17/04/2021.
 */

class DetectKeyMapsUseCaseImpl(
    accessibilityService: IAccessibilityService,
    detectMappingUseCase: DetectMappingUseCase,
    private val keyMapRepository: KeyMapRepository,
    private val preferenceRepository: PreferenceRepository,
    private val checkRootPermission: CheckRootPermissionUseCase,
    private val displayAdapter: DisplayAdapter
) : DetectKeyMapsUseCase, DetectMappingUseCase by detectMappingUseCase {
    override val allKeyMapList: Flow<List<KeyMap>> =
        keyMapRepository.keyMapList
            .dropWhile { it !is State.Data }
            .map { state ->
                (state as State.Data).data.map { KeyMapEntityMapper.fromEntity(it) }
            }.flowOn(Dispatchers.Default)

    override val keyMapsToTriggerFromOtherApps: Flow<List<KeyMap>> =
        allKeyMapList.map { keyMapList ->
            keyMapList.filter { it.trigger.triggerFromOtherApps }
        }.flowOn(Dispatchers.Default)

    override val detectScreenOffTriggers: Flow<Boolean> =
        combine(
            allKeyMapList,
            checkRootPermission.isGranted
        ) { keyMapList, isRootPermissionGranted ->
            keyMapList.any { it.trigger.screenOffTrigger } && isRootPermissionGranted
        }.flowOn(Dispatchers.Default)

    override val defaultLongPressDelay: Flow<Long> =
        preferenceRepository.get(Keys.defaultLongPressDelay)
            .map { it ?: PreferenceDefaults.LONG_PRESS_DELAY }
            .map { it.toLong() }

    override val defaultDoublePressDelay: Flow<Long> =
        preferenceRepository.get(Keys.defaultDoublePressDelay)
            .map { it ?: PreferenceDefaults.DOUBLE_PRESS_DELAY }
            .map { it.toLong() }

    override val defaultRepeatDelay: Flow<Long> =
        preferenceRepository.get(Keys.defaultRepeatDelay)
            .map { it ?: PreferenceDefaults.REPEAT_DELAY }
            .map { it.toLong() }

    override val defaultSequenceTriggerTimeout: Flow<Long> =
        preferenceRepository.get(Keys.defaultSequenceTriggerTimeout)
            .map { it ?: PreferenceDefaults.SEQUENCE_TRIGGER_TIMEOUT }
            .map { it.toLong() }

    override val currentTime: Long
        get() = SystemClock.elapsedRealtime()

    override fun imitateButtonPress(
        keyCode: Int,
        metaState: Int,
        deviceId: Int,
        keyEventAction: InputEventType,
        scanCode: Int
    ) {
        TODO("Not yet implemented")
    }

    override val isScreenOn: Flow<Boolean> = displayAdapter.isScreenOn
}

interface DetectKeyMapsUseCase : DetectMappingUseCase {
    val allKeyMapList: Flow<List<KeyMap>>
    val keyMapsToTriggerFromOtherApps: Flow<List<KeyMap>>
    val detectScreenOffTriggers: Flow<Boolean>

    val defaultLongPressDelay: Flow<Long>
    val defaultDoublePressDelay: Flow<Long>
    val defaultRepeatDelay: Flow<Long>
    val defaultSequenceTriggerTimeout: Flow<Long>

    val currentTime: Long

    fun imitateButtonPress(
        keyCode: Int,
        metaState: Int = 0,
        deviceId: Int = 0,
        keyEventAction: InputEventType =InputEventType.DOWN_UP,
        scanCode: Int = 0
    )

    val isScreenOn: Flow<Boolean>
}