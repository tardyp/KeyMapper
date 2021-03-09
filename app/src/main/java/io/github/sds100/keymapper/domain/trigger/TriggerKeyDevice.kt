package io.github.sds100.keymapper.domain.trigger

/**
 * Created by sds100 on 21/02/2021.
 */

sealed class TriggerKeyDevice {
    object Internal : TriggerKeyDevice()
    object Any : TriggerKeyDevice()
    data class External(val descriptor: String, val name:String) : TriggerKeyDevice()
}