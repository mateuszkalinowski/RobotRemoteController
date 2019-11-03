package pl.mateuszkalinowski.robotremotecontroller.steering

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import pl.mateuszkalinowski.robotremotecontroller.MainActivity

import pl.mateuszkalinowski.robotremotecontroller.R
import pl.mateuszkalinowski.robotremotecontroller.services.BluetoothService

class SteeringFragment : Fragment() {

    private lateinit var viewModel: SteeringViewModel

    private lateinit var velocitySeekBar: SeekBar
    private lateinit var headControlSlider: SeekBar

    private lateinit var displayBacklightSwitch: Switch
    private lateinit var distanceSensorSwitch: Switch

    fun setDefaultSettings() {
        velocitySeekBar.progress = 100
        headControlSlider.progress = 90

        displayBacklightSwitch.isChecked = true
        distanceSensorSwitch.isChecked = true
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val rootView: View =  inflater.inflate(R.layout.steering_fragment, container, false)

        val distanceTextView: TextView = rootView.findViewById(R.id.distance_text_view)

        val thread = object : Thread() {

            override fun run() {
                try {
                    while (!this.isInterrupted) {
                        sleep(2000)
                        activity?.runOnUiThread {
                            val mainActivity = activity as MainActivity
                            distanceTextView.text = mainActivity.getDistance()
                        }
                    }
                } catch (e: InterruptedException) {
                    activity?.runOnUiThread {
                        distanceTextView.text = ""
                    }
                }

            }
        }

        thread.start()


        val upButton: ImageButton = rootView.findViewById(R.id.button_up)
        val downButton: ImageButton = rootView.findViewById(R.id.button_down)
        val leftButton: ImageButton = rootView.findViewById(R.id.button_left)
        val rightButton: ImageButton = rootView.findViewById(R.id.button_right)
        val stopButton: ImageButton = rootView.findViewById(R.id.button_stop)

        val upleftButton: ImageButton = rootView.findViewById(R.id.button_up_left)
        val uprightButton: ImageButton = rootView.findViewById(R.id.button_up_right)
        val downleftButton: ImageButton = rootView.findViewById(R.id.button_down_left)
        val downrightButton: ImageButton = rootView.findViewById(R.id.button_down_right)

        val moveHeadForwardButton: Button = rootView.findViewById(R.id.move_head_forward)
        val moveHeadRightButton: Button = rootView.findViewById(R.id.move_head_right)
        val moveHeadLeftButton: Button = rootView.findViewById(R.id.move_head_left)

        velocitySeekBar = rootView.findViewById(R.id.velocity_seek_bar)
        headControlSlider = rootView.findViewById(R.id.head_controll_slider)

        displayBacklightSwitch = rootView.findViewById(R.id.display_backlight_switch)
        distanceSensorSwitch = rootView.findViewById(R.id.distance_sensor_switch)
        distanceSensorSwitch.isSelected = true

        distanceSensorSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            run {

                val action: String
                if(BluetoothService.customCharacteristic != null) {
                    action = if(isChecked) {
                        ";DSN,TRUE\n"
                    } else {
                        val mainActivity = activity as MainActivity
                        mainActivity.clearDistance()
                        ";DSN,FALSE\n"
                    }
                    BluetoothService.customCharacteristic?.setValue(action)
                    BluetoothService.bluetoothGatt?.writeCharacteristic(BluetoothService.customCharacteristic)
                } else {
                    Toast.makeText(
                        buttonView?.context,
                        "Najpiew podłącz się do urządzenia bluetooth",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        displayBacklightSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            run {
            val action: String
            if (BluetoothService.customCharacteristic != null) {
                action = if(isChecked) {
                    ";DSB,ON\n"
                } else {
                    ";DSB,OFF\n"
                }
                BluetoothService.customCharacteristic?.setValue(action)
                BluetoothService.bluetoothGatt?.writeCharacteristic(BluetoothService.customCharacteristic)

            } else {
                Toast.makeText(
                    buttonView?.context,
                    "Najpiew podłącz się do urządzenia bluetooth",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }

        }


        headControlSlider.progress = 90
        headControlSlider.max = 180

        moveHeadForwardButton.setOnClickListener{
            headControlSlider.progress = 90
            if(BluetoothService.customCharacteristic != null) {
                val action = ";DST,90\n"
                BluetoothService.customCharacteristic?.setValue(action)
                BluetoothService.bluetoothGatt?.writeCharacteristic(BluetoothService.customCharacteristic)
            }
        }

        moveHeadLeftButton.setOnClickListener{
            headControlSlider.progress = 9
            if(BluetoothService.customCharacteristic != null) {
                val action = ";DST,9\n"
                BluetoothService.customCharacteristic?.setValue(action)
                BluetoothService.bluetoothGatt?.writeCharacteristic(BluetoothService.customCharacteristic)
            }
        }

        moveHeadRightButton.setOnClickListener{
            headControlSlider.progress = 171
            if(BluetoothService.customCharacteristic != null) {
                val action = ";DST,171\n"
                BluetoothService.customCharacteristic?.setValue(action)
                BluetoothService.bluetoothGatt?.writeCharacteristic(BluetoothService.customCharacteristic)
            }
        }


        velocitySeekBar.progress = 100
        velocitySeekBar.incrementProgressBy(10)
        velocitySeekBar.max = 160

        velocitySeekBar.setOnSeekBarChangeListener(VelocitySelectClass())

        headControlSlider.setOnSeekBarChangeListener(DistanceSensorSelectorClass())

        val buttonSound1: Button = rootView.findViewById(R.id.sound_1)
        val buttonSound2: Button = rootView.findViewById(R.id.sound_2)
        val buttonSound3: Button = rootView.findViewById(R.id.sound_3)
        val buttonSound4: Button = rootView.findViewById(R.id.sound_4)

        upButton.setOnClickListener(SteeringClass())
        downButton.setOnClickListener(SteeringClass())
        leftButton.setOnClickListener(SteeringClass())
        rightButton.setOnClickListener(SteeringClass())
        stopButton.setOnClickListener(SteeringClass())
        upleftButton.setOnClickListener(SteeringClass())
        uprightButton.setOnClickListener(SteeringClass())
        downleftButton.setOnClickListener(SteeringClass())
        downrightButton.setOnClickListener(SteeringClass())

        buttonSound1.setOnClickListener(SoundsClass())
        buttonSound2.setOnClickListener(SoundsClass())
        buttonSound3.setOnClickListener(SoundsClass())
        buttonSound4.setOnClickListener(SoundsClass())


        return rootView
    }

    override fun onResume() {
        super.onResume()
        val mainActivity = activity as MainActivity
        if(mainActivity.wasDisconnected) {
            mainActivity.wasDisconnected = false
            setDefaultSettings()
        }

        mainActivity.connect()

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SteeringViewModel::class.java)
    }

    class DistanceSensorSelectorClass: SeekBar.OnSeekBarChangeListener{
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            val action = ";DST,$progress\n"
            if(progress%15 == 0) {
                if (BluetoothService.customCharacteristic != null) {
                    BluetoothService.customCharacteristic?.setValue(action)
                    BluetoothService.bluetoothGatt?.writeCharacteristic(BluetoothService.customCharacteristic)
                }
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
        }
    }

    class VelocitySelectClass: SeekBar.OnSeekBarChangeListener{
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            val action = ";SPD,$progress\n"
            if(progress%20 == 0) {
                if (BluetoothService.customCharacteristic != null) {
                    BluetoothService.customCharacteristic?.setValue(action)
                    BluetoothService.bluetoothGatt?.writeCharacteristic(BluetoothService.customCharacteristic)
                }
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
        }

    }

    class SoundsClass : View.OnClickListener {

        override fun onClick(v: View?) {
            var action = ""


            if(BluetoothService.customCharacteristic != null) {

                when (v?.id) {
                    R.id.sound_1 -> action = ";SND,1\n"
                    R.id.sound_2 -> action = ";SND,2\n"
                    R.id.sound_3 -> action = ";SND,3\n"
                    R.id.sound_4 -> action = ";SND,4\n"
                }

                BluetoothService.customCharacteristic?.setValue(action)
                BluetoothService.bluetoothGatt?.writeCharacteristic(BluetoothService.customCharacteristic)

            }

        }

    }

    class SteeringClass : View.OnClickListener {
        private val UP = ";MOV,UP\n"
        private val RIGHT = ";MOV,RIGHT\n"
        private val DOWN = ";MOV,DOWN\n"
        private val LEFT = ";MOV,LEFT\n"
        private val STOP = ";MOV,STOP\n"

        private val UPRIGHT = ";MOV,UPRIGHT\n"
        private val UPLEFT = ";MOV,UPLEFT\n"
        private val DOWNRIGHT = ";MOV,DOWNRIGHT\n"
        private val DOWNLEFT = ";MOV,DOWNLEFT\n"
        override fun onClick(v: View?) {

            if(BluetoothService.customCharacteristic != null) {

                var action = ""

                when (v?.id) {
                    R.id.button_up -> action = UP
                    R.id.button_right -> action = RIGHT
                    R.id.button_down -> action = DOWN
                    R.id.button_left -> action = LEFT
                    R.id.button_stop -> action = STOP
                    R.id.button_up_right -> action = UPRIGHT
                    R.id.button_up_left -> action = UPLEFT
                    R.id.button_down_right -> action = DOWNRIGHT
                    R.id.button_down_left -> action = DOWNLEFT
                }

                BluetoothService.customCharacteristic?.setValue(action)
                BluetoothService.bluetoothGatt?.writeCharacteristic(BluetoothService.customCharacteristic)

            } else {
                Toast.makeText(v?.context,"Najpiew podłącz się do urządzenia bluetooth",Toast.LENGTH_SHORT).show()
            }
    }

    }


}
