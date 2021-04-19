package io.github.sds100.keymapper.domain.mappings.fingerprintmap

import io.github.sds100.keymapper.mappings.fingerprintmaps.FingerprintMapRepository

/**
 * Created by sds100 on 20/03/2021.
 */

class EnableDisableFingerprintMapsUseCaseImpl(
    private val repository: FingerprintMapRepository,
) : EnableDisableFingerprintMapsUseCase {
    override fun enable(id: FingerprintMapId) {
        TODO("Not yet implemented")
    }

    override fun disable(id: FingerprintMapId) {
        TODO("Not yet implemented")
    }

    override fun enableAll() {
        TODO("Not yet implemented")
    }

    override fun disableAll() {
        TODO("Not yet implemented")
    }
}

interface EnableDisableFingerprintMapsUseCase {
    fun enable( id: FingerprintMapId)
    fun disable(id: FingerprintMapId)

    fun enableAll()
    fun disableAll()
}