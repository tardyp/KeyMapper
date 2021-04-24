package io.github.sds100.keymapper.system.keyevents

import android.view.KeyEvent
import io.github.sds100.keymapper.system.devices.DevicesAdapter
import io.github.sds100.keymapper.system.devices.InputDeviceInfo
import io.github.sds100.keymapper.system.root.SuAdapter
import io.github.sds100.keymapper.util.valueOrNull
import kotlinx.coroutines.*
import timber.log.Timber

/**
 * Created by sds100 on 21/06/2020.
 */
class GetEventDelegate(
    private val suAdapter: SuAdapter,
    private val devicesAdapter: DevicesAdapter,
    private val onKeyEvent: suspend (
        keyCode: Int,
        action: Int,
        deviceDescriptor: String,
        isExternal: Boolean,
        deviceId: Int
    ) -> Unit
) {

    companion object {
        private const val REGEX_GET_DEVICE_LOCATION = "/.*(?=:)"
        private const val REGEX_KEY_EVENT_ACTION = "(?<= )(DOWN|UP)"

        fun canDetectKeyWhenScreenOff(keyCode: Int): Boolean {
            return KeyEventUtils.GET_EVENT_LABEL_TO_KEYCODE.containsValue(keyCode)
        }
    }

    private var job: Job? = null

    /**
     * @return whether it successfully started listening.
     */
    fun startListening(scope: CoroutineScope): Boolean {
        try {
            job = scope.launch(Dispatchers.IO) {

                val devicesInputStream =
                    suAdapter.getCommandOutput("getevent -i").valueOrNull() ?: return@launch

                val getEventDevices: String = devicesInputStream.bufferedReader().readText()
                devicesInputStream.close()

                val deviceLocationToDeviceMap = mutableMapOf<String, InputDeviceInfo>()

                val inputDevices = devicesAdapter.inputDevices.value

                inputDevices.forEach { device ->
                    val deviceLocation =
                        getDeviceLocation(getEventDevices, device.name) ?: return@forEach

                    deviceLocationToDeviceMap[deviceLocation] = device
                }

                val getEventLabels = KeyEventUtils.GET_EVENT_LABEL_TO_KEYCODE.keys

                val deviceLocationRegex = Regex(REGEX_GET_DEVICE_LOCATION)
                val actionRegex = Regex(REGEX_KEY_EVENT_ACTION)

                //use -q option to not initially output the list of devices
                val inputStream =
                    suAdapter.getCommandOutput("getevent -lq").valueOrNull() ?: return@launch

                var line: String?

                while (inputStream.bufferedReader().readLine()
                        .also { line = it } != null && isActive
                ) {
                    line ?: continue

                    getEventLabels.forEach { label ->
                        if (line?.contains(label) == true) {
                            val keyCode = KeyEventUtils.GET_EVENT_LABEL_TO_KEYCODE[label]
                                ?: return@forEach

                            val deviceLocation =
                                deviceLocationRegex.find(line!!)?.value ?: return@forEach

                            val device = deviceLocationToDeviceMap[deviceLocation] ?: return@forEach

                            val actionString = actionRegex.find(line!!)?.value ?: return@forEach

                            when (actionString) {
                                "UP" -> {
                                    onKeyEvent.invoke(
                                        keyCode,
                                        KeyEvent.ACTION_UP,
                                        device.descriptor,
                                        device.isExternal,
                                        0
                                    )
                                }

                                "DOWN" -> {
                                    onKeyEvent.invoke(
                                        keyCode,
                                        KeyEvent.ACTION_DOWN,
                                        device.descriptor,
                                        device.isExternal,
                                        0
                                    )
                                }
                            }

                            return@forEach
                        }
                    }
                }

                inputStream.close()
            }

        } catch (e: Exception) {
            Timber.e(e)
            job?.cancel()
            return false
        }

        return true
    }

    fun stopListening() {
        job?.cancel()
        job = null
    }

    private fun getDeviceLocation(getEventDeviceOutput: String, deviceName: String): String? {
        val regex = Regex("(/.*)(?=(\\n.*){5}\"$deviceName\")")
        return regex.find(getEventDeviceOutput)?.value
    }
}