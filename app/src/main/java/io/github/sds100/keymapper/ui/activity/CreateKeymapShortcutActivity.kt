package io.github.sds100.keymapper.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.databinding.ActivityCreateKeymapShortcutBinding

/**
 * Created by sds100 on 08/09/20.
 */
//TODO test whether moving this to a different package affects old shortcut intents
class CreateKeymapShortcutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DataBindingUtil.setContentView<ActivityCreateKeymapShortcutBinding>(
            this,
            R.layout.activity_create_keymap_shortcut
        )
    }
}