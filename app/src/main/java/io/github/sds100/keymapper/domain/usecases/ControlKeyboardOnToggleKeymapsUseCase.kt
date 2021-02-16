package io.github.sds100.keymapper.domain.usecases

import io.github.sds100.keymapper.domain.adapter.KeyboardAdapter
import io.github.sds100.keymapper.domain.preferences.Keys
import io.github.sds100.keymapper.domain.repositories.PreferenceRepository
import io.github.sds100.keymapper.domain.utils.PrefDelegate
import io.github.sds100.keymapper.util.collectIn
import kotlinx.coroutines.CoroutineScope

/**
 * Created by sds100 on 14/02/2021.
 */
class ControlKeyboardOnToggleKeymapsUseCaseImpl(
    private val keyboardAdapter: KeyboardAdapter,
    private val preferenceRepository: PreferenceRepository
) : PreferenceRepository by preferenceRepository, ControlKeyboardOnToggleKeymapsUseCase {

    private val getKeymapsPausedUseCase = GetKeymapsPausedUseCase(preferenceRepository)

    private val keymapsPaused = getKeymapsPausedUseCase()

    private val toggleKeyboardOnToggleKeymaps by PrefDelegate(
        Keys.toggleKeyboardOnToggleKeymaps,
        false
    )

    override fun start(coroutineScope: CoroutineScope) {
        keymapsPaused.collectIn(coroutineScope) { paused ->

            if (!toggleKeyboardOnToggleKeymaps) return@collectIn

            if (paused) {
                keyboardAdapter.chooseLastUsedIncompatibleInputMethod()
            } else {
                keyboardAdapter.chooseCompatibleInputMethod()
            }
        }
    }
}

interface ControlKeyboardOnToggleKeymapsUseCase {
    fun start(coroutineScope: CoroutineScope)
}