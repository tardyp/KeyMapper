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
import io.github.sds100.keymapper.data.model.*
import io.github.sds100.keymapper.data.model.options.BaseOptions
import io.github.sds100.keymapper.data.model.options.TriggerKeyOptions
import io.github.sds100.keymapper.util.result.Error
import io.github.sds100.keymapper.util.result.Result
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable

@Serializable
sealed class Event

open class MessageEvent(@StringRes val textRes: Int) : Event()

class FixFailure(val error: Error) : Event()
class VibrateEvent(val duration: Long) : Event()
object ShowTriggeredKeymapToast : Event()
data class PerformAction(
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
class ChooseBluetoothDevice : Event()
class OpenUrl(val url: String) : Event()
class OpenUrlRes(@StringRes val url: Int) : Event()
class CloseDialog : Event()
class SelectScreenshot : Event()
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

sealed class ResultEvent<T> : Event() {
    abstract val result: Result<T>
}

data class BackupResult(override val result: Result<Unit>) : ResultEvent<Unit>()
data class RestoreResult(override val result: Result<Unit>) : ResultEvent<Unit>()
data class AutomaticBackupResult(override val result: Result<Unit>) : ResultEvent<Unit>()

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
object OnHideKeyboard : Event(), UpdateNotificationEvent
object OnShowKeyboard : Event(), UpdateNotificationEvent

//trigger

class EditTriggerKeyOptions(val options: TriggerKeyOptions) : Event()

//action list

class EditActionOptions(val options: BaseOptions<ActionEntity>) : Event()

//constraints
class DuplicateConstraints : MessageEvent(R.string.error_duplicate_constraint)
class SelectConstraint(val constraint: ConstraintEntity) : Event()

//fingerprint gesture maps
class BuildFingerprintMapModels(
    val maps: Map<String, FingerprintMapEntity>,
    val deviceInfoList: List<DeviceInfoEntity>,
    val hasRootPermission: Boolean,
    val showDeviceDescriptors: Boolean
) : Event()

class BackupFingerprintMaps : Event()
class RequestFingerprintMapReset : Event()

//menu
class OpenSettings : Event()
class OpenAbout : Event()
class ChooseKeyboard : Event()
class SendFeedback : Event()
class EnableAccessibilityService : Event()

//notifications
object ShowFingerprintFeatureNotification : Event(), UpdateNotificationEvent
object DismissFingerprintFeatureNotification : Event(), UpdateNotificationEvent
class DismissNotification(val id: Int) : Event(), UpdateNotificationEvent
interface UpdateNotificationEvent

//home
object ShowWhatsNewEvent : Event()