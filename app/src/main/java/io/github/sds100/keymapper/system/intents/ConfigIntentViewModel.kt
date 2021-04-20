package io.github.sds100.keymapper.system.intents

import android.content.Intent
import android.text.InputType
import androidx.core.net.toUri
import androidx.lifecycle.*
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.util.*
import io.github.sds100.keymapper.util.ui.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Created by sds100 on 01/01/21.
 */

class ConfigIntentViewModel(resourceProvider: ResourceProvider) : ViewModel(),
    ResourceProvider by resourceProvider, PopupViewModel by PopupViewModelImpl() {

    companion object {
        private val EXTRA_TYPES = arrayOf(
            BoolExtraType(),
            BoolArrayExtraType(),
            IntExtraType(),
            IntArrayExtraType(),
            StringExtraType(),
            StringArrayExtraType(),
            LongExtraType(),
            LongArrayExtraType(),
            ByteExtraType(),
            ByteArrayExtraType(),
            DoubleExtraType(),
            DoubleArrayExtraType(),
            CharExtraType(),
            CharArrayExtraType(),
            FloatExtraType(),
            FloatArrayExtraType(),
            ShortExtraType(),
            ShortArrayExtraType()
        )
    }

    private val target = MutableStateFlow(IntentTarget.BROADCAST_RECEIVER)
    val checkedTarget: StateFlow<Int> = target.map {
        when (it) {
            IntentTarget.ACTIVITY -> R.id.radioButtonTargetActivity
            IntentTarget.BROADCAST_RECEIVER -> R.id.radioButtonTargetBroadcastReceiver
            IntentTarget.SERVICE -> R.id.radioButtonTargetService
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, R.id.radioButtonTargetBroadcastReceiver)

    val description = MutableStateFlow("")
    val action = MutableStateFlow("")
    val categoriesString = MutableStateFlow("")
    val categoriesList = categoriesString.map {
        it.split(',')
    }

    val data = MutableStateFlow("")
    val targetPackage = MutableStateFlow("")
    val targetClass = MutableStateFlow("")

    private val extras = MutableStateFlow(emptyList<IntentExtraModel>())

    val extraListItems: StateFlow<List<IntentExtraListItem>> = extras.map { extras ->
        extras.map { it.toListItem() }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val isValid: Flow<Boolean> = combine(description, extras) { description, extras ->
        if (description.isEmpty()) {
            return@combine false
        }

        if (extras.any { !it.isValidValue || it.name.isEmpty() }) {
            return@combine false
        }

        true
    }

    val isDoneButtonEnabled = isValid.stateIn(
        viewModelScope, SharingStarted.Eagerly, false
    )

    private val _returnResult = MutableSharedFlow<ConfigIntentResult>()
    val returnResult = _returnResult.asSharedFlow()

    fun setActivityTargetChecked(isChecked: Boolean) {
        if (isChecked) {
            target.value = IntentTarget.ACTIVITY
        }
    }

    fun setBroadcastReceiverTargetChecked(isChecked: Boolean) {
        if (isChecked) {
            target.value = IntentTarget.BROADCAST_RECEIVER
        }
    }

    fun setServiceTargetChecked(isChecked: Boolean) {
        if (isChecked) {
            target.value = IntentTarget.SERVICE
        }
    }

    fun onDoneClick() {
        viewModelScope.launch {
            val intent = Intent().apply {
                if (this@ConfigIntentViewModel.action.value.isNotEmpty()) {
                    action = this@ConfigIntentViewModel.action.value
                }

                categoriesList.first().forEach {
                    addCategory(it)
                }

                if (this@ConfigIntentViewModel.data.value.isNotEmpty()) {
                    data = this@ConfigIntentViewModel.data.value.toUri()
                }

                if (this@ConfigIntentViewModel.targetPackage.value.isNotEmpty()) {
                    `package` = this@ConfigIntentViewModel.targetPackage.value

                    if (targetClass.value.isNotEmpty()) {
                        setClassName(targetPackage.value, targetClass.value)
                    }
                }

                this@ConfigIntentViewModel.extras.value.forEach { model ->
                    if (model.name.isEmpty()) return@forEach
                    if (model.parsedValue == null) return@forEach

                    model.type.putInIntent(this, model.name, model.value)
                }
            }

            val uri = intent.toUri(0)

            _returnResult.emit(
                ConfigIntentResult(
                    uri = uri,
                    target = target.value,
                    description = description.value
                )
            )
        }
    }

    fun setExtraName(uid: String, name: String) {
        extras.value = extras.value.map {
            if (it.uid == uid && it.name != name) {
                return@map it.copy(name = name)
            }

            it
        }
    }

    fun setExtraValue(uid: String, value: String) {
        extras.value = extras.value.map {
            if (it.uid == uid && it.value != value) {
                return@map it.copy(value = value)
            }

            it
        }
    }

    fun removeExtra(uid: String) {
        extras.value = extras.value.toMutableList().apply {
            removeAll { it.uid == uid }
        }
    }

    fun onAddExtraClick() {
        viewModelScope.launch {
            val items = EXTRA_TYPES.map { it to getString(it.labelStringRes) }

            val dialog = PopupUi.SingleChoice(items)

            val response = showPopup("add_extra", dialog) ?: return@launch

            val model = IntentExtraModel(response.item)

            extras.value = extras.value.toMutableList().apply {
                add(model)
            }
        }
    }

    fun onShowExtraExampleClick(listItem: IntentExtraListItem){
        viewModelScope.launch {
            if (listItem is GenericIntentExtraListItem) {
                val dialog = PopupUi.Ok(message = listItem.exampleString)
                showPopup("extra_example", dialog)
            }
        }
    }

    fun onShowCategoriesExampleClick() {
        viewModelScope.launch {
            val dialog = PopupUi.Ok(message = getString(R.string.intent_categories_example))
            showPopup("categories_example", dialog)
        }
    }

    private fun IntentExtraModel.toListItem(): IntentExtraListItem {
        return when (type) {
            is BoolExtraType -> BoolIntentExtraListItem(
                uid,
                name,
                parsedValue?.let { it as Boolean } ?: true,
                isValidValue
            )

            else -> {
                val inputType = when (type) {
                    is IntExtraType ->
                        InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED

                    is IntArrayExtraType -> InputType.TYPE_CLASS_NUMBER or
                        InputType.TYPE_NUMBER_FLAG_SIGNED or InputType.TYPE_CLASS_TEXT

                    is LongExtraType ->
                        InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED

                    is LongArrayExtraType -> InputType.TYPE_CLASS_NUMBER or
                        InputType.TYPE_NUMBER_FLAG_SIGNED or InputType.TYPE_CLASS_TEXT

                    is ByteExtraType ->
                        InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED

                    is ByteArrayExtraType -> InputType.TYPE_CLASS_NUMBER or
                        InputType.TYPE_NUMBER_FLAG_SIGNED or InputType.TYPE_CLASS_TEXT

                    is DoubleExtraType ->
                        InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED or
                            InputType.TYPE_NUMBER_FLAG_DECIMAL

                    is DoubleArrayExtraType -> InputType.TYPE_CLASS_NUMBER or
                        InputType.TYPE_NUMBER_FLAG_DECIMAL or
                        InputType.TYPE_NUMBER_FLAG_SIGNED or
                        InputType.TYPE_CLASS_TEXT

                    is FloatExtraType ->
                        InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED or
                            InputType.TYPE_NUMBER_FLAG_DECIMAL

                    is FloatArrayExtraType -> InputType.TYPE_CLASS_NUMBER or
                        InputType.TYPE_NUMBER_FLAG_DECIMAL or
                        InputType.TYPE_NUMBER_FLAG_SIGNED or
                        InputType.TYPE_CLASS_TEXT

                    is ShortExtraType ->
                        InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED

                    is ShortArrayExtraType -> InputType.TYPE_CLASS_NUMBER or
                        InputType.TYPE_NUMBER_FLAG_SIGNED or InputType.TYPE_CLASS_TEXT

                    else -> InputType.TYPE_CLASS_TEXT
                }

                GenericIntentExtraListItem(
                    uid,
                    getString(type.labelStringRes),
                    name,
                    value,
                    isValidValue,
                    getString(type.exampleStringRes),
                    inputType
                )
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val resourceProvider: ResourceProvider
    ) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ConfigIntentViewModel(resourceProvider) as T
        }
    }
}
