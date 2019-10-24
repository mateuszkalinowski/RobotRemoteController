package pl.mateuszkalinowski.robotremotecontroller.bluetooth_settings

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import pl.mateuszkalinowski.robotremotecontroller.R

import kotlinx.android.synthetic.main.activity_bluetooth_settings.*
import pl.mateuszkalinowski.robotremotecontroller.list_adapters.BluetoothSettingsListCustomListAdapter
import pl.mateuszkalinowski.robotremotecontroller.model.BluetoothListElement

class BluetoothSettingsActivity : AppCompatActivity() {

    private val REQUEST_ENABLE_BT: Int = 1
    private var startScanningButton: Button? = null
    private var stopScanningButton: Button? = null
    private var foundedDevicesListView: ListView? = null
    private var scanningProgressBar: ProgressBar? = null

    private val discoveredBluetoothDevicesFound: ArrayList<BluetoothListElement> = ArrayList()

    private val customListAdapter: BluetoothSettingsListCustomListAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        BluetoothSettingsListCustomListAdapter(this,discoveredBluetoothDevicesFound)
    }

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
            startScanningButton?.isEnabled = false
            stopScanningButton?.isEnabled = true
            scanningProgressBar?.isVisible = true
            discoveredBluetoothDevicesFound.clear()
            customListAdapter?.notifyDataSetChanged()
            startScanning()
        }

        stopScanningButton = findViewById(R.id.button_stop_scanning) as Button
        stopScanningButton!!.run {
            setOnClickListener {
                startScanningButton?.isEnabled = true
                stopScanningButton?.isEnabled = false
                scanningProgressBar?.isVisible = false
                stopScanning()
            }
        }


        foundedDevicesListView = findViewById(R.id.bluetooth_devices_list) as ListView

        foundedDevicesListView?.adapter = customListAdapter

        foundedDevicesListView?.onItemClickListener = object : AdapterView.OnItemClickListener {

            override fun onItemClick(parent: AdapterView<*>, view: View,
                                     position: Int, id: Long) {
                // Toast the values

                var sharedPreferences: SharedPreferences = getPreferences(Context.MODE_PRIVATE)
                var editor: SharedPreferences.Editor = sharedPreferences.edit()
                editor.putString("device_mac_address", discoveredBluetoothDevicesFound[position].deviceMacAddress)
                editor.commit()

//                Toast.makeText(applicationContext, discoveredBluetoothDevicesFound[position].deviceMacAddress,
//                     Toast.LENGTH_LONG)
//                    .show()
            }
        }

       stopScanningButton?.isEnabled = false


        bluetoothAdapter?.takeIf { it.isDisabled }?.apply {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        btScanner = bluetoothAdapter?.getBluetoothLeScanner()

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


    fun startScanning() {
        AsyncTask.execute {
            run {
                btScanner?.startScan(scalCallback)
            }
        };
    }

    fun stopScanning() {
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
            }

            customListAdapter?.notifyDataSetChanged()


        }
    }

}
