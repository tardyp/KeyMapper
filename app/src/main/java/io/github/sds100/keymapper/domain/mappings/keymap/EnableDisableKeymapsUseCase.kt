package io.github.sds100.keymapper.domain.mappings.keymap

/**
 * Created by sds100 on 20/03/2021.
 */

class EnableDisableKeymapsUseCaseImpl(
    private val repository: KeymapRepository,
) : EnableDisableKeymapsUseCase {
    override fun enable(vararg id: Long) {
        repository.enableById(*id)
    }

    override fun disable(vararg id: Long) {
        repository.disableById(*id)
    }

    override fun enableAll() {
        repository.enableAll()
    }

    override fun disableAll() {
        repository.disableAll()
    }
}

interface EnableDisableKeymapsUseCase {
    fun enable(vararg id: Long)
    fun disable(vararg id: Long)

    fun enableAll()
    fun disableAll()
}