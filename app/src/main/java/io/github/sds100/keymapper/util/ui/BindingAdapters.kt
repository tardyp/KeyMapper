package io.github.sds100.keymapper.util.ui

import android.content.Context
import android.content.res.ColorStateList
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.widget.addTextChangedListener
import androidx.databinding.BindingAdapter
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputLayout
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.system.url.UrlUtils
import io.github.sds100.keymapper.util.str
import io.github.sds100.keymapper.util.styledColor
import io.github.sds100.keymapper.util.styledColorSL

/**
 * Created by sds100 on 25/01/2020.
 */

@BindingAdapter("app:onTextChanged")
fun EditText.onTextChangedListener(textWatcher: TextWatcher) {
    addTextChangedListener(textWatcher)
}

@BindingAdapter("app:tintType")
fun AppCompatImageView.tintType(tintType: TintType?) {
    tintType?.toColor(context)?.let { setColorFilter(it) } ?: clearColorFilter()
}

@BindingAdapter("app:errorWhenEmpty")
fun TextInputLayout.errorWhenEmpty(enabled: Boolean) {

    //need to set it up when the view is created
    if (editText?.text.isNullOrBlank()) {
        error = if (enabled) {
            str(R.string.error_cant_be_empty)
        } else {
            null
        }
    }

    editText?.addTextChangedListener {
        error = if (it.isNullOrBlank() && enabled) {
            str(R.string.error_cant_be_empty)
        } else {
            null
        }
    }
}

@BindingAdapter("app:onLongClick")
fun setLongClickListener(view: View, onLongClickListener: View.OnLongClickListener?) {
    view.setOnLongClickListener(onLongClickListener)
}

@BindingAdapter("app:seekBarEnabled")
fun Slider.enabled(enabled: Boolean) {
    isEnabled = enabled
}

@BindingAdapter("app:customBackgroundTint")
fun View.backgroundTint(@ColorInt color: Int) {
    backgroundTintList = ColorStateList.valueOf(color)
}

@BindingAdapter("app:openUrlOnClick")
fun Button.openUrlOnClick(url: String?) {
    url ?: return

    setOnClickListener {
        UrlUtils.openUrl(context, url)
    }
}

@BindingAdapter("app:openUrlOnClick")
fun SquareImageButton.openUrlOnClick(url: Int?) {
    url ?: return

    setOnClickListener {
        UrlUtils.openUrl(context, context.str(url))
    }
}

@BindingAdapter("app:chipUiModels", "app:onChipClickCallback", requireAll = true)
fun ChipGroup.setChipUiModels(
    models: List<ChipUi>,
    callback: OnChipClickCallback
) {
    removeAllViews()

    val colorTintError by lazy { styledColorSL(R.attr.colorError) }
    val colorOnSurface by lazy { styledColorSL(R.attr.colorOnSurface) }

    models.forEach { model ->
        when (model) {

            is ChipUi.Error -> {
                MaterialButton(context, null, R.attr.errorChipButtonStyle).apply {
                    id = View.generateViewId()

                    text = model.text
                    setOnClickListener { callback.onChipClick(model) }
                    addView(this)
                }
            }

            is ChipUi.Normal -> {
                MaterialButton(context, null, R.attr.normalChipButtonStyle).apply {
                    id = View.generateViewId()

                    this.text = model.text
                    this.icon = model.icon?.drawable

                    if (model.icon != null) {
                        this.iconTint = when (model.icon.tintType) {
                            TintType.NONE -> null
                            TintType.ON_SURFACE -> colorOnSurface
                            TintType.ERROR -> colorTintError
                        }
                    }

                    addView(this)
                }
            }

            is ChipUi.Transparent -> {
                MaterialButton(context, null, R.attr.transparentChipButtonStyle).apply {
                    id = View.generateViewId()

                    text = model.text
                    addView(this)
                }
            }
        }
    }
}

fun TintType.toColor(ctx: Context): Int? =
    when (this) {
        TintType.NONE -> null
        TintType.ON_SURFACE -> ctx.styledColor(R.attr.colorOnSurface)
        TintType.ERROR -> ctx.styledColor(R.attr.colorError)
    }