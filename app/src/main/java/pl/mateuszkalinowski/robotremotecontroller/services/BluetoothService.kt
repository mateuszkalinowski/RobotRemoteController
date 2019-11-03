package pl.mateuszkalinowski.robotremotecontroller.services

import android.bluetooth.*

class BluetoothService {

    companion object {
        var bluetoothAdapter: BluetoothAdapter? = null
        var bluetoothDevice: BluetoothDevice? = null
        var bluetoothGatt: BluetoothGatt? = null
        var customCharacteristic: BluetoothGattCharacteristic? = null

        const val ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
        const val ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"
        const val ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"
    }



}