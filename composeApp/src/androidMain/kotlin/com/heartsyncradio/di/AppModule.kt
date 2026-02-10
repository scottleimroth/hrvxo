package com.heartsyncradio.di

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.heartsyncradio.ble.GenericBleManager
import com.heartsyncradio.ble.HrDeviceManager
import com.heartsyncradio.db.HrvXoDatabase
import com.heartsyncradio.db.createDatabase
import com.heartsyncradio.music.MusicApiClient
import com.heartsyncradio.music.SongCoherenceRepository
import com.heartsyncradio.polar.PolarManager
import com.heartsyncradio.viewmodel.HomeViewModel
import com.heartsyncradio.viewmodel.SessionViewModel

enum class DeviceMode {
    POLAR,
    GENERIC_BLE
}

object AppModule {

    @Volatile
    private var deviceManager: HrDeviceManager? = null

    @Volatile
    var currentMode: DeviceMode = DeviceMode.POLAR
        private set

    @Volatile
    private var database: HrvXoDatabase? = null

    @Volatile
    private var musicApiClient: MusicApiClient? = null

    @Volatile
    private var repository: SongCoherenceRepository? = null

    fun getDeviceManager(context: Context, mode: DeviceMode = currentMode): HrDeviceManager {
        if (mode != currentMode && deviceManager != null) {
            deviceManager?.shutDown()
            deviceManager = null
            currentMode = mode
        }
        return deviceManager ?: synchronized(this) {
            deviceManager ?: createManager(context, mode).also {
                deviceManager = it
                currentMode = mode
            }
        }
    }

    private fun createManager(context: Context, mode: DeviceMode): HrDeviceManager {
        return when (mode) {
            DeviceMode.POLAR -> PolarManager(context.applicationContext)
            DeviceMode.GENERIC_BLE -> GenericBleManager(context.applicationContext)
        }
    }

    fun switchMode(context: Context, mode: DeviceMode): HrDeviceManager {
        return getDeviceManager(context, mode)
    }

    private fun getDatabase(context: Context): HrvXoDatabase {
        return database ?: synchronized(this) {
            database ?: createDatabase(context.applicationContext).also { database = it }
        }
    }

    private fun getMusicApiClient(): MusicApiClient {
        return musicApiClient ?: synchronized(this) {
            musicApiClient ?: MusicApiClient().also { musicApiClient = it }
        }
    }

    private fun getRepository(context: Context): SongCoherenceRepository {
        return repository ?: synchronized(this) {
            repository ?: SongCoherenceRepository(getDatabase(context)).also { repository = it }
        }
    }

    fun provideHomeViewModelFactory(context: Context): ViewModelProvider.Factory {
        return viewModelFactory {
            initializer {
                HomeViewModel(getDeviceManager(context))
            }
        }
    }

    fun provideSessionViewModelFactory(context: Context): ViewModelProvider.Factory {
        val ctx = context.applicationContext
        return viewModelFactory {
            initializer {
                SessionViewModel(
                    deviceManagerProvider = { getDeviceManager(ctx) },
                    musicApiClient = getMusicApiClient(),
                    repository = getRepository(ctx)
                )
            }
        }
    }

    fun shutDown() {
        deviceManager?.shutDown()
        deviceManager = null
        musicApiClient?.close()
        musicApiClient = null
    }
}
