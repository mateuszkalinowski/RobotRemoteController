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
import kotlin.collections.ArrayList

class BluetoothSettingsActivity : AppCompatActivity() {

    private val REQUEST_ENABLE_BT: Int = 1
    private var startScanningButton: Button? = null
    private var foundedDevicesListView: ListView? = null
    private var scanningProgressBar: ProgressBar? = null
    var bluetoothGatt: BluetoothGatt? = null
    private val discoveredBluetoothDevicesFound: ArrayList<BluetoothListElement> = ArrayList()
    private val customListAdapter: BluetoothSettingsListCustomListAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        BluetoothSettingsListCustomListAdapter(this,discoveredBluetoothDevicesFound)
    }
    private var bluetoothDevice: BluetoothDevice? = null
    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
    var btScanner: BluetoothLeScanner? = null;
    private val BluetoothAdapter.isDisabled: Boolean
        get() = !isEnabled

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth_settings)
        setSupportActionBar(toolbar)
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

        foundedDevicesListView = findViewById(R.id.bluetooth_devices_list) as ListView

        foundedDevicesListView?.adapter = customListAdapter

        foundedDevicesListView?.onItemClickListener = object : AdapterView.OnItemClickListener {

            override fun onItemClick(parent: AdapterView<*>, view: View,
                                     position: Int, id: Long) {

                startScanningButton?.isEnabled = true

                scanningProgressBar?.isVisible = false
                stopScanning()

                var sharedPreferences: SharedPreferences = getPreferences(Context.MODE_PRIVATE)
                var editor: SharedPreferences.Editor = sharedPreferences.edit()
                editor.putString("device_mac_address", discoveredBluetoothDevicesFound[position].deviceMacAddress)
                editor.commit()

                bluetoothDevice = bluetoothAdapter!!.getRemoteDevice(discoveredBluetoothDevicesFound[position].deviceMacAddress);
                bluetoothGatt = bluetoothDevice!!.connectGatt(applicationContext,false, gattCallback)
            }
        }


        bluetoothAdapter?.takeIf { it.isDisabled }?.apply {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        btScanner = bluetoothAdapter?.getBluetoothLeScanner()

    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(
            gatt: BluetoothGatt,
            status: Int,
            newState: Int
        ) {
            val intentAction: String
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.i("tak", "Connected to GATT server.")
                    Log.i(
                        "tak", "Attempting to start service discovery: " +
                                bluetoothGatt?.discoverServices()
                    )
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.i("tak", "Disconnected from GATT server.")
                }
            }
        }
    }

//    private val gattCallback = object : BluetoothGattCallback() {
//        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
//            val intentAction: String
//            if(newState == BluetoothProfile.STATE_CONNECTED) {
//                when (newState) {
//                    BluetoothProfile.STATE_CONNECTED -> {
//                        bluetoothGatt?.discoverServices()
//                    }
//                }
//
//            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
//
//            }
//        }
//
//        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//
//            }
//        }
//
//        override fun onCharacteristicChanged(
//            gatt: BluetoothGatt?,
//            characteristic: BluetoothGattCharacteristic?
//        ) {
//            println(characteristic?.toString())
//            super.onCharacteristicChanged(gatt, characteristic)
//        }
//    }

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


    fun startScanning() {

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
