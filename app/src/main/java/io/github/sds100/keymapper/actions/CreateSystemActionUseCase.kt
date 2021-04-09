package io.github.sds100.keymapper.actions

import android.os.Build
import io.github.sds100.keymapper.domain.actions.SystemActionId
import io.github.sds100.keymapper.domain.adapter.InputMethodAdapter
import io.github.sds100.keymapper.domain.adapter.SystemFeatureAdapter
import io.github.sds100.keymapper.domain.ime.ImeInfo
import io.github.sds100.keymapper.domain.packages.PackageInfo
import io.github.sds100.keymapper.domain.packages.PackageManagerAdapter
import io.github.sds100.keymapper.domain.utils.dataOrNull
import io.github.sds100.keymapper.util.SystemActionUtils
import io.github.sds100.keymapper.util.result.Error
import io.github.sds100.keymapper.util.result.Result
import kotlinx.coroutines.flow.first

/**
 * Created by sds100 on 03/04/2021.
 */
class CreateSystemActionUseCaseImpl(
    private val systemFeatureAdapter: SystemFeatureAdapter,
    private val packageManagerAdapter: PackageManagerAdapter,
    private val inputMethodAdapter: InputMethodAdapter

) : CreateSystemActionUseCase {
    override fun isSupported(id: SystemActionId): Error? {
        val minApi = SystemActionUtils.getMinApi(id)

        if (Build.VERSION.SDK_INT < minApi) {
            return Error.SdkVersionTooLow(minApi)
        }

        val maxApi = SystemActionUtils.getMaxApi(id)
        if (Build.VERSION.SDK_INT > maxApi) {
            return Error.SdkVersionTooHigh(maxApi)
        }

        SystemActionUtils.getRequiredSystemFeatures(id).forEach { feature ->
            if (!systemFeatureAdapter.hasSystemFeature(feature)) {
                return Error.FeatureUnavailable(feature)
            }
        }

        return null
    }

    override fun getInstalledPackages(): List<PackageInfo> {
        return packageManagerAdapter.installedPackages.value.dataOrNull() ?: emptyList()
    }

    override fun getAppName(packageName: String): Result<String> {
        return packageManagerAdapter.getAppName(packageName)
    }

    override suspend fun getInputMethods(): List<ImeInfo> {
        return inputMethodAdapter.enabledInputMethods.first()
    }
}

interface CreateSystemActionUseCase {
    fun isSupported(id: SystemActionId): Error?

    fun getInstalledPackages(): List<PackageInfo>
    fun getAppName(packageName: String): Result<String>
   suspend fun getInputMethods(): List<ImeInfo>
}