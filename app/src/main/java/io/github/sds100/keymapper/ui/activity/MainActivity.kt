package io.github.sds100.keymapper.ui.activity

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.addRepeatingJob
import androidx.navigation.findNavController
import io.github.sds100.keymapper.*
import io.github.sds100.keymapper.Constants.PACKAGE_NAME
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.data.viewmodel.KeyActionTypeViewModel
import io.github.sds100.keymapper.databinding.ActivityMainBinding
import io.github.sds100.keymapper.permissions.Permission
import io.github.sds100.keymapper.permissions.RequestPermissionDelegate
import io.github.sds100.keymapper.util.*
import io.github.sds100.keymapper.util.result.Error
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.flow.collectLatest
import splitties.alertdialog.appcompat.*
import splitties.snackbar.snack
import splitties.toast.toast

/**
 * Created by sds100 on 19/02/2020.
 */

class MainActivity : AppCompatActivity() {

    companion object {
        const val KEY_SHOW_ACCESSIBILITY_SETTINGS_NOT_FOUND_DIALOG =
            "$PACKAGE_NAME.show_accessibility_settings_not_found_dialog"
    }

    private val keyActionTypeViewModel: KeyActionTypeViewModel by viewModels {
        InjectorUtils.provideKeyActionTypeViewModel()
    }

    private lateinit var requestPermissionDelegate: RequestPermissionDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)

        if (intent.getBooleanExtra(KEY_SHOW_ACCESSIBILITY_SETTINGS_NOT_FOUND_DIALOG, false)) {
            alertDialog {
                titleResource = R.string.dialog_title_cant_find_accessibility_settings_page
                messageResource = R.string.dialog_message_cant_find_accessibility_settings_page

                okButton {
                    ServiceLocator.permissionAdapter(this@MainActivity)
                        .request(Permission.WRITE_SECURE_SETTINGS)
                }

                show()
            }
        }

        requestPermissionDelegate = RequestPermissionDelegate(this)

        addRepeatingJob(Lifecycle.State.RESUMED) {
            ServiceLocator.permissionAdapter(this@MainActivity).request.collectLatest { permission ->
                requestPermissionDelegate.requestPermission(
                    permission,
                    findNavController(R.id.container)
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()

        ServiceLocator.notificationController(this).invalidateNotifications()
    }

    override fun onDestroy() {
        ServiceLocator.release()

        super.onDestroy()
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        event?.let { keyActionTypeViewModel.onKeyDown(it.keyCode) }

        return super.onKeyUp(keyCode, event)
    }

    //TODO replace this
    private fun showFileAccessDeniedSnackBar() {
        coordinatorLayout.snack(R.string.error_file_access_denied_automatic_backup).apply {
            setAction(R.string.reset) {
                container.findNavController().navigate(R.id.action_global_settingsFragment)
            }

            show()
        }
    }
}