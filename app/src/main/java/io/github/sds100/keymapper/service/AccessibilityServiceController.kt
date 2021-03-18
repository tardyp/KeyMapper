package io.github.sds100.keymapper.service

import android.os.Build
import android.view.KeyEvent
import androidx.annotation.RequiresApi
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import com.hadilq.liveevent.LiveEvent
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.data.model.ActionEntity
import io.github.sds100.keymapper.data.model.KeyMapEntity
import io.github.sds100.keymapper.data.model.TriggerEntity
import io.github.sds100.keymapper.data.repository.FingerprintMapRepository
import io.github.sds100.keymapper.data.usecase.GlobalKeymapUseCase
import io.github.sds100.keymapper.domain.usecases.DetectKeymapsUseCase
import io.github.sds100.keymapper.domain.usecases.GetKeymapsPausedUseCase
import io.github.sds100.keymapper.domain.usecases.OnboardingUseCase
import io.github.sds100.keymapper.domain.usecases.PerformActionsUseCase
import io.github.sds100.keymapper.util.*
import io.github.sds100.keymapper.util.delegate.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import splitties.bitflags.hasFlag
import splitties.toast.toast
import timber.log.Timber

/**
 * Created by sds100 on 18/01/21.
 */
class AccessibilityServiceController(
    lifecycleOwner: LifecycleOwner,
    constraintState: IConstraintState,
    clock: IClock,
    actionError: IActionError,
    fingerprintGestureDetectionState: FingerprintGestureDetectionState,
    private val getKeymapsPaused: GetKeymapsPausedUseCase,
    onboarding: OnboardingUseCase,
    private val detectKeymapsUseCase: DetectKeymapsUseCase,
    private val performActionsUseCase: PerformActionsUseCase,
    private val fingerprintMapRepository: FingerprintMapRepository,
    private val keymapRepository: GlobalKeymapUseCase
) : FingerprintGestureDetectionState by fingerprintGestureDetectionState,
    LifecycleOwner by lifecycleOwner {

    companion object {
        /**
         * How long should the accessibility service record a trigger in seconds.
         */
        private const val RECORD_TRIGGER_TIMER_LENGTH = 5
    }

    private val constraintDelegate = ConstraintDelegate(constraintState)

    private val triggerKeymapByIntentController = TriggerKeymapByIntentController(
        lifecycleScope,
        performActionsUseCase,
        constraintDelegate,
        actionError
    )

    private val fingerprintGestureMapController = FingerprintGestureMapController(
        lifecycleScope,
        performActionsUseCase,
        constraintDelegate,
        actionError
    )

    private var recordingTriggerJob: Job? = null

    private val recordingTrigger: Boolean
        get() = recordingTriggerJob != null && recordingTriggerJob?.isActive == true

    private val getEventDelegate =
        GetEventDelegate { keyCode, action, deviceDescriptor, isExternal, deviceId ->

            if (!getKeymapsPaused().firstBlocking()) {
                withContext(Dispatchers.Main.immediate) {
                    keymapDetectionDelegate.onKeyEvent(
                        keyCode,
                        action,
                        deviceDescriptor,
                        isExternal,
                        0,
                        deviceId
                    )
                }
            }
        }

    private var screenOffTriggersEnabled = false

    private val keymapDetectionDelegate = KeymapDetectionDelegate(
        lifecycleScope,
        getKeymapDetectionDelegatePreferences(),
        clock,
        constraintDelegate,
        canActionBePerformed = { action ->
            actionError.canActionBePerformed(action, performActionsUseCase.hasRootPermission)
        }
    )

    //TODO delete
    private val _eventStream = LiveEvent<Event>().apply {
        //vibrate
        addSource(keymapDetectionDelegate.vibrate) {
            value = it
        }

        addSource(fingerprintGestureMapController.vibrateEvent) {
            value = it
        }

        addSource(triggerKeymapByIntentController.vibrateEvent) {
            value = it
        }

        //perform action
        addSource(keymapDetectionDelegate.performAction) {
            value = it
        }

        addSource(fingerprintGestureMapController.performAction) {
            value = it
        }

        addSource(triggerKeymapByIntentController.performAction) {
            value = it
        }

        //show triggered keymap toast
        addSource(keymapDetectionDelegate.showTriggeredKeymapToast) {
            value = it
        }

        addSource(triggerKeymapByIntentController.showTriggeredToastEvent) {
            value = it
        }

        addSource(fingerprintGestureMapController.showTriggeredToastEvent) {
            value = it
        }

    }

    val eventStream: LiveData<Event> = _eventStream

    private val _sendEventToUi = MutableSharedFlow<Event>()
    val sendEventToUi: SharedFlow<Event> = _sendEventToUi.asSharedFlow()

    init {
        requestFingerprintGestureDetection()

        if (onboarding.showFingerprintFeatureNotificationIfAvailable) {

            if (isGestureDetectionAvailable) {
                lifecycleScope.launchWhenStarted {
                    _eventStream.value = ShowFingerprintFeatureNotification
                }
            }
        }

        denyFingerprintGestureDetection()
        onboarding.showedFingerprintFeatureNotificationIfAvailable()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            checkFingerprintGesturesAvailability()

            observeFingerprintMaps()
        }

        lifecycleScope.launchWhenStarted {
            val keymapList = withContext(Dispatchers.Default) {
                keymapRepository.getKeymaps()
            }

            withContext(Dispatchers.Main) {
                updateKeymapListCache(keymapList)
            }
        }

        subscribeToPreferenceChanges()
    }

    fun onKeyEvent(
        keyCode: Int,
        action: Int,
        deviceName: String,
        descriptor: String,
        isExternal: Boolean,
        metaState: Int,
        deviceId: Int,
        scanCode: Int = 0
    ): Boolean {

        if (recordingTrigger) {
            if (action == KeyEvent.ACTION_DOWN) {
                lifecycleScope.launchWhenStarted {
                    _sendEventToUi.emit(
                        RecordedTriggerKeyEvent(
                            keyCode,
                            deviceName,
                            descriptor,
                            isExternal
                        )
                    )
                }
            }

            return true
        }

        if (!getKeymapsPaused().firstBlocking()) {
            try {
                val consume = keymapDetectionDelegate.onKeyEvent(
                    keyCode,
                    action,
                    descriptor,
                    isExternal,
                    metaState,
                    deviceId,
                    scanCode
                )

                return consume

            } catch (e: Exception) {
                Timber.e(e)
            }
        }

        return false
    }

    fun onFingerprintGesture(sdkGestureId: Int) {
        fingerprintGestureMapController.onGesture(sdkGestureId)
    }

    fun onScreenOn() {
        getEventDelegate.stopListening()
    }

    fun onScreenOff() {
        val hasRootPermission = performActionsUseCase.hasRootPermission

        if (hasRootPermission && screenOffTriggersEnabled) {
            if (!getEventDelegate.startListening(lifecycleScope)) {
                toast(R.string.error_failed_execute_getevent)
            }
        }
    }

    fun triggerKeymapByIntent(uid: String) {
        triggerKeymapByIntentController.onDetected(uid)
    }

    fun startRecordingTrigger() {
        //don't start recording if a trigger is being recorded
        if (!recordingTrigger) {
            recordingTriggerJob = recordTriggerJob()
        }
    }

    fun stopRecordingTrigger() {
        val wasRecordingTrigger = recordingTrigger

        recordingTriggerJob?.cancel()
        recordingTriggerJob = null

        if (wasRecordingTrigger) {
            lifecycleScope.launchWhenStarted {
                _sendEventToUi.emit(OnStoppedRecordingTrigger)
            }

            _eventStream.value = OnStoppedRecordingTrigger
        }
    }

    fun updateKeymapListCache(keymapList: List<KeyMapEntity>) {
        keymapDetectionDelegate.keymapListCache = keymapList

        screenOffTriggersEnabled = keymapList.any { keymap ->
            keymap.trigger.flags.hasFlag(TriggerEntity.TRIGGER_FLAG_SCREEN_OFF_TRIGGERS)
        }

        triggerKeymapByIntentController.onKeymapListUpdate(keymapList)
    }

    fun testAction(action: ActionEntity) {
        _eventStream.value = PerformAction(action)
    }

    fun onReceiveEvent(event: Event) {
        when (event) {
            is StartRecordingTrigger -> startRecordingTrigger()
            is StopRecordingTrigger -> stopRecordingTrigger()
        }
    }

    private fun recordTriggerJob() = lifecycleScope.launchWhenStarted {
        repeat(RECORD_TRIGGER_TIMER_LENGTH) { iteration ->
            if (isActive) {
                val timeLeft = RECORD_TRIGGER_TIMER_LENGTH - iteration
                _sendEventToUi.emit(OnIncrementRecordTriggerTimer(timeLeft))
                _eventStream.value = OnIncrementRecordTriggerTimer(timeLeft)

                delay(1000)
            }
        }

        _eventStream.value = OnStoppedRecordingTrigger
        _sendEventToUi.emit(OnStoppedRecordingTrigger)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun observeFingerprintMaps() {
        fingerprintMapRepository.fingerprintGestureMaps.collectWhenStarted(this, { maps ->
            fingerprintGestureMapController.fingerprintMaps = maps

            invalidateFingerprintGestureDetection()
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun invalidateFingerprintGestureDetection() {
        fingerprintGestureMapController.fingerprintMaps.let { maps ->
            if (maps.any { it.value.isEnabled && it.value.actionList.isNotEmpty() }
                && !getKeymapsPaused().firstBlocking()) {
                requestFingerprintGestureDetection()
            } else {
                denyFingerprintGestureDetection()
            }
        }
    }

    private fun checkFingerprintGesturesAvailability() {
        requestFingerprintGestureDetection()

        //this is important
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /* Don't update whether fingerprint gesture detection is supported if it has
            * been supported at some point. Just in case the fingerprint reader is being
            * used while this is called. */
            if (fingerprintMapRepository.fingerprintGesturesAvailable.firstBlocking() != true) {
                fingerprintMapRepository
                    .setFingerprintGesturesAvailable(isGestureDetectionAvailable)
            }
        }

        denyFingerprintGestureDetection()
    }

    private fun getKeymapDetectionDelegatePreferences() = KeymapDetectionPreferences(
        detectKeymapsUseCase.defaultLongPressDelay.firstBlocking(),
        detectKeymapsUseCase.defaultDoublePressDelay.firstBlocking(),
        detectKeymapsUseCase.defaultRepeatDelay.firstBlocking(),
        detectKeymapsUseCase.defaultRepeatRate.firstBlocking(),
        detectKeymapsUseCase.defaultSequenceTriggerTimeout.firstBlocking(),
        detectKeymapsUseCase.defaultVibrateDuration.firstBlocking(),
        detectKeymapsUseCase.defaultHoldDownDuration.firstBlocking(),
        detectKeymapsUseCase.forceVibrate.firstBlocking()
    )

    private fun subscribeToPreferenceChanges() {
        detectKeymapsUseCase.defaultLongPressDelay.collectWhenStarted(this) {
            keymapDetectionDelegate.preferences.defaultLongPressDelay = it
        }

        detectKeymapsUseCase.defaultDoublePressDelay.collectWhenStarted(this) {
            keymapDetectionDelegate.preferences.defaultDoublePressDelay = it
        }

        detectKeymapsUseCase.defaultRepeatDelay.collectWhenStarted(this) {
            keymapDetectionDelegate.preferences.defaultRepeatDelay = it
        }

        detectKeymapsUseCase.defaultRepeatRate.collectWhenStarted(this) {
            keymapDetectionDelegate.preferences.defaultRepeatRate = it
        }

        detectKeymapsUseCase.defaultSequenceTriggerTimeout.collectWhenStarted(this) {
            keymapDetectionDelegate.preferences.defaultSequenceTriggerTimeout = it
        }

        detectKeymapsUseCase.defaultVibrateDuration.collectWhenStarted(this) {
            keymapDetectionDelegate.preferences.defaultVibrateDuration = it
        }

        detectKeymapsUseCase.defaultHoldDownDuration.collectWhenStarted(this) {
            keymapDetectionDelegate.preferences.defaultHoldDownDuration = it
        }

        detectKeymapsUseCase.forceVibrate.collectWhenStarted(this) {
            keymapDetectionDelegate.preferences.forceVibrate = it
        }

        getKeymapsPaused().collectWhenStarted(this) { paused ->
            keymapDetectionDelegate.reset()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (paused) {
                    denyFingerprintGestureDetection()
                } else {
                    requestFingerprintGestureDetection()
                }
            }
        }
    }
}