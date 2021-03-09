package io.github.sds100.keymapper.data.model.options

import io.github.sds100.keymapper.data.model.TriggerEntity
import io.github.sds100.keymapper.data.model.options.BoolOption.Companion.saveBoolOption
import kotlinx.android.parcel.Parcelize
import splitties.bitflags.hasFlag

@Parcelize
class TriggerKeyOptions(
    override val id: String,
    val clickType: IntOption,
    private val doNotConsumeKeyEvents: BoolOption
) : BaseOptions<TriggerEntity> {

    companion object {
        const val ID_DO_NOT_CONSUME_KEY_EVENT = "do_not_consume_key_event"
        const val ID_CLICK_TYPE = "click_type"
    }

    constructor(key: TriggerEntity.KeyEntity, @TriggerEntity.Mode mode: Int) : this(
        id = key.uid,

        clickType = IntOption(
            id = ID_CLICK_TYPE,
            value = key.clickType,
            isAllowed = mode == TriggerEntity.SEQUENCE || mode == TriggerEntity.UNDEFINED
        ),

        doNotConsumeKeyEvents = BoolOption(
            id = ID_DO_NOT_CONSUME_KEY_EVENT,
            value = key.flags.hasFlag(TriggerEntity.KeyEntity.FLAG_DO_NOT_CONSUME_KEY_EVENT),
            isAllowed = true
        )
    )

    override fun setValue(id: String, value: Boolean): TriggerKeyOptions {
        when (id) {
            ID_DO_NOT_CONSUME_KEY_EVENT -> doNotConsumeKeyEvents.value = value
        }

        return this
    }

    override fun setValue(id: String, value: Int): TriggerKeyOptions {
        when (id) {
            ID_CLICK_TYPE -> clickType.value = value
        }

        return this
    }

    override val intOptions: List<IntOption>
        get() = listOf()

    override val boolOptions: List<BoolOption>
        get() = listOf(doNotConsumeKeyEvents)

    override fun apply(trigger: TriggerEntity): TriggerEntity {

        val newTriggerKeys = trigger.keys
            .toMutableList()
            .map {
                if (it.uid == id) {
                    return@map it.copy(
                        clickType = clickType.value,
                        flags = it.flags.saveBoolOption(
                            doNotConsumeKeyEvents,
                            TriggerEntity.KeyEntity.FLAG_DO_NOT_CONSUME_KEY_EVENT
                        )
                    )
                }

                it
            }

        return trigger.copy(keys = newTriggerKeys)
    }
}