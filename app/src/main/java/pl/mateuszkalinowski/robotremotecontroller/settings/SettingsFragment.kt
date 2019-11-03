package pl.mateuszkalinowski.robotremotecontroller.settings

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
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import pl.mateuszkalinowski.robotremotecontroller.MainActivity

import pl.mateuszkalinowski.robotremotecontroller.R
import pl.mateuszkalinowski.robotremotecontroller.list_adapters.BluetoothSettingsListCustomListAdapter
import pl.mateuszkalinowski.robotremotecontroller.model.BluetoothListElement
import pl.mateuszkalinowski.robotremotecontroller.services.BluetoothService
import pl.mateuszkalinowski.robotremotecontroller.services.BluetoothService.Companion.ACTION_GATT_CONNECTED
import pl.mateuszkalinowski.robotremotecontroller.services.BluetoothService.Companion.ACTION_GATT_SERVICES_DISCOVERED
import java.util.*
import kotlin.collections.ArrayList


class SettingsFragment : Fragment() {

    private lateinit var viewModel: SettingsViewModel

    private var cancelParringButton: ImageButton? = null

    private val REQUEST_ENABLE_BT: Int = 1
    private var startScanningButton: Button? = null
    private var foundedDevicesListView: ListView? = null
    private var scanningProgressBar: ProgressBar? = null
    private var bluetoothGatt: BluetoothGatt? = null

    private var connectedDeviceName: TextView? = null
    private var connectedDeviceMacAddress: TextView? = null
    private var connectedDeviceIcon: ImageView? = null

    private val discoveredBluetoothDevicesFound: ArrayList<BluetoothListElement> = ArrayList()
    private val customListAdapter: BluetoothSettingsListCustomListAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        BluetoothSettingsListCustomListAdapter(this.requireContext(),discoveredBluetoothDevicesFound)
    }
    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = this.activity!!.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
    private var bluetoothDevice: BluetoothDevice? = null
    private var customCharacteristic: BluetoothGattCharacteristic? = null
    private var btScanner: BluetoothLeScanner? = null

    var candidateMacAddress: String? = null
    var candidateDeviceName: String? = null
    private var candidatePosition: Int = -1

    private val BluetoothAdapter.isDisabled: Boolean
        get() = !isEnabled

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView: View = inflater.inflate(R.layout.settings_fragment, container, false)



        scanningProgressBar = rootView.findViewById(R.id.scanning_progress_bar) as ProgressBar

        scanningProgressBar?.isVisible = false

        startScanningButton = rootView.findViewById(R.id.button_start_scanning) as Button
        startScanningButton!!.setOnClickListener {

            if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity!!,
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),1)
            }
            else {
                startScanning()
            }
        }

        cancelParringButton = rootView.findViewById(R.id.cancel_paring_button)

        cancelParringButton!!.setOnClickListener {
            bluetoothGatt?.close()
            bluetoothGatt = null

            val sharedPreferences: SharedPreferences = this.activity!!.getSharedPreferences("bluetooth-data",Context.MODE_PRIVATE)
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            editor.putString("device_mac_address", "")
            editor.putString("device_name", "")
            editor.apply()

            val mainActivity = activity as MainActivity
            mainActivity.setBluetoothDeviceAddressAndName()

            setPairedDeviceInfo()

            BluetoothService.customCharacteristic = null
            BluetoothService.bluetoothGatt?.close()
            BluetoothService.bluetoothGatt = null
            activity!!.runOnUiThread {
                mainActivity.setConnectionStatus()
            }


        }

        foundedDevicesListView = rootView.findViewById(R.id.bluetooth_devices_list) as ListView

        foundedDevicesListView?.adapter = customListAdapter

        foundedDevicesListView?.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                startScanningButton?.isEnabled = true

                scanningProgressBar?.isVisible = false
                stopScanning()

                candidateMacAddress = discoveredBluetoothDevicesFound[position].deviceMacAddress
                candidateDeviceName = discoveredBluetoothDevicesFound[position].deviceName
                candidatePosition = position

                bluetoothDevice =
                    bluetoothAdapter!!.getRemoteDevice(discoveredBluetoothDevicesFound[position].deviceMacAddress)
                bluetoothGatt =
                    bluetoothDevice!!.connectGatt(requireContext(), false, mGattCallback)
            }


        bluetoothAdapter?.takeIf { it.isDisabled }?.apply {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        btScanner = bluetoothAdapter?.bluetoothLeScanner

        connectedDeviceName = rootView.findViewById(R.id.device_name)
        connectedDeviceMacAddress = rootView.findViewById(R.id.device_mac_address)
        connectedDeviceIcon = rootView.findViewById(R.id.bluetooth_icon)

        startScanning()
        setPairedDeviceInfo()
        return rootView

    }




    private fun setPairedDeviceInfo() {
        val sharedPreferences: SharedPreferences = activity!!.getSharedPreferences("bluetooth-data",Context.MODE_PRIVATE)
        val deviceUUID = sharedPreferences.getString("device_mac_address", "").orEmpty()
        val deviceName = sharedPreferences.getString("device_name","").orEmpty()


        if(deviceUUID != "") {
            connectedDeviceName?.text = deviceName
            connectedDeviceMacAddress?.text = deviceUUID
            connectedDeviceIcon?.visibility = View.VISIBLE
            cancelParringButton?.visibility = View.VISIBLE
        } else {
            connectedDeviceName?.text = getString(R.string.no_paired_deviced)
            connectedDeviceIcon?.visibility = View.INVISIBLE
            connectedDeviceMacAddress?.text = ""
            cancelParringButton?.visibility = View.INVISIBLE
        }
    }

    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        activity!!.sendBroadcast(intent)
    }

    private val mGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            val intentAction: String
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED
                broadcastUpdate(intentAction)
                Log.i("BLUETOOTH","Connected to GATT server")
                Log.i("BLUETOOTH","Attemting to discover services" + bluetoothGatt?.discoverServices())
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                for (gattService in gatt!!.services) {
                  //  Log.i("BLUETOOTH", "onServicesDiscovered: ---------------------")
                  //  Log.i("BLUETOOTH", "onServicesDiscovered: service=" + gattService.uuid)
                    for (characteristic in gattService.characteristics) {
//                        Log.i(
//                            "BLUETOOTH",
//                            "onServicesDiscovered: characteristic=" + characteristic.uuid
//                        )
                        if (characteristic.uuid == UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")) {

                            customCharacteristic = characteristic
                            val sharedPreferences: SharedPreferences = activity!!.getSharedPreferences("bluetooth-data",Context.MODE_PRIVATE)
                            val editor: SharedPreferences.Editor = sharedPreferences.edit()
                            editor.putString("device_mac_address", candidateMacAddress)
                            editor.putString("device_name", candidateDeviceName)
                            editor.apply()

                            val mainActivity = activity as MainActivity
                            mainActivity.setBluetoothDeviceAddressAndName()

                            bluetoothGatt?.close()
                            bluetoothGatt = null
                            activity!!.runOnUiThread {
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
                Toast.makeText(requireContext(),"OK",Toast.LENGTH_LONG).show()
            }
            else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(requireContext(),"CANCEL",Toast.LENGTH_LONG).show()

            }
        }
    }

    private fun startScanning() {

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

    private fun stopScanning() {
        startScanningButton?.isEnabled = true
        scanningProgressBar?.isVisible = false
        AsyncTask.execute {
            run {
                btScanner?.stopScan(scalCallback)
            }
        }
    }

    private val scalCallback: ScanCallback = object :  ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)

            var deviceName: String?
            val deviceAddress: String? = result?.device?.address

            deviceName = result?.device?.name

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
                    if(item.deviceName == "Nazwa nieznana" && newElem.deviceName != "Nazwa nieznana") {
                        discoveredBluetoothDevicesFound.remove(item)
                        discoveredBluetoothDevicesFound.add(newElem)
                    }
                }
            }
            customListAdapter?.notifyDataSetChanged()
        }
    }




    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SettingsViewModel::class.java)
    }

}
