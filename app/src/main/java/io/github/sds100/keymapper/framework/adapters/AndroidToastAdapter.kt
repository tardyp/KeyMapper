package io.github.sds100.keymapper.framework.adapters

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.getSystemService
import io.github.sds100.keymapper.domain.adapter.PopupMessageAdapter
import io.github.sds100.keymapper.domain.adapter.VibratorAdapter
import splitties.toast.toast

/**
 * Created by sds100 on 17/04/2021.
 */
class AndroidToastAdapter(context: Context) : PopupMessageAdapter {
    private val ctx: Context = context.applicationContext

    override fun showPopupMessage(message: String) {
        ctx.toast(message)
    }
}