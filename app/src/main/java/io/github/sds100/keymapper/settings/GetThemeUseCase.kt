package io.github.sds100.keymapper.settings

import io.github.sds100.keymapper.data.Keys
import io.github.sds100.keymapper.data.repositories.PreferenceRepository
import kotlinx.coroutines.flow.map

/**
 * Created by sds100 on 14/02/21.
 */

//TODO delete move to keymapperapp
class GetThemeUseCase(
    preferenceRepository: PreferenceRepository
) : PreferenceRepository by preferenceRepository {

    private val themeMode =
        preferenceRepository.get(Keys.darkTheme).map { it?.toInt() ?: ThemeUtils.AUTO }

    operator fun invoke() = themeMode
}