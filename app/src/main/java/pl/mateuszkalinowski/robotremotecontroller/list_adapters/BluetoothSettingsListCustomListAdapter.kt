package pl.mateuszkalinowski.robotremotecontroller.list_adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import pl.mateuszkalinowski.robotremotecontroller.R
import pl.mateuszkalinowski.robotremotecontroller.model.BluetoothListElement

class BluetoothSettingsListCustomListAdapter(
    private val context: Context,
    private val dataSource: ArrayList<BluetoothListElement>
    ) : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = inflater.inflate(R.layout.bluetooth_settings_list_view_row,parent,false)
        val deviceNameTextField = rowView.findViewById<TextView>(R.id.device_name)
        val deviceMacAddressTextField = rowView.findViewById<TextView>(R.id.device_mac_address)

        val bluetoothListElement = getItem(position) as BluetoothListElement

        deviceNameTextField.text = bluetoothListElement.deviceName
        deviceMacAddressTextField.text = bluetoothListElement.deviceMacAddress

        return rowView
    }

    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return dataSource.size
    }

//    fun updateData(newData: ArrayList<BluetoothListElement>) {
//        dataSource.clear();
//        dataSource.addAll(newData);
//        super.notifyDataSetChanged()
//    }

    private val inflater: LayoutInflater
            = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater


}