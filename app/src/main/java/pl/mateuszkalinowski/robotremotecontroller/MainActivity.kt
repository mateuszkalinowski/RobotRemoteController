package pl.mateuszkalinowski.robotremotecontroller

import android.app.Activity
import android.bluetooth.*
import android.content.Context
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

class MainActivity : AppCompatActivity() {

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

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
        setSupportActionBar(findViewById(R.id.main_toolbar))
        setupActionBarWithNavController(navigationController,applicationBarConfiguration)
        bottomNavigationView.setupWithNavController(navigationController)

        setBluetoothDeviceAddressAndName()

        if(deviceUUID != "") {

        } else {

        }
    }

    override fun onResume() {
        super.onResume()
        setBluetoothDeviceAddressAndName()
    }



    fun setBluetoothDeviceAddressAndName(){
        var sharedPreferences: SharedPreferences = getSharedPreferences("bluetooth-data",Context.MODE_PRIVATE)
        deviceUUID = sharedPreferences.getString("device_mac_address", "").orEmpty()
        deviceName = sharedPreferences.getString("device_name","").orEmpty()
    }

}
