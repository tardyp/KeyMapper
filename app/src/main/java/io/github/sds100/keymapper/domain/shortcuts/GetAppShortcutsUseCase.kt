package io.github.sds100.keymapper.domain.shortcuts

import android.content.Intent
import android.content.pm.PackageManager
import io.github.sds100.keymapper.domain.utils.State
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Created by sds100 on 23/03/2021.
 */

class GetAppShortcutsUseCaseImpl(packageManager: PackageManager) : GetAppShortcutsUseCase {
    override val shortcuts = MutableStateFlow<State<List<AppShortcutInfo>>>(State.Loading)

    init {
        val shortcutIntent = Intent(Intent.ACTION_CREATE_SHORTCUT)

        packageManager.queryIntentActivities(shortcutIntent, 0)
            .map {
                val activityInfo = it.activityInfo
                AppShortcutInfo(
                    packageName = activityInfo.packageName,
                    activityName = activityInfo.name
                )
            }
            .let { shortcuts.value = State.Data(it) }
    }
}

interface GetAppShortcutsUseCase {
    val shortcuts: StateFlow<State<List<AppShortcutInfo>>>
}