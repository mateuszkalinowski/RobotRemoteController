//package pl.mateuszkalinowski.robotremotecontroller.services
//
//import android.bluetooth.BluetoothAdapter
//import android.bluetooth.BluetoothDevice
//import android.bluetooth.BluetoothGatt
//import android.bluetooth.BluetoothManager
//import android.bluetooth.le.BluetoothLeScanner
//import android.content.Context
//import androidx.core.content.ContextCompat.getSystemService
//
//class BluetoothService(context: Context) {
//
//    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
//        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
//        bluetoothManager.adapter
//    }
//
//
//
//    var btScanner: BluetoothLeScanner? = null;
//
//
//    private val BluetoothAdapter.isDisabled: Boolean
//        get() = !isEnabled
//
//    constructor() {
//
//    }
//
//
//    fun connect(macAddress: String){
//       var bluetoothDevice: BluetoothDevice  =
//    }
//}