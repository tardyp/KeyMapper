package io.github.sds100.keymapper.domain.mappings.fingerprintmap

import io.github.sds100.keymapper.domain.constraints.ConfigConstraintUseCaseImpl
import io.github.sds100.keymapper.domain.constraints.ConfigConstraintsState
import io.github.sds100.keymapper.domain.utils.State
import io.github.sds100.keymapper.domain.utils.ifIsData
import io.github.sds100.keymapper.domain.utils.mapData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Created by sds100 on 16/02/2021.
 */
class ConfigFingerprintMapUseCaseImpl : ConfigFingerprintMapUseCase {

    private val fingerprintMap = MutableStateFlow<State<FingerprintMap>>(State.Loading)

    override val state =
        fingerprintMap.map { state -> state.mapData { ConfigFingerprintMapState(it.isEnabled) } }

    val configActions = ConfigFingerprintMapActionsUseCaseImpl(fingerprintMap, ::setFingerprintMap)
    val configConstraints = ConfigConstraintUseCaseImpl(
        state = fingerprintMap.map { state ->
            state.mapData {
                ConfigConstraintsState(
                    it.constraintList,
                    it.constraintMode
                )
            }
        },
        editState = { block ->
            editFingerprintMap { old ->
                val newConstraintState = block.invoke(
                    ConfigConstraintsState(
                        old.constraintList,
                        old.constraintMode
                    )
                )

                old.copy(
                    constraintList = newConstraintState.list,
                    constraintMode = newConstraintState.mode
                )
            }
        }
    )
    override fun setEnabled(enabled: Boolean) = editFingerprintMap { it.copy(isEnabled = enabled) }

    override fun setFingerprintMap(fingerprintMap: FingerprintMap) {
        this.fingerprintMap.value = State.Data(fingerprintMap)
    }

    override fun getFingerprintMap() = fingerprintMap.value

    private fun editFingerprintMap(block: (fingerprintMap: FingerprintMap) -> FingerprintMap) {
        fingerprintMap.value.ifIsData { setFingerprintMap(block.invoke(it)) }
    }
}

interface ConfigFingerprintMapUseCase {
    val state: Flow<State<ConfigFingerprintMapState>>
    fun setEnabled(enabled: Boolean)

    fun setFingerprintMap(fingerprintMap: FingerprintMap)
    fun getFingerprintMap(): State<FingerprintMap>
}

data class ConfigFingerprintMapState(val isEnabled: Boolean)