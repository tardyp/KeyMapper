package io.github.sds100.keymapper.system.inputmethod

import io.github.sds100.keymapper.data.Keys
import io.github.sds100.keymapper.data.repositories.PreferenceRepository
import io.github.sds100.keymapper.mappings.GetKeymapsPausedUseCase
import io.github.sds100.keymapper.domain.utils.PrefDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Created by sds100 on 14/02/2021.
 */

//TODO rename as AutoSwitchImeController and remove interface
class ControlKeyboardOnToggleKeymapsUseCaseImpl(
    inputMethodAdapter: InputMethodAdapter,
    private val preferenceRepository: PreferenceRepository
) : PreferenceRepository by preferenceRepository, ControlKeyboardOnToggleKeymapsUseCase {

    private val imeManager = KeyMapperImeHelper(inputMethodAdapter)
    private val getKeymapsPausedUseCase = GetKeymapsPausedUseCase(preferenceRepository)

    private val keymapsPaused = getKeymapsPausedUseCase()

    private val toggleKeyboardOnToggleKeymaps by PrefDelegate(
        Keys.toggleKeyboardOnToggleKeymaps,
        false
    )

    override fun start(coroutineScope: CoroutineScope) {
        keymapsPaused.onEach { paused ->

            if (!toggleKeyboardOnToggleKeymaps) return@onEach

            if (paused) {
                imeManager.chooseLastUsedIncompatibleInputMethod(fromForeground = false)
            } else {
                imeManager.chooseCompatibleInputMethod(fromForeground = false)
            }
        }.launchIn(coroutineScope)
    }
}

interface ControlKeyboardOnToggleKeymapsUseCase {
    fun start(coroutineScope: CoroutineScope)
}