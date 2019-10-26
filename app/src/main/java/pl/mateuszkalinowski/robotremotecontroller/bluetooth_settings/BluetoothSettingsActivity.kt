package pl.mateuszkalinowski.robotremotecontroller.bluetooth_settings

import android.Manifest
import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import pl.mateuszkalinowski.robotremotecontroller.R

import kotlinx.android.synthetic.main.activity_bluetooth_settings.*
import pl.mateuszkalinowski.robotremotecontroller.list_adapters.BluetoothSettingsListCustomListAdapter
import pl.mateuszkalinowski.robotremotecontroller.model.BluetoothListElement
import java.util.*
import kotlin.collections.ArrayList

const val ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
const val ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"
const val ACTION_GATT_SERVICES_DISCOVERED =
    "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"


class BluetoothSettingsActivity : AppCompatActivity() {

    private var cancelParringButton: ImageButton? = null

    private val REQUEST_ENABLE_BT: Int = 1
    private var startScanningButton: Button? = null
    private var foundedDevicesListView: ListView? = null
    private var scanningProgressBar: ProgressBar? = null
    private var bluetoothGatt: BluetoothGatt? = null

    private var connectedDeviceName: TextView? = null;
    private var connectedDeviceMacAddress: TextView? = null;
    private var connectedDeviceIcon: ImageView? = null;

    private val discoveredBluetoothDevicesFound: ArrayList<BluetoothListElement> = ArrayList()
    private val customListAdapter: BluetoothSettingsListCustomListAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        BluetoothSettingsListCustomListAdapter(this,discoveredBluetoothDevicesFound)
    }
    private var bluetoothDevice: BluetoothDevice? = null
    private var customCharacteristic: BluetoothGattCharacteristic? = null
    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
    var btScanner: BluetoothLeScanner? = null

    var candidateMacAddress: String? = null
    var candidateDeviceName: String? = null
    var candidatePosition: Int = -1

    private val BluetoothAdapter.isDisabled: Boolean
        get() = !isEnabled

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth_settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        scanningProgressBar = findViewById(R.id.scanning_progress_bar) as ProgressBar

        scanningProgressBar?.isVisible = false

        startScanningButton = findViewById(R.id.button_start_scanning) as Button
        startScanningButton!!.setOnClickListener {

            if(ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),1)
            }
            else {
                startScanning()
            }
        }
//
//
//        testButton!!.setOnClickListener {
//            customCharacteristic!!.setValue("12")
//            bluetoothGatt!!.writeCharacteristic(customCharacteristic)
//        }

        cancelParringButton = findViewById(R.id.cancel_paring_button)

        cancelParringButton!!.setOnClickListener {
            bluetoothGatt?.close()
            bluetoothGatt = null

            var sharedPreferences: SharedPreferences = getSharedPreferences("bluetooth-data",Context.MODE_PRIVATE)
            var editor: SharedPreferences.Editor = sharedPreferences.edit()
            editor.putString("device_mac_address", "")
            editor.putString("device_name", "")
            editor.apply()

            setPairedDeviceInfo()

        }

        foundedDevicesListView = findViewById(R.id.bluetooth_devices_list) as ListView

        foundedDevicesListView?.adapter = customListAdapter

        foundedDevicesListView?.onItemClickListener = object : AdapterView.OnItemClickListener {

            override fun onItemClick(parent: AdapterView<*>, view: View,
                                     position: Int, id: Long) {

                startScanningButton?.isEnabled = true

                scanningProgressBar?.isVisible = false
                stopScanning()

                candidateMacAddress = discoveredBluetoothDevicesFound[position].deviceMacAddress
                candidateDeviceName = discoveredBluetoothDevicesFound[position].deviceName
                candidatePosition = position

                bluetoothDevice =
                    bluetoothAdapter!!.getRemoteDevice(discoveredBluetoothDevicesFound[position].deviceMacAddress);
                bluetoothGatt =
                    bluetoothDevice!!.connectGatt(applicationContext, false, mGattCallback)
            }
        }


        bluetoothAdapter?.takeIf { it.isDisabled }?.apply {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        btScanner = bluetoothAdapter?.getBluetoothLeScanner()

        connectedDeviceName = findViewById(R.id.device_name)
        connectedDeviceMacAddress = findViewById(R.id.device_mac_address)
        connectedDeviceIcon = findViewById(R.id.bluetooth_icon)

        startScanning()

//        var sharedPreferences: SharedPreferences = getSharedPreferences("bluetooth-data",Context.MODE_PRIVATE)
//        val deviceUUID = sharedPreferences.getString("device_mac_address", "").orEmpty()
//        val deviceName = sharedPreferences.getString("device_name","").orEmpty()
//
//
//        if(deviceUUID != "") {
//            connectedDeviceName?.text = deviceName
//            connectedDeviceMacAddress?.text = deviceUUID
//        } else {
//            connectedDeviceName?.text = "Brak sparowanego urządzenia"
//            connectedDeviceIcon?.visibility = View.INVISIBLE
//        }

        setPairedDeviceInfo()
    }

    private fun setPairedDeviceInfo() {
        var sharedPreferences: SharedPreferences = getSharedPreferences("bluetooth-data",Context.MODE_PRIVATE)
        val deviceUUID = sharedPreferences.getString("device_mac_address", "").orEmpty()
        val deviceName = sharedPreferences.getString("device_name","").orEmpty()


        if(deviceUUID != "") {
            connectedDeviceName?.text = deviceName
            connectedDeviceMacAddress?.text = deviceUUID
            connectedDeviceIcon?.visibility = View.VISIBLE
            cancelParringButton?.visibility = View.VISIBLE
        } else {
            connectedDeviceName?.text = "Brak sparowanego urządzenia"
            connectedDeviceIcon?.visibility = View.INVISIBLE
            connectedDeviceMacAddress?.text = ""
            cancelParringButton?.visibility = View.INVISIBLE
        }
    }

    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
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
                            var sharedPreferences: SharedPreferences = getSharedPreferences("bluetooth-data",Context.MODE_PRIVATE)
                            var editor: SharedPreferences.Editor = sharedPreferences.edit()
                            editor.putString("device_mac_address", candidateMacAddress)
                            editor.putString("device_name", candidateDeviceName)
                            editor.commit()

                            runOnUiThread {
                                setPairedDeviceInfo()
                            }

                        }
                    }
                }
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)
            } else {
                Log.w("BLUETOOTH", "onServicesDiscovered received: $status")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(applicationContext,"OK",Toast.LENGTH_LONG).show()
            }
            else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(applicationContext,"CANCEL",Toast.LENGTH_LONG).show()

            }
        }
    }

    override fun onStop() {
        super.onStop()
        bluetoothGatt?.close()
        bluetoothGatt = null
    }


    fun startScanning() {

        candidatePosition = -1

        startScanningButton?.isEnabled = false
        scanningProgressBar?.isVisible = true
        discoveredBluetoothDevicesFound.clear()
        customListAdapter?.notifyDataSetChanged()
        AsyncTask.execute {
            run {
                btScanner?.startScan(scalCallback)
            }
        }
        Handler().postDelayed({
            stopScanning()

        }, 5000)
    }

    fun stopScanning() {
        startScanningButton?.isEnabled = true
        scanningProgressBar?.isVisible = false
        AsyncTask.execute {
            run {
                btScanner?.stopScan(scalCallback)
            }
        };
    }

    val scalCallback: ScanCallback = object :  ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)

            var deviceName: String?;
            var deviceAddress: String?;

            deviceName = result?.device?.name
            deviceAddress = result?.device?.address

            if(deviceName.isNullOrBlank())
                deviceName = "Nazwa nieznana"

            if(!deviceAddress.isNullOrEmpty()) {
                val newElem = BluetoothListElement(
                    deviceName,
                    deviceAddress
                )
                if(!discoveredBluetoothDevicesFound.contains(newElem))
                    discoveredBluetoothDevicesFound.add(newElem)
                else {
                    val item =
                        discoveredBluetoothDevicesFound[discoveredBluetoothDevicesFound.indexOf(newElem)]
                    if(item.deviceName.equals("Nazwa nieznana") && !newElem.deviceName.equals("Nazwa nieznana")) {
                        discoveredBluetoothDevicesFound.remove(item)
                        discoveredBluetoothDevicesFound.add(newElem)
                    }
                }
            }
            customListAdapter?.notifyDataSetChanged()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode) {
            1 -> {
                if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startScanning()
                }
                else {
                    Toast.makeText(applicationContext, "Bez uprawnień do lokalizacji nie jest możliwe wyszukiwanie urządzeń bluetooth",
                        Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}
