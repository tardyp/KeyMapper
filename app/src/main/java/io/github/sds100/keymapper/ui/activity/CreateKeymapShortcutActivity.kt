package io.github.sds100.keymapper.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.addRepeatingJob
import androidx.navigation.findNavController
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.ServiceLocator
import io.github.sds100.keymapper.databinding.ActivityCreateKeymapShortcutBinding
import io.github.sds100.keymapper.permissions.RequestPermissionDelegate
import kotlinx.coroutines.flow.collectLatest

/**
 * Created by sds100 on 08/09/20.
 */
//TODO test whether moving this to a different package affects old shortcut intents
class CreateKeymapShortcutActivity : AppCompatActivity() {
    private lateinit var requestPermissionDelegate: RequestPermissionDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DataBindingUtil.setContentView<ActivityCreateKeymapShortcutBinding>(
            this,
            R.layout.activity_create_keymap_shortcut
        )

        requestPermissionDelegate = RequestPermissionDelegate(this)

        addRepeatingJob(Lifecycle.State.RESUMED) {
            ServiceLocator.permissionAdapter(this@CreateKeymapShortcutActivity).request
                .collectLatest { permission ->
                    requestPermissionDelegate.requestPermission(
                        permission,
                        findNavController(R.id.container)
                    )
                }
        }
    }
}