package pl.mateuszkalinowski.robotremotecontroller.bluetooth_settings

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.*
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import pl.mateuszkalinowski.robotremotecontroller.R

import kotlinx.android.synthetic.main.activity_bluetooth_settings.*
import pl.mateuszkalinowski.robotremotecontroller.list_adapters.BluetoothSettingsListCustomListAdapter
import pl.mateuszkalinowski.robotremotecontroller.model.BluetoothListElement

class BluetoothSettingsActivity : AppCompatActivity() {

    private val REQUEST_ENABLE_BT: Int = 1
    private var peripheralTextView: TextView? = null
    private var startScanningButton: Button? = null
    private var stopScanningButton: Button? = null
    private var foundedDevicesListView: ListView? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private val discoveredBluetoothDevicesFound: ArrayList<BluetoothListElement> = ArrayList()

    private val adapter: BluetoothSettingsListCustomListAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        BluetoothSettingsListCustomListAdapter(this,discoveredBluetoothDevicesFound)
    }

//    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
//        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
//        bluetoothManager.adapter
//    }



    var btScanner: BluetoothLeScanner? = null;


    private val BluetoothAdapter.isDisabled: Boolean
        get() = !isEnabled

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth_settings)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        peripheralTextView = findViewById(R.id.results) as TextView

        startScanningButton = findViewById(R.id.button_start_scanning) as Button
        startScanningButton!!.setOnClickListener {
            startScanningButton?.isEnabled = false
            stopScanningButton?.isEnabled = true
            startScanning()
        }

        stopScanningButton = findViewById(R.id.button_stop_scanning) as Button
        stopScanningButton!!.run {
            setOnClickListener {
                startScanningButton?.isEnabled = true
                stopScanningButton?.isEnabled = false
                stopScanning()
            }
        }


        foundedDevicesListView = findViewById(R.id.bluetooth_devices_list) as ListView

      //  val adapter = BluetoothSettingsListCustomListAdapter(this,discoveredBluetoothDevicesFound)
        foundedDevicesListView?.adapter = adapter

       stopScanningButton?.isEnabled = false

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        bluetoothAdapter = bluetoothManager.adapter;


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
        peripheralTextView!!.setText("");
        AsyncTask.execute {
            run {
                btScanner?.startScan(scalCallback)
            }
        };
    }

    fun stopScanning() {
        peripheralTextView!!.append("Stopped Scanning")
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

            adapter?.notifyDataSetChanged()


        }
    }


}
