package io.github.sds100.keymapper.framework.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/**
 * Created by sds100 on 21/02/2021.
 */

class ResourceProviderImpl(context: Context): ResourceProvider{
    override fun getString(resId: Int, args: Array<Any>): String {
        TODO("Not yet implemented")
    }

    override fun getString(resId: Int, arg: Any): String {
        TODO("Not yet implemented")
    }

    override fun getString(resId: Int): String {
        TODO("Not yet implemented")
    }

    override fun getDrawable(resId: Int): Drawable {
        TODO("Not yet implemented")
    }

}

interface ResourceProvider {
    fun getString(@StringRes resId: Int, args: Array<Any>): String
    fun getString(@StringRes resId: Int, arg: Any): String
    fun getString(@StringRes resId: Int): String
    fun getDrawable(@DrawableRes resId: Int): Drawable
}