package pl.mateuszkalinowski.robotremotecontroller

import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
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

    private lateinit var connectButton: Button

    private lateinit var connectionStatusTextView: TextView

    var deviceUUID: String = ""
    var deviceName: String = ""

    private var distance: String = "";

    fun getDistance(): String {
        return distance
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        val navigationController = findNavController(R.id.navigation_fragment)

        bottomNavigationView.setupWithNavController(navigationController)

        setBluetoothDeviceAddressAndName()

        if(deviceUUID != "") {

                bluetoothAdapter?.takeIf { it.isDisabled }?.apply {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                }

                bluetoothDevice =
                    bluetoothAdapter!!.getRemoteDevice(deviceUUID)

                bluetoothGatt =
                    bluetoothDevice!!.connectGatt(applicationContext, false, mGattCallback)

                BluetoothService.bluetoothGatt = bluetoothGatt
        } else {
            Toast.makeText(applicationContext,"Nie znaleziono urządzenia. Wybierz urządzenie bluetooth z ustawień",Toast.LENGTH_LONG).show()
        }

        connectionStatusTextView = findViewById(R.id.connection_status)
        connectButton = findViewById(R.id.button_recconect)

        if(BluetoothService.bluetoothGatt != null && BluetoothService.customCharacteristic != null) {
            connectionStatusTextView.text = "Połączony"
            connectionStatusTextView.setTextColor(ContextCompat.getColor(applicationContext, R.color.success))
            connectButton.text = "Rozłącz"
        } else {
            connectionStatusTextView.text = "Rozłączony"
            connectionStatusTextView.setTextColor(ContextCompat.getColor(applicationContext, R.color.failure))
        }

        connectButton.setOnClickListener{
            if(BluetoothService.bluetoothGatt != null) {
                BluetoothService.bluetoothGatt?.close()
                BluetoothService.bluetoothGatt = null
                BluetoothService.customCharacteristic = null

                connectionStatusTextView.text = "Rozłączony"
                connectionStatusTextView.setTextColor(ContextCompat.getColor(applicationContext, R.color.failure))
                connectButton.text = "Połącz"
            } else {
                if(deviceUUID =="") {
                    Toast.makeText(applicationContext,"Nie znaleziono urządzenia. Wybierz urządzenie bluetooth z ustawień",Toast.LENGTH_LONG).show()
                }
                else {
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
            }
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


        if(BluetoothService.bluetoothGatt != null && BluetoothService.customCharacteristic != null) {
            connectionStatusTextView.text = "Połączony"
            connectionStatusTextView.setTextColor(ContextCompat.getColor(applicationContext, R.color.success))
            connectButton.text = "Rozłącz"
        } else {
            connectionStatusTextView.text = "Rozłączony"
            connectionStatusTextView.setTextColor(ContextCompat.getColor(applicationContext, R.color.failure))
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
                intentAction = ACTION_GATT_CONNECTED
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
                            BluetoothService.customCharacteristic = customCharacteristic

                            bluetoothGatt?.setCharacteristicNotification(characteristic,true)

                            val uuid: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
                            val descriptor = characteristic.getDescriptor(uuid).apply {
                                value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            }
                            bluetoothGatt?.writeDescriptor(descriptor)

                            runOnUiThread {
                                connectionStatusTextView.text = "Połączony"
                                connectionStatusTextView.setTextColor(ContextCompat.getColor(applicationContext, R.color.success))
                                connectButton.text = "Rozłącz"
                            }
                        }
                    }
                }
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)
            } else {
                BluetoothService.bluetoothGatt?.close()
                BluetoothService.bluetoothGatt = null
                Log.w("BLUETOOTH", "onServicesDiscovered received: $status")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            //super.onCharacteristicChanged(gatt, characteristic)
            val receivedValue = characteristic?.getStringValue(0).orEmpty()
            if(receivedValue.startsWith("MSG+DST",ignoreCase = true)) {
                    distance = receivedValue.substring("MSG+DST".length)
                }
        }

    }


    fun convertFromInteger(i: Int): UUID {
        val MSB = 0x0000000000001000L
        val LSB = -0x7fffff7fa064cb05L
        val value = (i and -0x1).toLong()
        return UUID(MSB or (value shl 32), LSB)
    }

    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
    }

}
