package io.github.sds100.keymapper.domain.mappings.keymap

/**
 * Created by sds100 on 07/03/2021.
 */

class ConfigTriggerKeyUseCaseImpl : ConfigTriggerKeyUseCase {
    override fun setTriggerKey(key: TriggerKey) {
        TODO("Not yet implemented")
    }

    override fun getTriggerKey(key: TriggerKey) {
        TODO("Not yet implemented")
    }
}

interface ConfigTriggerKeyUseCase {
    fun setTriggerKey(key: TriggerKey)
    fun getTriggerKey(key: TriggerKey)
}