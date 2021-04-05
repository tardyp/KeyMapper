package io.github.sds100.keymapper.ui.utils

import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.airbnb.epoxy.EpoxyController
import io.github.sds100.keymapper.checkbox
import io.github.sds100.keymapper.domain.utils.Defaultable
import io.github.sds100.keymapper.slider
import io.github.sds100.keymapper.ui.CheckBoxListItem
import io.github.sds100.keymapper.ui.SliderListItem
import io.github.sds100.keymapper.util.editTextNumberAlertDialog
import io.github.sds100.keymapper.util.viewLifecycleScope
import timber.log.Timber

/**
 * Created by sds100 on 20/03/2021.
 */

fun EpoxyController.configuredCheckBox(
    fragment: Fragment,
    model: CheckBoxListItem,
    onCheckedChange: (checked: Boolean) -> Unit
) {
    fragment.apply {
        checkbox {
            id(model.id)
            isChecked(model.isChecked)
            primaryText(model.label)

            onCheckedChange { buttonView, isChecked ->
                onCheckedChange.invoke(isChecked)
            }
        }
    }
}

fun EpoxyController.configuredSlider(
    fragment: Fragment,
    model: SliderListItem,
    onValueChanged: (newValue: Defaultable<Int>) -> Unit
) {
    fragment.apply {
        slider {
            id(model.id)
            label(model.label)
            model(model.sliderModel)

            onSliderChangeListener { slider, value, fromUser ->
                if (fromUser) {
                    if (value < model.sliderModel.min) {
                        onValueChanged.invoke(Defaultable.Default)
                    } else {
                        onValueChanged.invoke(Defaultable.Custom(value.toInt()))
                    }
                }
            }

            onSliderValueClickListener { _ ->
                viewLifecycleScope.launchWhenResumed {
                    val newValue = requireContext().editTextNumberAlertDialog(
                        viewLifecycleOwner,
                        hint = model.label,
                        min = model.sliderModel.min
                    )?: return@launchWhenResumed

                    onValueChanged.invoke(Defaultable.Custom(newValue))
                }
            }
        }
    }
}