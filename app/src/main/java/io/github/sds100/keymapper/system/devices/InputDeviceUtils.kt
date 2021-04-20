package io.github.sds100.keymapper.system.devices

import android.os.Build
import android.view.InputDevice
import io.github.sds100.keymapper.system.devices.DeviceInfoEntity
import io.github.sds100.keymapper.util.result.Error
import io.github.sds100.keymapper.util.result.Result
import io.github.sds100.keymapper.util.result.Success
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * Created by sds100 on 24/04/2020.
 */

object InputDeviceUtils {
    fun getName(descriptor: String): Result<String> {
        InputDevice.getDeviceIds().forEach {
            val device = InputDevice.getDevice(it)

            if (device.descriptor == descriptor) {
                return Success(device.name)
            }
        }

        return Error.DeviceNotFound(descriptor)
    }

    fun createDeviceInfoModelsForAll() = sequence {
        InputDevice.getDeviceIds().forEach {
            val device = InputDevice.getDevice(it)

            yield(DeviceInfoEntity(device.descriptor, device.name))
        }
    }.toList()

    fun getDeviceNames() = sequence {
        InputDevice.getDeviceIds().forEach {
            val device = InputDevice.getDevice(it)

            yield(device.name)
        }
    }.toList()

    fun getAllDeviceDescriptors() = sequence {
        InputDevice.getDeviceIds().forEach {
            val device = InputDevice.getDevice(it)
            yield(device.descriptor)
        }
    }

    fun getExternalDeviceDescriptors() = sequence {
        InputDevice.getDeviceIds().forEach {
            val device = InputDevice.getDevice(it)

            if (device.isExternalCompat) {
                yield(device.descriptor)
            }
        }
    }

    fun getExternalDeviceNames() = sequence {
        InputDevice.getDeviceIds().forEach {
            val device = InputDevice.getDevice(it)

            if (device.isExternalCompat) {
                yield(device.name)
            }
        }
    }

    fun getDeviceIdFromDescriptor(descriptor: String) =
        InputDevice.getDeviceIds().find {
            val device = InputDevice.getDevice(it)
            device.descriptor == descriptor
        }
}

val InputDevice.isExternalCompat: Boolean
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        isExternal
    } else {
        try {
            val m: Method = InputDevice::class.java.getMethod("isExternal")
            (m.invoke(this) as Boolean)
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
            false
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
            false
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
            false
        }
    }