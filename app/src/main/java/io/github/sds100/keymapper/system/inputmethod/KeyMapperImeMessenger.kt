package io.github.sds100.keymapper.system.inputmethod

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.view.KeyEvent
import io.github.sds100.keymapper.system.keyevents.InputKeyModel
import io.github.sds100.keymapper.util.InputEventType
import io.github.sds100.keymapper.util.Result
import io.github.sds100.keymapper.util.firstBlocking

/**
 * Created by sds100 on 21/04/2021.
 */

class KeyMapperImeMessengerImpl(
    context: Context,
    private val inputMethodAdapter: InputMethodAdapter
) : KeyMapperImeMessenger {

    companion object {
        //DON'T CHANGE THESE!!!
        private const val KEY_MAPPER_INPUT_METHOD_ACTION_INPUT_DOWN_UP =
            "io.github.sds100.keymapper.system.inputmethod.ACTION_INPUT_DOWN_UP"
        private const val KEY_MAPPER_INPUT_METHOD_ACTION_INPUT_DOWN =
            "io.github.sds100.keymapper.system.inputmethod.ACTION_INPUT_DOWN"
        private const val KEY_MAPPER_INPUT_METHOD_ACTION_INPUT_UP =
            "io.github.sds100.keymapper.system.inputmethod.ACTION_INPUT_UP"
        private const val KEY_MAPPER_INPUT_METHOD_ACTION_TEXT =
            "io.github.sds100.keymapper.system.inputmethod.ACTION_INPUT_TEXT"

        private const val KEY_MAPPER_INPUT_METHOD_EXTRA_KEY_EVENT =
            "io.github.sds100.keymapper.system.inputmethod.EXTRA_KEY_EVENT"
        private const val KEY_MAPPER_INPUT_METHOD_EXTRA_TEXT =
            "io.github.sds100.keymapper.system.inputmethod.EXTRA_TEXT"
    }

    private val ctx = context.applicationContext

    private val keyMapperImeHelper = KeyMapperImeHelper(inputMethodAdapter)

    override fun inputKeyEvent(model: InputKeyModel) {

        val imePackageName = inputMethodAdapter.chosenIme.firstBlocking().packageName

        val intentAction = when (model.inputType) {
            InputEventType.DOWN -> KEY_MAPPER_INPUT_METHOD_ACTION_INPUT_DOWN
            InputEventType.DOWN_UP -> KEY_MAPPER_INPUT_METHOD_ACTION_INPUT_DOWN_UP
            InputEventType.UP -> KEY_MAPPER_INPUT_METHOD_ACTION_INPUT_UP
        }

        Intent(intentAction).apply {
            setPackage(imePackageName)

            val action = when (model.inputType) {
                InputEventType.DOWN, InputEventType.DOWN_UP -> KeyEvent.ACTION_DOWN
                InputEventType.UP -> KeyEvent.ACTION_UP
            }

            val eventTime = SystemClock.uptimeMillis()

            val keyEvent = KeyEvent(
                eventTime,
                eventTime,
                action,
                model.keyCode,
                model.repeat,
                model.metaState,
                model.deviceId,
                model.scanCode
            )

            putExtra(KEY_MAPPER_INPUT_METHOD_EXTRA_KEY_EVENT, keyEvent)

            ctx.sendBroadcast(this)
        }
    }

    override fun inputText(text: String) {
        val imePackageName = inputMethodAdapter.chosenIme.firstBlocking().packageName

        Intent(KEY_MAPPER_INPUT_METHOD_ACTION_TEXT).apply {
            setPackage(imePackageName)

            putExtra(KEY_MAPPER_INPUT_METHOD_EXTRA_TEXT, text)
            ctx.sendBroadcast(this)
        }
    }
}

interface KeyMapperImeMessenger {
    fun inputKeyEvent(model: InputKeyModel)
    fun inputText(text: String)
}