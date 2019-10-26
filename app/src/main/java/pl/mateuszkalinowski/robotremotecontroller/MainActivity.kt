package pl.mateuszkalinowski.robotremotecontroller

import android.app.Activity
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import pl.mateuszkalinowski.robotremotecontroller.bluetooth_settings.ACTION_GATT_CONNECTED
import pl.mateuszkalinowski.robotremotecontroller.bluetooth_settings.ACTION_GATT_SERVICES_DISCOVERED
import pl.mateuszkalinowski.robotremotecontroller.services.BluetoothService
import java.util.*

class MainActivity : AppCompatActivity() {

    private val REQUEST_ENABLE_BT: Int = 1

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val BluetoothAdapter.isDisabled: Boolean
        get() = !isEnabled

    private var bluetoothDevice: BluetoothDevice? = null
    private var customCharacteristic: BluetoothGattCharacteristic? = null
    private var bluetoothGatt: BluetoothGatt? = null

    var deviceUUID: String = "";
    var deviceName: String = "";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        val navigationController = findNavController(R.id.navigation_fragment)

        val applicationBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.steering_fragment,R.id.information_fragment,R.id.settings_fragment
            )
        )
        //setSupportActionBar(findViewById(R.id.main_toolbar))
        //setupActionBarWithNavController(navigationController,applicationBarConfiguration)
        bottomNavigationView.setupWithNavController(navigationController)

        setBluetoothDeviceAddressAndName()

        if(deviceUUID != "") {

          //  if(BluetoothService.bluetoothGatt == null) {

                bluetoothAdapter?.takeIf { it.isDisabled }?.apply {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                }

                bluetoothDevice =
                    bluetoothAdapter!!.getRemoteDevice(deviceUUID)

                bluetoothGatt =
                    bluetoothDevice!!.connectGatt(applicationContext, false, mGattCallback)

                BluetoothService.bluetoothGatt = bluetoothGatt
//            }
//            else {
//                Toast.makeText(applicationContext,"Nie null",Toast.LENGTH_LONG).show()
//
//
//
//            }

        } else {
            Toast.makeText(applicationContext,"Nie znaleziono urządzenia. Wybierz urządzenie bluetooth z ustawień",Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        setBluetoothDeviceAddressAndName()

        if(deviceUUID != "") {

            if(BluetoothService.bluetoothGatt == null) {

                bluetoothAdapter?.takeIf { it.isDisabled }?.apply {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                }

                bluetoothDevice =
                    bluetoothAdapter!!.getRemoteDevice(deviceUUID)

                bluetoothGatt =
                    bluetoothDevice!!.connectGatt(applicationContext, false, mGattCallback)

                BluetoothService.bluetoothGatt = bluetoothGatt
            }

        } else {
            Toast.makeText(applicationContext,"Nie znaleziono urządzenia. Wybierz urządzenie bluetooth z ustawień",Toast.LENGTH_LONG).show()
        }
    }



    fun setBluetoothDeviceAddressAndName(){
        var sharedPreferences: SharedPreferences = getSharedPreferences("bluetooth-data",Context.MODE_PRIVATE)
        deviceUUID = sharedPreferences.getString("device_mac_address", "").orEmpty()
        deviceName = sharedPreferences.getString("device_name","").orEmpty()
    }

    val mGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            var intentAction: String;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                broadcastUpdate(intentAction)
                Log.i("BLUETOOTH","Connected to GATT server")
                Log.i("BLUETOOTH","Attemting to discover services" + bluetoothGatt?.discoverServices())
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                for (gattService in gatt!!.getServices()) {
                    Log.i("BLUETOOTH", "onServicesDiscovered: ---------------------")
                    Log.i("BLUETOOTH", "onServicesDiscovered: service=" + gattService.uuid)
                    for (characteristic in gattService.characteristics) {
                        Log.i(
                            "BLUETOOTH",
                            "onServicesDiscovered: characteristic=" + characteristic.uuid
                        )
                        if (characteristic.uuid == UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")) {

                            customCharacteristic = characteristic
                            BluetoothService.customCharacteristic = customCharacteristic;

                        }
                    }
                }
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)
            } else {
                Log.w("BLUETOOTH", "onServicesDiscovered received: $status")
            }
        }
    }

    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
    }

}
