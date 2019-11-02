package pl.mateuszkalinowski.robotremotecontroller.services

import android.bluetooth.*

class BluetoothService {

    companion object {
        var bluetoothGatt: BluetoothGatt? = null
        var customCharacteristic: BluetoothGattCharacteristic? = null
    }



}