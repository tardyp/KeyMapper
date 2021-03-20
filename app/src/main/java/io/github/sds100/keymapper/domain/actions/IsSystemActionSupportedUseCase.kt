package io.github.sds100.keymapper.domain.actions

import android.os.Build
import io.github.sds100.keymapper.domain.adapter.SystemFeatureAdapter
import io.github.sds100.keymapper.util.SystemActionUtils
import io.github.sds100.keymapper.util.result.Error

/**
 * Created by sds100 on 16/03/2021.
 */

class IsSystemActionSupportedUseCaseImpl(
   private val adapter: SystemFeatureAdapter
) : IsSystemActionSupportedUseCase {

    override fun invoke(action: SystemAction): Error? {
        val minApi = SystemActionUtils.getMinApi(action)
        if (Build.VERSION.SDK_INT < minApi) {
            return Error.SdkVersionTooLow(minApi)
        }

        val maxApi = SystemActionUtils.getMaxApi(action)
        if (Build.VERSION.SDK_INT > maxApi) {
            return Error.SdkVersionTooHigh(maxApi)
        }

        SystemActionUtils.getRequiredSystemFeatures(action).forEach { feature ->
            if (!adapter.hasSystemFeature(feature)){
                return Error.FeatureUnavailable(feature)
            }
        }

        return null
    }
}

interface IsSystemActionSupportedUseCase {
    operator fun invoke(action: SystemAction): Error?
}