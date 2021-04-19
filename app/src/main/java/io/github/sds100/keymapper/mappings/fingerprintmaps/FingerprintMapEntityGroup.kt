package io.github.sds100.keymapper.mappings.fingerprintmaps

import io.github.sds100.keymapper.data.model.FingerprintMapEntity
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.FingerprintMap
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.FingerprintMapId

/**
 * Created by sds100 on 04/04/2021.
 */
data class FingerprintMapEntityGroup(val swipeDown: FingerprintMapEntity = FingerprintMapEntity(),
                                     val swipeUp: FingerprintMapEntity= FingerprintMapEntity(),
                                     val swipeLeft: FingerprintMapEntity= FingerprintMapEntity(),
                                     val swipeRight: FingerprintMapEntity= FingerprintMapEntity())