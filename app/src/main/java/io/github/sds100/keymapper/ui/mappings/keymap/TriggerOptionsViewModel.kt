package io.github.sds100.keymapper.ui.mappings.keymap

import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.data.model.SliderModel
import io.github.sds100.keymapper.domain.mappings.keymap.ConfigKeyMapUseCase
import io.github.sds100.keymapper.domain.mappings.keymap.KeyMap
import io.github.sds100.keymapper.domain.preferences.PreferenceMinimums
import io.github.sds100.keymapper.domain.usecases.OnboardingUseCase
import io.github.sds100.keymapper.domain.utils.Defaultable
import io.github.sds100.keymapper.domain.utils.State
import io.github.sds100.keymapper.domain.utils.dataOrNull
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.ui.*
import io.github.sds100.keymapper.ui.dialogs.DialogUi
import io.github.sds100.keymapper.ui.shortcuts.CreateKeyMapShortcutUseCase
import io.github.sds100.keymapper.util.UserResponse
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Created by sds100 on 29/11/20.
 */
class TriggerOptionsViewModel(
    private val coroutineScope: CoroutineScope,
    private val onboarding: OnboardingUseCase,
    private val config: ConfigKeyMapUseCase,
    private val createKeyMapShortcut: CreateKeyMapShortcutUseCase,
    resourceProvider: ResourceProvider
) : ResourceProvider by resourceProvider, DialogViewModel by DialogViewModelImpl() {

    private companion object {
        const val ID_LONG_PRESS_DELAY = "long_press_delay"
        const val ID_DOUBLE_PRESS_DELAY = "double_press_delay"
        const val ID_SEQUENCE_TRIGGER_TIMEOUT = "sequence_trigger_timeout"
        const val ID_VIBRATE_DURATION = "vibrate_duration"
        const val ID_VIBRATE = "vibrate"
        const val ID_LONG_PRESS_DOUBLE_VIBRATION = "long_press_double_vibration"
        const val ID_SCREEN_OFF_TRIGGER = "screen_off_trigger"
        const val ID_TRIGGER_FROM_OTHER_APPS = "trigger_from_other_apps"
        const val ID_SHOW_TOAST = "show_toast"

        const val KEY_SCREEN_OFF_TRIGGERS = "screen_off_triggers"
    }

    //TODO screen off triggers explanation

    private val _state = MutableStateFlow(buildUiState(State.Loading))
    val state = _state.asStateFlow()

    private val rebuildUiState = MutableSharedFlow<Unit>()

    private var createLauncherShortcutJob: Job? = null

    init {
        coroutineScope.launch {
            combine(rebuildUiState, config.mapping) { _, configState ->
                configState
            }.collectLatest {
                _state.value = withContext(Dispatchers.Default) {
                    buildUiState(it)
                }
            }
        }
    }

    fun setSliderValue(id: String, value: Defaultable<Int>) {
        when (id) {
            ID_VIBRATE_DURATION -> config.setVibrationDuration(value)
            ID_LONG_PRESS_DELAY -> config.setLongPressDelay(value)
            ID_DOUBLE_PRESS_DELAY -> config.setDoublePressDelay(value)
            ID_SEQUENCE_TRIGGER_TIMEOUT -> config.setSequenceTriggerTimeout(value)
        }
    }

    fun setCheckboxValue(id: String, value: Boolean) {
        when (id) {
            ID_VIBRATE -> config.setVibrateEnabled(value)
            ID_TRIGGER_FROM_OTHER_APPS -> config.setTriggerFromOtherAppsEnabled(value)
            ID_LONG_PRESS_DOUBLE_VIBRATION -> config.setLongPressDoubleVibrationEnabled(value)
            ID_SHOW_TOAST -> config.setShowToastEnabled(value)
            ID_SCREEN_OFF_TRIGGER -> config.setTriggerWhenScreenOff(value)
        }
    }

    //TODO replace with dialogviewmodel
    fun onDialogResponse(key: String, response: UserResponse) {
        when {
            key == KEY_SCREEN_OFF_TRIGGERS && response == UserResponse.POSITIVE ->
                onboarding.shownScreenOffTriggersExplanation = true
        }
    }

    fun createLauncherShortcut() {
        createLauncherShortcutJob?.cancel()
        createLauncherShortcutJob = coroutineScope.launch {
            val mapping = config.mapping.firstOrNull()?.dataOrNull() ?: return@launch
            val keyMapUid = mapping.uid

            if (mapping.actionList.size == 1) {
                createKeyMapShortcut.createForSingleAction(keyMapUid, mapping.actionList[0])
            } else {

                //TODO test this
                val key = "create_launcher_shortcut"
                val response = showDialog(key, DialogUi.Text(getString(R.string.hint_shortcut_name), allowEmpty = false))?: return@launch

                createKeyMapShortcut.createForMultipleActions(
                    keyMapUid = keyMapUid,
                    shortcutLabel = response.text
                )
            }
        }
    }

    fun rebuildUiState() {
        runBlocking { rebuildUiState.emit(Unit) }
    }

    private fun buildUiState(configState: State<KeyMap>): ListUiState<ListItem> {
        return when (configState) {
            is State.Data -> sequence {
                val trigger = configState.data.trigger
                val keyMapUid = configState.data.uid

                yield(
                    TriggerFromOtherAppsListItem(
                        id = ID_TRIGGER_FROM_OTHER_APPS,
                        isEnabled = trigger.triggerFromOtherApps,
                        keyMapUid = keyMapUid,
                        label = getString(R.string.flag_trigger_from_other_apps),
                        showCreateLauncherShortcutButton = createKeyMapShortcut.isSupported
                    )
                )

                yield(
                    CheckBoxListItem(
                        id = ID_SHOW_TOAST,
                        isChecked = trigger.showToast,
                        label = getString(R.string.flag_show_toast)
                    )
                )

                if (trigger.isDetectingWhenScreenOffAllowed()) {
                    yield(
                        CheckBoxListItem(
                            id = ID_SCREEN_OFF_TRIGGER,
                            isChecked = trigger.screenOffTrigger,
                            label = getString(R.string.flag_detect_triggers_screen_off)
                        )
                    )
                }

                if (trigger.isVibrateAllowed()) {
                    yield(
                        CheckBoxListItem(
                            id = ID_VIBRATE,
                            isChecked = trigger.vibrate,
                            label = getString(R.string.flag_vibrate)
                        )
                    )
                }

                if (trigger.isLongPressDoubleVibrationAllowed()) {
                    yield(
                        CheckBoxListItem(
                            id = ID_LONG_PRESS_DOUBLE_VIBRATION,
                            isChecked = trigger.longPressDoubleVibration,
                            label = getString(R.string.flag_long_press_double_vibration)
                        )
                    )
                }

                if (trigger.isChangingVibrationDurationAllowed()) {
                    yield(
                        SliderListItem(
                            id = ID_VIBRATE_DURATION,
                            label = getString(R.string.extra_label_vibration_duration),
                            SliderModel(
                                value = trigger.vibrateDuration,
                                isDefaultStepEnabled = true,
                                min = PreferenceMinimums.VIBRATION_DURATION_MIN,
                                max = 1000,
                                stepSize = 5,
                            )
                        )
                    )
                }

                if (trigger.isChangingLongPressDelayAllowed()) {
                    yield(
                        SliderListItem(
                            id = ID_LONG_PRESS_DELAY,
                            label = getString(R.string.extra_label_long_press_delay_timeout),
                            SliderModel(
                                value = trigger.longPressDelay,
                                isDefaultStepEnabled = true,
                                min = PreferenceMinimums.LONG_PRESS_DELAY_MIN,
                                max = 5000,
                                stepSize = 5,
                            )
                        )
                    )
                }

                if (trigger.isChangingDoublePressDelayAllowed()) {
                    yield(
                        SliderListItem(
                            id = ID_DOUBLE_PRESS_DELAY,
                            label = getString(R.string.extra_label_double_press_delay_timeout),
                            SliderModel(
                                value = trigger.doublePressDelay,
                                isDefaultStepEnabled = true,
                                min = PreferenceMinimums.DOUBLE_PRESS_DELAY_MIN,
                                max = 5000,
                                stepSize = 5,
                            )
                        )
                    )
                }

                if (trigger.isChangingSequenceTriggerTimeoutAllowed()) {
                    yield(
                        SliderListItem(
                            id = ID_SEQUENCE_TRIGGER_TIMEOUT,
                            label = getString(R.string.extra_label_sequence_trigger_timeout),
                            SliderModel(
                                value = trigger.sequenceTriggerTimeout,
                                isDefaultStepEnabled = true,
                                min = PreferenceMinimums.SEQUENCE_TRIGGER_TIMEOUT_MIN,
                                max = 5000,
                                stepSize = 5,
                            )
                        )
                    )
                }
            }.toList().createListState()

            is State.Loading -> ListUiState.Loading
        }
    }
}