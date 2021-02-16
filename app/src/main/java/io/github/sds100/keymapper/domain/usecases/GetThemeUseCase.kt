package io.github.sds100.keymapper.domain.usecases

import io.github.sds100.keymapper.domain.preferences.Keys
import io.github.sds100.keymapper.domain.repositories.PreferenceRepository
import io.github.sds100.keymapper.domain.utils.ThemeUtils
import kotlinx.coroutines.flow.map

/**
 * Created by sds100 on 14/02/21.
 */
class GetThemeUseCase(
    preferenceRepository: PreferenceRepository
) : PreferenceRepository by preferenceRepository {

    private val themeMode =
        preferenceRepository.get(Keys.darkTheme).map { it?.toInt() ?: ThemeUtils.AUTO }

    operator fun invoke() = themeMode
}