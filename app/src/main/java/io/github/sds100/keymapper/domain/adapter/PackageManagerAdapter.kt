package io.github.sds100.keymapper.domain.adapter

/**
 * Created by sds100 on 26/02/2021.
 */
interface PackageManagerAdapter {
    fun isAppEnabled(packageName: String)
    fun isAppInstalled(packageName: String)
}