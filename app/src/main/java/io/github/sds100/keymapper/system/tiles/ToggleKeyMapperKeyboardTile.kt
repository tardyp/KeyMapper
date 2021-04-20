package io.github.sds100.keymapper.system.tiles

import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.ServiceLocator
import io.github.sds100.keymapper.UseCases
import io.github.sds100.keymapper.util.*
import kotlinx.coroutines.flow.first
import splitties.toast.toast

/**
 * Created by sds100 on 12/06/2020.
 */
@RequiresApi(Build.VERSION_CODES.N)
class ToggleKeyMapperKeyboardTile : TileService(), LifecycleOwner {

    private val useCase by lazy { UseCases.toggleCompatibleIme(this) }
    private val resourceProvider by lazy { ServiceLocator.resourceProvider(this) }

    private lateinit var lifecycleRegistry: LifecycleRegistry

    override fun onCreate() {
        super.onCreate()

        lifecycleRegistry = LifecycleRegistry(this)

        lifecycleRegistry.currentState = Lifecycle.State.CREATED

        addRepeatingJob(Lifecycle.State.STARTED) {
            qsTile.icon = Icon.createWithResource(
                this@ToggleKeyMapperKeyboardTile,
                R.drawable.ic_tile_keyboard
            )
            qsTile.label = str(R.string.tile_toggle_keymapper_keyboard)
            qsTile.contentDescription = str(R.string.tile_toggle_keymapper_keyboard)
            qsTile.state = Tile.STATE_INACTIVE

            qsTile?.updateTile()
        }
    }

    override fun onStartListening() {

        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        super.onStartListening()
    }

    override fun onStopListening() {

        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        super.onStopListening()
    }

    override fun onDestroy() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED

        super.onDestroy()
    }

    override fun onClick() {
        super.onClick()

        lifecycleScope.launchWhenStarted {
            if (!useCase.sufficientPermissions.first()) {
                toast(R.string.error_insufficient_permissions)
                return@launchWhenStarted
            }

            useCase.toggle().onSuccess {
                toast(resourceProvider.getString(R.string.toast_chose_keyboard, it.label))

            }.onFailure {
                toast(it.getFullMessage(resourceProvider))
            }
        }
    }

    override fun getLifecycle() = lifecycleRegistry
}