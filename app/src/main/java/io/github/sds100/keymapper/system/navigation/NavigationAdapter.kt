package io.github.sds100.keymapper.system.navigation

import io.github.sds100.keymapper.util.Result

/**
 * Created by sds100 on 20/04/2021.
 */
interface NavigationAdapter {
    fun goBack(): Result<*>
    fun goHome(): Result<*>
    fun openRecents(): Result<*>
    fun openMenu(): Result<*>
}