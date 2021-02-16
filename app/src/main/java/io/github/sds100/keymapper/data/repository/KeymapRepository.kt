package io.github.sds100.keymapper.data.repository

import io.github.sds100.keymapper.data.usecase.ConfigKeymapUseCase
import io.github.sds100.keymapper.data.usecase.KeymapListUseCase

/**
 * Created by sds100 on 13/02/21.
 */
interface KeymapRepository : KeymapListUseCase, ConfigKeymapUseCase