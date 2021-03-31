package io.github.sds100.keymapper.util

import android.content.Context
import android.content.res.ColorStateList
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.BindingAdapter
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputLayout
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.data.model.*
import io.github.sds100.keymapper.ui.ChipUi
import io.github.sds100.keymapper.ui.callback.OnChipClickCallback
import io.github.sds100.keymapper.ui.view.SquareImageButton
import io.github.sds100.keymapper.ui.view.StatusLayout
import io.noties.markwon.Markwon
import kotlinx.android.synthetic.main.list_item_status.view.*

/**
 * Created by sds100 on 25/01/2020.
 */

@BindingAdapter(
    "app:statusLayoutState",
    "app:fixedText",
    "app:warningText",
    "app:errorText",
    "app:showFixButton",
    "app:onFixClick",
    requireAll = false
)
fun StatusLayout.setStatusLayoutState(
    state: StatusLayout.State,
    fixedText: String?,
    warningText: String? = null,
    errorText: String?,
    showFixButton: Boolean = false,
    onFixClick: View.OnClickListener?
) {
    button.isVisible = state != StatusLayout.State.POSITIVE && showFixButton

    button.setOnClickListener(onFixClick)

    when (state) {
        StatusLayout.State.POSITIVE -> {
            textView.text = fixedText
        }

        StatusLayout.State.WARN -> {
            textView.text = warningText

            val color = color(R.color.warn)
            button.setBackgroundColor(color)
        }

        StatusLayout.State.ERROR -> {
            textView.text = errorText

            val color = styledColor(R.attr.colorError)
            button.setBackgroundColor(color)
        }
    }

    val drawable = when (state) {
        StatusLayout.State.POSITIVE -> context.drawable(R.drawable.ic_outline_check_circle_outline_24)
        StatusLayout.State.WARN -> context.drawable(R.drawable.ic_baseline_error_outline_24)
        StatusLayout.State.ERROR -> context.drawable(R.drawable.ic_baseline_error_outline_24)
    }

    val tint = when (state) {
        StatusLayout.State.POSITIVE -> color(R.color.green)
        StatusLayout.State.WARN -> color(R.color.warn)
        StatusLayout.State.ERROR -> styledColor(R.attr.colorError)
    }

    TextViewCompat.setCompoundDrawableTintList(textView, ColorStateList.valueOf(tint))
    textView.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable, null, null, null)
}

@BindingAdapter("app:onTextChanged")
fun EditText.onTextChangedListener(textWatcher: TextWatcher) {
    addTextChangedListener(textWatcher)
}

@BindingAdapter("app:markdown")
fun TextView.markdown(markdown: OldDataState<String>) {
    when (markdown) {
        is Data -> Markwon.create(context).apply {
            setMarkdown(this@markdown, markdown.data)
        }

        is Loading, is Empty -> text = ""
    }
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

@BindingAdapter("app:errorText")
fun TextInputLayout.errorText(text: String?) {
    error = text
}

@BindingAdapter("app:onChangeListener")
fun SeekBar.setOnChangeListener(onChangeListener: SeekBar.OnSeekBarChangeListener) {
    setOnSeekBarChangeListener(onChangeListener)
}

@BindingAdapter("app:seekBarEnabled")
fun Slider.enabled(enabled: Boolean) {
    isEnabled = enabled
}

@BindingAdapter("app:customBackgroundTint")
fun MaterialButton.backgroundTint(@ColorInt color: Int) {
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