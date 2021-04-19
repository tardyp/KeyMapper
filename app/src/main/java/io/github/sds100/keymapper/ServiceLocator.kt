package io.github.sds100.keymapper

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.datastore.preferences.createDataStore
import androidx.room.Room
import io.github.sds100.keymapper.mappings.keymaps.db.KeyMapDatabase
import io.github.sds100.keymapper.data.repositories.DataStorePreferenceRepository
import io.github.sds100.keymapper.data.repositories.DataStoreFingerprintMapRepository
import io.github.sds100.keymapper.data.repositories.RoomDeviceInfoCache
import io.github.sds100.keymapper.data.repositories.RoomKeyMapRepository
import io.github.sds100.keymapper.devices.DeviceInfoCache
import io.github.sds100.keymapper.domain.BackupManager
import io.github.sds100.keymapper.domain.BackupManagerImpl
import io.github.sds100.keymapper.domain.adapter.*
import io.github.sds100.keymapper.domain.packages.PackageManagerAdapter
import io.github.sds100.keymapper.domain.repositories.PreferenceRepository
import io.github.sds100.keymapper.files.FileAdapter
import io.github.sds100.keymapper.framework.adapters.AccessibilityServiceAdapter
import io.github.sds100.keymapper.framework.adapters.AndroidPermissionAdapter
import io.github.sds100.keymapper.domain.adapter.AppShortcutAdapter
import io.github.sds100.keymapper.framework.adapters.FileRepository
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.mappings.fingerprintmaps.FingerprintMapRepository
import io.github.sds100.keymapper.notifications.AndroidNotificationAdapter
import io.github.sds100.keymapper.ui.NotificationController
import kotlinx.coroutines.runBlocking

/**
 * Created by sds100 on 17/05/2020.
 */
object ServiceLocator {

    private val lock = Any()
    private var database: KeyMapDatabase? = null

    @Volatile
    private var deviceInfoRepository: DeviceInfoCache? = null

    fun deviceInfoRepository(context: Context): DeviceInfoCache {
        synchronized(this) {
            return deviceInfoRepository ?: createDeviceInfoRepository(context)
        }
    }

    @Volatile
    private var roomKeymapRepository: RoomKeyMapRepository? = null

    fun roomKeymapRepository(context: Context): RoomKeyMapRepository {
        synchronized(this) {
            val dataBase = database ?: createDatabase(context.applicationContext)

            return roomKeymapRepository ?: RoomKeyMapRepository(
                dataBase.keymapDao(),
                (context.applicationContext as KeyMapperApp).appCoroutineScope
            ).also {
                this.roomKeymapRepository = it
            }
        }
    }

    private fun createDeviceInfoRepository(context: Context): DeviceInfoCache {
        val database = database ?: createDatabase(context.applicationContext)
        deviceInfoRepository = RoomDeviceInfoCache(
            database.deviceInfoDao(),
            (context.applicationContext as KeyMapperApp).appCoroutineScope
        )
        return deviceInfoRepository!!
    }

    @Volatile
    private var fingerprintMapRepository: FingerprintMapRepository? = null

    fun fingerprintMapRepository(context: Context): FingerprintMapRepository {
        synchronized(this) {
            return fingerprintMapRepository ?: createFingerprintMapRepository(context)
        }
    }

    private fun createFingerprintMapRepository(context: Context): FingerprintMapRepository {
        val dataStore = context.createDataStore("fingerprint_gestures")
        val scope = (context.applicationContext as KeyMapperApp).appCoroutineScope

        return fingerprintMapRepository
            ?: DataStoreFingerprintMapRepository(dataStore, scope).also {
                this.fingerprintMapRepository = it
            }
    }

    @Volatile
    private var fileRepository: FileRepository? = null

    fun fileRepository(context: Context): FileRepository {
        synchronized(this) {
            return fileRepository ?: createFileRepository(context)
        }
    }

    private fun createFileRepository(context: Context): FileRepository {
        return fileRepository
            ?: FileRepository(context.applicationContext).also {
                this.fileRepository = it
            }
    }

    @Volatile
    private var preferenceRepository: PreferenceRepository? = null

    fun preferenceRepository(context: Context): PreferenceRepository {
        synchronized(this) {
            return preferenceRepository ?: createPreferenceRepository(context)
        }
    }

    private fun createPreferenceRepository(context: Context): PreferenceRepository {

        return preferenceRepository
            ?: DataStorePreferenceRepository(
                context.applicationContext,
                (context.applicationContext as KeyMapperApp).appCoroutineScope
            ).also {
                this.preferenceRepository = it
            }
    }

    @Volatile
    private var backupManager: BackupManager? = null

    fun backupManager(context: Context): BackupManager {
        synchronized(this) {
            return backupManager ?: createBackupManager(context)
        }
    }

    private fun createBackupManager(context: Context): BackupManager {
        return backupManager ?: BackupManagerImpl(
            (context.applicationContext as KeyMapperApp).appCoroutineScope,
            fileAdapter(context),
            roomKeymapRepository(context),
            deviceInfoRepository(context),
            preferenceRepository(context),
            fingerprintMapRepository(context)
        ).also {
            this.backupManager = it
        }
    }

    fun fileAdapter(context: Context): FileAdapter {
        return (context.applicationContext as KeyMapperApp).fileAdapter
    }

    fun inputMethodAdapter(context: Context): InputMethodAdapter {
        return (context.applicationContext as KeyMapperApp).inputMethodAdapter
    }

    fun externalDevicesAdapter(context: Context): ExternalDevicesAdapter {
        return (context.applicationContext as KeyMapperApp).externalDevicesAdapter
    }

    fun bluetoothMonitor(context: Context): BluetoothMonitor {
        return (context.applicationContext as KeyMapperApp).bluetoothMonitor
    }

    fun notificationController(context: Context): NotificationController {
        return (context.applicationContext as KeyMapperApp).notificationController
    }

    fun resourceProvider(context: Context): ResourceProvider {
        return (context.applicationContext as KeyMapperApp).resourceProvider
    }

    fun packageManagerAdapter(context: Context): PackageManagerAdapter {
        return (context.applicationContext as KeyMapperApp).packageManagerAdapter
    }

    fun cameraAdapter(context: Context): CameraAdapter {
        return (context.applicationContext as KeyMapperApp).cameraAdapter
    }

    fun permissionAdapter(context: Context): AndroidPermissionAdapter {
        return (context.applicationContext as KeyMapperApp).permissionAdapter
    }

    fun systemFeatureAdapter(context: Context): SystemFeatureAdapter {
        return (context.applicationContext as KeyMapperApp).systemFeatureAdapter
    }

    fun serviceAdapter(context: Context): AccessibilityServiceAdapter {
        return (context.applicationContext as KeyMapperApp).serviceAdapter
    }

    fun appShortcutAdapter(context: Context): AppShortcutAdapter {
        return (context.applicationContext as KeyMapperApp).appShortcutAdapter
    }

    fun notificationAdapter(context: Context): AndroidNotificationAdapter {
        return (context.applicationContext as KeyMapperApp).notificationAdapter
    }

    fun popupMessageAdapter(context: Context): PopupMessageAdapter {
        return (context.applicationContext as KeyMapperApp).popupMessageAdapter
    }

    fun vibratorAdapter(context: Context): VibratorAdapter {
        return (context.applicationContext as KeyMapperApp).vibratorAdapter
    }

    fun displayAdapter(context: Context): DisplayAdapter {
        return (context.applicationContext as KeyMapperApp).displayAdapter
    }

    private fun createDatabase(context: Context): KeyMapDatabase {
        val result = Room.databaseBuilder(
            context.applicationContext,
            KeyMapDatabase::class.java,
            KeyMapDatabase.DATABASE_NAME
        ).addMigrations(
            KeyMapDatabase.MIGRATION_1_2,
            KeyMapDatabase.MIGRATION_2_3,
            KeyMapDatabase.MIGRATION_3_4,
            KeyMapDatabase.MIGRATION_4_5,
            KeyMapDatabase.MIGRATION_5_6,
            KeyMapDatabase.MIGRATION_6_7,
            KeyMapDatabase.MIGRATION_7_8,
            KeyMapDatabase.MIGRATION_8_9,
            KeyMapDatabase.MIGRATION_9_10
        ).build()
        /* REMINDER!!!! Need to migrate fingerprint maps and other stuff???
         * Keep this note at the bottom */
        database = result
        return result
    }
}