package io.github.sds100.keymapper.domain.mappings.keymap

/**
 * Created by sds100 on 20/03/2021.
 */

class EnableDisableKeymapsUseCaseImpl(
    private val repository: KeymapRepository,
) : EnableDisableKeymapsUseCase {
    override fun enable(vararg uid: String) {
        repository.enableById(*uid)
    }

    override fun disable(vararg uid: String) {
        repository.disableById(*uid)
    }

    override fun enableAll() {
        repository.enableAll()
    }

    override fun disableAll() {
        repository.disableAll()
    }
}

interface EnableDisableKeymapsUseCase {
    fun enable(vararg uid: String)
    fun disable(vararg uid: String)

    fun enableAll()
    fun disableAll()
}