package io.github.sds100.keymapper.domain.usecases

import io.github.sds100.keymapper.domain.adapter.InputMethodAdapter
import io.github.sds100.keymapper.domain.ime.KeyMapperImeHelper
import io.github.sds100.keymapper.domain.preferences.Keys
import io.github.sds100.keymapper.domain.repositories.PreferenceRepository
import io.github.sds100.keymapper.domain.utils.PrefDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Created by sds100 on 14/02/2021.
 */
//TODO rename as delegate. doesnt need to be an interface
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