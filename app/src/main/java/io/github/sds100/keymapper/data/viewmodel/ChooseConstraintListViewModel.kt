package io.github.sds100.keymapper.data.viewmodel

import androidx.lifecycle.*
import com.hadilq.liveevent.LiveEvent
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.data.model.ChooseConstraintListItemModel
import io.github.sds100.keymapper.data.model.ConstraintEntity
import io.github.sds100.keymapper.data.model.ConstraintType
import io.github.sds100.keymapper.util.*
import io.github.sds100.keymapper.util.delegate.ModelState

/**
 * Created by sds100 on 21/03/2020.
 */

class ChooseConstraintListViewModel(
    private val supportedConstraints: List<String>
) : ViewModel(), ModelState<Map<Int, List<ChooseConstraintListItemModel>>> {

    companion object {
        private val ALL_MODELS = listOf(
            ChooseConstraintListItemModel(
                ConstraintEntity.APP_FOREGROUND,
                ConstraintEntity.CATEGORY_APP,
                R.string.constraint_choose_app_foreground),

            ChooseConstraintListItemModel(
                ConstraintEntity.APP_NOT_FOREGROUND,
                ConstraintEntity.CATEGORY_APP,
                R.string.constraint_choose_app_not_foreground
            ),

            ChooseConstraintListItemModel(
                ConstraintEntity.APP_PLAYING_MEDIA,
                ConstraintEntity.CATEGORY_APP,
                R.string.constraint_choose_app_playing_media
            ),

            ChooseConstraintListItemModel(
                ConstraintEntity.BT_DEVICE_CONNECTED,
                ConstraintEntity.CATEGORY_BLUETOOTH,
                R.string.constraint_choose_bluetooth_device_connected
            ),
            ChooseConstraintListItemModel(
                ConstraintEntity.BT_DEVICE_DISCONNECTED,
                ConstraintEntity.CATEGORY_BLUETOOTH,
                R.string.constraint_choose_bluetooth_device_disconnected
            ),

            ChooseConstraintListItemModel(
                ConstraintEntity.SCREEN_ON,
                ConstraintEntity.CATEGORY_SCREEN,
                R.string.constraint_choose_screen_on_description
            ),
            ChooseConstraintListItemModel(
                ConstraintEntity.SCREEN_OFF,
                ConstraintEntity.CATEGORY_SCREEN,
                R.string.constraint_choose_screen_off_description
            ),
            ChooseConstraintListItemModel(
                ConstraintEntity.ORIENTATION_PORTRAIT,
                ConstraintEntity.CATEGORY_ORIENTATION,
                R.string.constraint_choose_orientation_portrait
            ),
            ChooseConstraintListItemModel(
                ConstraintEntity.ORIENTATION_LANDSCAPE,
                ConstraintEntity.CATEGORY_ORIENTATION,
                R.string.constraint_choose_orientation_landscape
            ),
            ChooseConstraintListItemModel(
                ConstraintEntity.ORIENTATION_0,
                ConstraintEntity.CATEGORY_ORIENTATION,
                R.string.constraint_choose_orientation_0
            ),
            ChooseConstraintListItemModel(
                ConstraintEntity.ORIENTATION_90,
                ConstraintEntity.CATEGORY_ORIENTATION,
                R.string.constraint_choose_orientation_90
            ),
            ChooseConstraintListItemModel(
                ConstraintEntity.ORIENTATION_180,
                ConstraintEntity.CATEGORY_ORIENTATION,
                R.string.constraint_choose_orientation_180
            ),
            ChooseConstraintListItemModel(
                ConstraintEntity.ORIENTATION_270,
                ConstraintEntity.CATEGORY_ORIENTATION,
                R.string.constraint_choose_orientation_270
            ),
        )

        private const val KEY_BT_CONSTRAINT_LIMITATION = "bt_constraint_limitation"
        private const val KEY_SCREEN_OFF_CONSTRAINT_LIMITATION = "bt_constraint_screen_off_limitation"
        private const val KEY_SCREEN_ON_CONSTRAINT_LIMITATION = "bt_constraint_screen_on_limitation"
    }

    override val model = liveData {
        emit(Loading())

        emit(
            sequence {
                for ((id, label) in ConstraintEntity.CATEGORY_LABEL_MAP) {
                    val constraints = ALL_MODELS.filter {
                        it.categoryId == id && supportedConstraints.contains(it.id)
                    }

                    if (constraints.isNotEmpty()) {
                        yield(label to constraints)
                    }
                }
            }.toMap().getDataState()
        )
    }
    override val viewState = MutableLiveData<ViewState>(ViewLoading())

    private val _eventStream = LiveEvent<Event>()
    val eventStream: LiveData<Event> = _eventStream

    private var chosenConstraintType: String? = null

    fun chooseConstraint(@ConstraintType constraintType: String) {
        chosenConstraintType = constraintType

        when (constraintType) {
            ConstraintEntity.APP_FOREGROUND,
            ConstraintEntity.APP_NOT_FOREGROUND,
            ConstraintEntity.APP_PLAYING_MEDIA,
            -> _eventStream.value = ChoosePackage()

            ConstraintEntity.BT_DEVICE_CONNECTED, ConstraintEntity.BT_DEVICE_DISCONNECTED -> {
                _eventStream.value = OkDialog(KEY_BT_CONSTRAINT_LIMITATION,
                    R.string.dialog_message_bt_constraint_limitation)
            }
            ConstraintEntity.SCREEN_ON -> {
                _eventStream.value = OkDialog(KEY_SCREEN_OFF_CONSTRAINT_LIMITATION,
                    R.string.dialog_message_screen_constraints_limitation)
            }
            ConstraintEntity.SCREEN_OFF -> {
                _eventStream.value = OkDialog(KEY_SCREEN_OFF_CONSTRAINT_LIMITATION,
                    R.string.dialog_message_screen_constraints_limitation)
            }
            else -> {
                _eventStream.value = SelectConstraint(ConstraintEntity(constraintType))
            }
        }
    }

    fun packageChosen(packageName: String) {
        _eventStream.value = SelectConstraint(ConstraintEntity.appConstraint(chosenConstraintType!!, packageName))
        chosenConstraintType = null
    }

    fun bluetoothDeviceChosen(address: String, name: String) {
        _eventStream.value = SelectConstraint(ConstraintEntity.btConstraint(chosenConstraintType!!, address, name))
        chosenConstraintType = null
    }


    fun onDialogResponse(key: String, response: UserResponse) {
        when (key) {
            KEY_BT_CONSTRAINT_LIMITATION ->
                _eventStream.value = ChooseBluetoothDevice()

            KEY_SCREEN_OFF_CONSTRAINT_LIMITATION ->
                _eventStream.value = SelectConstraint(ConstraintEntity(ConstraintEntity.SCREEN_OFF))

            KEY_SCREEN_ON_CONSTRAINT_LIMITATION ->
                _eventStream.value = SelectConstraint(ConstraintEntity(ConstraintEntity.SCREEN_ON))
        }
    }


    @Suppress("UNCHECKED_CAST")
    class Factory(private val supportedConstraints: List<String>) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ChooseConstraintListViewModel(supportedConstraints) as T
        }
    }
}