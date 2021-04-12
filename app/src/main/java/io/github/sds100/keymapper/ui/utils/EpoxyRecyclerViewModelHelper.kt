package io.github.sds100.keymapper.ui.utils

import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.airbnb.epoxy.EpoxyController
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.slider.Slider
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.checkbox
import io.github.sds100.keymapper.domain.utils.Defaultable
import io.github.sds100.keymapper.radioButtonPair
import io.github.sds100.keymapper.slider
import io.github.sds100.keymapper.ui.CheckBoxListItem
import io.github.sds100.keymapper.ui.RadioButtonPairListItem
import io.github.sds100.keymapper.ui.SliderListItem
import io.github.sds100.keymapper.util.editTextNumberAlertDialog
import io.github.sds100.keymapper.util.viewLifecycleScope
import timber.log.Timber

/**
 * Created by sds100 on 20/03/2021.
 */

fun EpoxyController.configuredRadioButtonPair(
    fragment: Fragment,
    model: RadioButtonPairListItem,
    onCheckedChange: (buttonId: String, isChecked: Boolean) -> Unit
) {

    fragment.apply {
        radioButtonPair {
            id(model.id)
            model(model)

            onCheckedChange { group, checkedId ->
                val isChecked = group.checkedRadioButtonId == checkedId

                when(checkedId){
                    R.id.radioButtonLeft -> onCheckedChange(model.leftButtonId, isChecked)
                    R.id.radioButtonRight -> onCheckedChange(model.rightButtonId, isChecked)
                }
            }
        }
    }
}

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

            /*
            Only change the model when the touch has been released because the otherwise jank happens
            because the list is trying to update dozens/100s of times super fast.
             */
            onSliderTouchListener(object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {

                }

                override fun onStopTrackingTouch(slider: Slider) {
                    if (slider.isInTouchMode) {
                        val value = if (slider.value < model.sliderModel.min) {
                            Defaultable.Default
                        } else {
                            Defaultable.Custom(slider.value.toInt())
                        }

                        onValueChanged.invoke(value)
                    }
                }
            })

            onSliderChangeListener { slider, value, fromUser ->
                if (fromUser && !slider.isInTouchMode) {
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
                    ) ?: return@launchWhenResumed

                    onValueChanged.invoke(Defaultable.Custom(newValue))
                }
            }
        }
    }
}