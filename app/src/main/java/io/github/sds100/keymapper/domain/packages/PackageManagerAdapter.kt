package io.github.sds100.keymapper.domain.packages

import io.github.sds100.keymapper.domain.utils.State
import kotlinx.coroutines.flow.StateFlow

/**
 * Created by sds100 on 26/02/2021.
 */
interface PackageManagerAdapter {
    val installedPackages: StateFlow<State<List<PackageInfo>>>
    fun isAppEnabled(packageName: String): Boolean
    fun isAppInstalled(packageName: String): Boolean

    fun isVoiceAssistantInstalled(): Boolean
}