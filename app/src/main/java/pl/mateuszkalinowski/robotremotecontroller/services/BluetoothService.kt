package pl.mateuszkalinowski.robotremotecontroller.services

import android.app.Service
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.content.ContextCompat.getSystemService

class BluetoothService {

    companion object {
        var bluetoothGatt: BluetoothGatt? = null
        var customCharacteristic: BluetoothGattCharacteristic? = null
    }



}