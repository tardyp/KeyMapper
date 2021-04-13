package io.github.sds100.keymapper.domain.packages

import android.graphics.drawable.Drawable
import io.github.sds100.keymapper.domain.utils.State
import io.github.sds100.keymapper.util.result.Result
import kotlinx.coroutines.flow.StateFlow

/**
 * Created by sds100 on 26/02/2021.
 */
interface PackageManagerAdapter {
    val installedPackages: StateFlow<State<List<PackageInfo>>>

    fun getAppName(packageName: String): Result<String>
    fun getAppIcon(packageName: String): Result<Drawable>
    fun isAppEnabled(packageName: String): Boolean
    fun isAppInstalled(packageName: String): Boolean

    fun enableApp(packageName: String)
    fun installApp(packageName: String)

    fun isVoiceAssistantInstalled(): Boolean
}