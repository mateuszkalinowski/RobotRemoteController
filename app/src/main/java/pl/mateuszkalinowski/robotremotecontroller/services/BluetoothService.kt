package pl.mateuszkalinowski.robotremotecontroller.services

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.content.ContextCompat.getSystemService

class BluetoothService {

    private constructor() {

    }

    var bluetoothGatt: BluetoothGatt? = null

    private val bluetoothAdapter: BluetoothAdapter? = null;

    companion object {
        private lateinit var instance: BluetoothService

        val bluetoothServiceInstance: BluetoothService
            get() {
                if(instance == null) {
                    instance = BluetoothService()
                }
                return instance
            }
    }

}