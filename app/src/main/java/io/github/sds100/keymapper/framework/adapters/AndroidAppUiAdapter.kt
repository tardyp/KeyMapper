package io.github.sds100.keymapper.framework.adapters

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Created by sds100 on 03/03/2021.
 */

class AndroidAppUiAdapter(
    private val packageManager: PackageManager
) : AppUiAdapter {
    override fun getAppName(packageName: String): Flow<String> = flow {
        emit(packageManager.getApplicationInfo(packageName, 0).loadLabel(packageManager).toString())
    }

    override fun getAppIcon(packageName: String): Flow<Drawable> = flow {
        emit(packageManager.getApplicationInfo(packageName, 0).loadIcon(packageManager))
    }
}