/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.sds100.keymapper.util

import android.os.Parcelable
import androidx.annotation.StringRes
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.data.entities.ActionEntity
import io.github.sds100.keymapper.constraints.ConstraintEntity
import io.github.sds100.keymapper.devices.DeviceInfoEntity
import io.github.sds100.keymapper.domain.actions.ActionData
import io.github.sds100.keymapper.intents.IntentExtraModel
import io.github.sds100.keymapper.mappings.keymaps.KeyMapEntity
import io.github.sds100.keymapper.util.result.Error
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable

//TODO delete unused events
@Serializable
sealed class Event

@Serializable
data class PingService(val key: String) : Event()
@Serializable
data class PingServiceResponse(val key: String) : Event()

open class MessageEvent(@StringRes val textRes: Int) : Event()

class FixFailure(val error: Error) : Event()
class VibrateEvent(val duration: Long) : Event()
object ShowTriggeredKeymapToast : Event()
data class PerformActionEvent(
    val action: ActionEntity,
    val additionalMetaState: Int = 0,
    val keyEventAction: InputEventType = InputEventType.DOWN_UP
) : Event()

data class ImitateButtonPress(
    val keyCode: Int,
    val metaState: Int = 0,
    val deviceId: Int = 0,
    val keyEventAction: InputEventType,
    val scanCode: Int = 0
) : Event()

class ChoosePackage : Event()
class OpenUrl(val url: String) : Event()
class OpenUrlRes(@StringRes val url: Int) : Event()
class CloseDialog : Event()
class ChooseKeycode : Event()

//TODO delete
class BuildDeviceInfoModels : Event()
class RequestBackupSelectedKeymaps : Event()

class BuildKeymapListModels(
    val keymapList: List<KeyMapEntity>,
    val deviceInfoList: List<DeviceInfoEntity>,
    val hasRootPermission: Boolean,
    val showDeviceDescriptors: Boolean
) : Event()

class OkDialog(val responseKey: String, @StringRes val message: Int) : Event()
class EnableAccessibilityServicePrompt : Event()
class BackupRequest<T>(val model: T) : Event()
class RequestRestore : Event()
class RequestBackupAll : Event()
class ShowErrorMessage(val error: Error) : Event()
class BuildIntentExtraListItemModels(val extraModels: List<IntentExtraModel>) : Event()
class CreateKeymapShortcutEvent(
    val uuid: String,
    val actionList: List<ActionEntity>,
    val deviceInfoList: List<DeviceInfoEntity>,
    val showDeviceDescriptors: Boolean
) : Event()

data class SaveEvent<T>(val model: T) : Event()

object OnBootEvent : Event(), UpdateNotificationEvent

@Parcelize
@Serializable
data class RecordedTriggerKeyEvent(
    val keyCode: Int,
    val deviceName: String,
    val deviceDescriptor: String,
    val isExternal: Boolean
) : Event(), Parcelable

@Serializable
object StartRecordingTrigger : Event()

@Serializable
object StopRecordingTrigger : Event()

@Serializable
data class OnIncrementRecordTriggerTimer(val timeLeft: Int) : Event()

@Serializable
object OnStoppedRecordingTrigger : Event()
object OnAccessibilityServiceStarted : Event(), UpdateNotificationEvent
object OnAccessibilityServiceStopped : Event(), UpdateNotificationEvent

@Serializable
object OnHideKeyboardEvent : Event(), UpdateNotificationEvent

@Serializable
object OnShowKeyboardEvent : Event(), UpdateNotificationEvent

@Serializable
object HideKeyboardEvent: Event()

@Serializable
object ShowKeyboardEvent: Event()

//constraints
class DuplicateConstraints : MessageEvent(R.string.error_duplicate_constraint)
class SelectConstraint(val constraint: ConstraintEntity) : Event()

//notifications
class DismissNotification(val id: Int) : Event(), UpdateNotificationEvent

data class TestActionEvent(val action: ActionData): Event()

//TODO delete
interface UpdateNotificationEvent