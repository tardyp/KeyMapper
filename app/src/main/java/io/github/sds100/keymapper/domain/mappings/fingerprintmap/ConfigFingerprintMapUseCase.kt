package io.github.sds100.keymapper.domain.mappings.fingerprintmap

import io.github.sds100.keymapper.domain.constraints.ConfigConstraintUseCaseImpl
import io.github.sds100.keymapper.util.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Created by sds100 on 16/02/2021.
 */
class ConfigFingerprintMapUseCaseImpl : ConfigFingerprintMapUseCase {

    private val fingerprintMap = MutableStateFlow<DataState<FingerprintMap>>(Loading())

    override val state =
        fingerprintMap.map { state -> state.mapData { ConfigFingerprintMapState(it.isEnabled) } }

    val configActions = ConfigFingerprintMapActionsUseCaseImpl(fingerprintMap, ::setFingerprintMap)
    val configConstraints = ConfigConstraintUseCaseImpl()

    override fun setEnabled(enabled: Boolean) = editFingerprintMap { it.copy(isEnabled = enabled) }

    override fun setFingerprintMap(fingerprintMap: FingerprintMap) {
        this.fingerprintMap.value = Data(fingerprintMap)
    }

    override fun getFingerprintMap() = fingerprintMap.value

    private fun editFingerprintMap(block: (fingerprintMap: FingerprintMap) -> FingerprintMap) {
        fingerprintMap.value.ifIsData { setFingerprintMap(block.invoke(it)) }
    }
}

interface ConfigFingerprintMapUseCase {
    val state: Flow<DataState<ConfigFingerprintMapState>>
    fun setEnabled(enabled: Boolean)

    fun setFingerprintMap(fingerprintMap: FingerprintMap)
    fun getFingerprintMap(): DataState<FingerprintMap>
}

data class ConfigFingerprintMapState(val isEnabled: Boolean)