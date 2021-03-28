package io.github.sds100.keymapper

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Room
import io.github.sds100.keymapper.data.BackupManager
import io.github.sds100.keymapper.data.IBackupManager
import io.github.sds100.keymapper.data.db.AppDatabase
import io.github.sds100.keymapper.data.db.DefaultDataStoreManager
import io.github.sds100.keymapper.data.db.IDataStoreManager
import io.github.sds100.keymapper.data.preferences.DataStorePreferenceRepository
import io.github.sds100.keymapper.data.repository.*
import io.github.sds100.keymapper.domain.adapter.*
import io.github.sds100.keymapper.domain.packages.PackageManagerAdapter
import io.github.sds100.keymapper.domain.repositories.PreferenceRepository
import io.github.sds100.keymapper.domain.usecases.BackupRestoreUseCase
import io.github.sds100.keymapper.framework.adapters.AppUiAdapter
import io.github.sds100.keymapper.framework.adapters.LauncherShortcutAdapter
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import kotlinx.coroutines.runBlocking

/**
 * Created by sds100 on 17/05/2020.
 */
object ServiceLocator {

    private val lock = Any()
    private var database: AppDatabase? = null

    @Volatile
    private var keymapRepository: DefaultKeymapRepository? = null

    fun defaultKeymapRepository(context: Context): DefaultKeymapRepository {
        synchronized(this) {
            return keymapRepository ?: createKeymapRepository(context)
        }
    }

    private fun createKeymapRepository(context: Context): DefaultKeymapRepository {
        val database = database ?: createDatabase(context.applicationContext)
        keymapRepository = DefaultKeymapRepository(
            database.keymapDao(),
            (context.applicationContext as MyApplication).appCoroutineScope
        )
        return keymapRepository!!
    }

    @Volatile
    private var deviceInfoRepository: DeviceInfoCache? = null

    fun deviceInfoRepository(context: Context): DeviceInfoCache {
        synchronized(this) {
            return deviceInfoRepository ?: createDeviceInfoRepository(context)
        }
    }

    @Volatile
    private var roomKeymapRepository: RoomKeymapRepository? = null

    fun roomKeymapRepository(context: Context): RoomKeymapRepository {
        synchronized(this) {
            val dataBase = database ?: createDatabase(context.applicationContext)

            return roomKeymapRepository ?: RoomKeymapRepository(
                dataBase.keymapDao(),
                (context.applicationContext as MyApplication).appCoroutineScope
            ).also {
                this.roomKeymapRepository = it
            }
        }
    }

    private fun createDeviceInfoRepository(context: Context): DeviceInfoCache {
        val database = database ?: createDatabase(context.applicationContext)
        deviceInfoRepository = RoomDeviceInfoCache(
            database.deviceInfoDao(),
            (context.applicationContext as MyApplication).appCoroutineScope
        )
        return deviceInfoRepository!!
    }

    @Volatile
    private var dataStoreManager: IDataStoreManager? = null

    private fun dataStoreManager(context: Context): IDataStoreManager {
        synchronized(this) {
            return dataStoreManager ?: createDataStoreManager(context)
        }
    }

    private fun createDataStoreManager(context: Context): IDataStoreManager {
        return dataStoreManager ?: DefaultDataStoreManager(context.applicationContext).also {
            this.dataStoreManager = it
        }
    }

    @Volatile
    private var fingerprintMapRepository: FingerprintMapRepository? = null

    fun fingerprintMapRepository(context: Context): FingerprintMapRepository {
        synchronized(this) {
            return fingerprintMapRepository
                ?: createFingerprintMapRepository(context)
        }
    }

    private fun createFingerprintMapRepository(context: Context): FingerprintMapRepository {
        val dataStore = dataStoreManager(context).fingerprintGestureDataStore
        val scope = (context.applicationContext as MyApplication).appCoroutineScope

        return fingerprintMapRepository
            ?: DefaultFingerprintMapRepository(dataStore, scope).also {
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
    private var systemActionRepository: SystemActionRepository? = null

    fun systemActionRepository(context: Context): SystemActionRepository {
        synchronized(this) {
            return systemActionRepository ?: createSystemActionRepository(context)
        }
    }

    private fun createSystemActionRepository(context: Context): SystemActionRepository {
        return systemActionRepository
            ?: DefaultSystemActionRepository(context.applicationContext).also {
                this.systemActionRepository = it
            }
    }

    @Volatile
    private var packageRepository: AndroidAppRepository? = null

    fun packageRepository(context: Context): AppRepository {
        synchronized(this) {
            return packageRepository ?: createPackageRepository(context)
        }
    }

    private fun createPackageRepository(context: Context): AppRepository {
        return packageRepository
            ?: AndroidAppRepository(context.packageManager).also {
                this.packageRepository = it
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
                (context.applicationContext as MyApplication).appCoroutineScope
            ).also {
                this.preferenceRepository = it
            }
    }

    @Volatile
    private var backupManager: IBackupManager? = null

    fun backupManager(context: Context): IBackupManager {
        synchronized(this) {
            return backupManager ?: createBackupManager(context)
        }
    }

    private fun createBackupManager(context: Context): IBackupManager {
        return backupManager ?: BackupManager(
            defaultKeymapRepository(context),
            fingerprintMapRepository(context),
            deviceInfoRepository(context),
            (context.applicationContext as MyApplication).appCoroutineScope,
            (context.applicationContext as MyApplication),
            BackupRestoreUseCase(preferenceRepository(context))
        ).also {
            this.backupManager = it
        }
    }

    fun inputMethodAdapter(context: Context): InputMethodAdapter {
        return (context.applicationContext as MyApplication).inputMethodAdapter
    }

    fun externalDeviceAdapter(context: Context): ExternalInputDeviceAdapter {
        return (context.applicationContext as MyApplication).externalDeviceAdapter
    }

    fun bluetoothMonitor(context: Context): BluetoothMonitor {
        return (context.applicationContext as MyApplication).bluetoothMonitor
    }

    fun notificationController(context: Context): NotificationController {
        return (context.applicationContext as MyApplication).notificationController
    }

    fun resourceProvider(context: Context): ResourceProvider {
        return (context.applicationContext as MyApplication).resourceProvider
    }

    fun appRepository(context: Context): AppRepository {
        return (context.applicationContext as MyApplication).appRepository
    }

    fun appInfoAdapter(context: Context): AppUiAdapter {
        return (context.applicationContext as MyApplication).appInfoAdapter
    }

    fun packageManagerAdapter(context: Context): PackageManagerAdapter {
        return (context.applicationContext as MyApplication).packageManagerAdapter
    }

    fun cameraAdapter(context: Context): CameraAdapter {
        return (context.applicationContext as MyApplication).cameraAdapter
    }

    fun permissionAdapter(context: Context): PermissionAdapter {
        return (context.applicationContext as MyApplication).permissionAdapter
    }

    fun systemFeatureAdapter(context: Context): SystemFeatureAdapter {
        return (context.applicationContext as MyApplication).systemFeatureAdapter
    }

    fun serviceAdapter(context: Context): ServiceAdapter {
        return (context.applicationContext as MyApplication).serviceAdapter
    }

    fun launcherShortcutAdapter(context: Context): LauncherShortcutAdapter {
        return (context.applicationContext as MyApplication).launcherShortcutAdapter
    }

    @VisibleForTesting
    fun resetKeymapRepository() {
        synchronized(lock) {
            runBlocking {
                keymapRepository?.deleteAll()
                deviceInfoRepository?.deleteAll()
            }

            database?.apply {
                clearAllTables()
                close()
            }

            database = null
            keymapRepository = null
            deviceInfoRepository = null
        }
    }

    private fun createDatabase(context: Context): AppDatabase {
        val result = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).addMigrations(
            AppDatabase.MIGRATION_1_2,
            AppDatabase.MIGRATION_2_3,
            AppDatabase.MIGRATION_3_4,
            AppDatabase.MIGRATION_4_5,
            AppDatabase.MIGRATION_5_6,
            AppDatabase.MIGRATION_6_7,
            AppDatabase.MIGRATION_7_8,
            AppDatabase.MIGRATION_8_9,
            AppDatabase.MIGRATION_9_10
        ).build()
        /* REMINDER!!!! Need to migrate fingerprint maps and other stuff???
         * Keep this note at the bottom */
        database = result
        return result
    }

    fun release() {
        synchronized(this) {
            packageRepository = null
            systemActionRepository = null
        }
    }
}