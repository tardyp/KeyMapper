package io.github.sds100.keymapper.system.keyevents

import android.view.KeyEvent
import io.github.sds100.keymapper.system.Shell
import io.github.sds100.keymapper.system.devices.DevicesAdapter
import io.github.sds100.keymapper.system.root.RootUtils
import io.github.sds100.keymapper.util.State
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

/**
 * Created by sds100 on 21/06/2020.
 */
class GetEventDelegate(
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

                val getEventDevices: String

                RootUtils.getRootCommandOutput("getevent -i").apply {
                    getEventDevices = bufferedReader().readText()
                    close()
                }

                val deviceLocationToDescriptorMap = mutableMapOf<String, String>()
                val descriptorToIsExternalMap = mutableMapOf<String, Boolean>()

                val inputDevices = devicesAdapter.inputDevices.value

                inputDevices.forEach { device ->
                    val deviceLocation =
                        getDeviceLocation(getEventDevices, device.name) ?: return@forEach
                    deviceLocationToDescriptorMap[deviceLocation] = device.descriptor
                    descriptorToIsExternalMap[device.descriptor] = device.isExternal
                }

                val getEventLabels = KeyEventUtils.GET_EVENT_LABEL_TO_KEYCODE.keys

                val deviceLocationRegex = Regex(REGEX_GET_DEVICE_LOCATION)
                val actionRegex = Regex(REGEX_KEY_EVENT_ACTION)

                //use -q option to not initially output the list of devices
                val inputStream = Shell.getShellCommandStdOut("su", "-c", "getevent -lq")
                var line: String?

                while (inputStream.bufferedReader().readLine()
                        .also { line = it } != null && isActive
                ) {
                    line ?: continue

                    getEventLabels.forEach { label ->
                        if (line?.contains(label) == true) {
                            val keycode = KeyEventUtils.GET_EVENT_LABEL_TO_KEYCODE[label]!!
                            val deviceLocation =
                                deviceLocationRegex.find(line!!)?.value ?: return@forEach
                            val deviceDescriptor =
                                deviceLocationToDescriptorMap[deviceLocation]!!
                            val isExternal = descriptorToIsExternalMap[deviceDescriptor]!!
                            val actionString = actionRegex.find(line!!)?.value ?: return@forEach

                            when (actionString) {
                                "UP" -> {
                                    onKeyEvent.invoke(
                                        keycode,
                                        KeyEvent.ACTION_UP,
                                        deviceDescriptor,
                                        isExternal,
                                        0
                                    )
                                }

                                "DOWN" -> {
                                    onKeyEvent.invoke(
                                        keycode,
                                        KeyEvent.ACTION_DOWN,
                                        deviceDescriptor,
                                        isExternal,
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
            job?.cancel()
            return false
        }

        return true
    }

    fun stopListening() {
        job?.cancel()
    }

    private fun getDeviceLocation(getEventDeviceOutput: String, deviceName: String): String? {
        val regex = Regex("(/.*)(?=(\\n.*){5}\"$deviceName\")")
        return regex.find(getEventDeviceOutput)?.value
    }
}