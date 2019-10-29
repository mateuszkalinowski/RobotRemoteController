package pl.mateuszkalinowski.robotremotecontroller.steering

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.Toast

import pl.mateuszkalinowski.robotremotecontroller.R
import pl.mateuszkalinowski.robotremotecontroller.services.BluetoothService

class SteeringFragment : Fragment() {

    companion object {
        fun newInstance() = SteeringFragment()
    }
    private lateinit var viewModel: SteeringViewModel




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val rootView: View =  inflater.inflate(R.layout.steering_fragment, container, false)

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

        val velocitySeekBar: SeekBar = rootView.findViewById(R.id.velocity_seek_bar)
        val headControlSlider: SeekBar = rootView.findViewById(R.id.head_controll_slider)

        headControlSlider.progress = 50
        headControlSlider.max = 100;

        moveHeadForwardButton.setOnClickListener{
            headControlSlider.progress = 50
            if(BluetoothService.customCharacteristic != null) {
                val action = "CMD+DSTSENCENTER\n"
                BluetoothService.customCharacteristic?.setValue(action)
                BluetoothService.bluetoothGatt?.writeCharacteristic(BluetoothService.customCharacteristic)
            }
        }

        moveHeadLeftButton.setOnClickListener{
            headControlSlider.progress = 0
            if(BluetoothService.customCharacteristic != null) {
                val action = "CMD+DSTSENLEFT\n"
                BluetoothService.customCharacteristic?.setValue(action)
                BluetoothService.bluetoothGatt?.writeCharacteristic(BluetoothService.customCharacteristic)
            }
        }

        moveHeadRightButton.setOnClickListener{
            headControlSlider.progress = 100
            if(BluetoothService.customCharacteristic != null) {
                val action = "CMD+DSTSENRIGHT\n"
                BluetoothService.customCharacteristic?.setValue(action)
                BluetoothService.bluetoothGatt?.writeCharacteristic(BluetoothService.customCharacteristic)
            }
        }


        velocitySeekBar.progress = 100;
        velocitySeekBar.incrementProgressBy(10)
        velocitySeekBar.max = 120

        velocitySeekBar.setOnSeekBarChangeListener(VelocitySelectClass())

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


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SteeringViewModel::class.java)
    }

    class VelocitySelectClass: SeekBar.OnSeekBarChangeListener{
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            val action = "CMD+SPEED$progress\n"
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
            var action = "";


            if(BluetoothService.customCharacteristic != null) {

                when (v?.id) {
                    R.id.sound_1 -> action = "CMD+SOUND1\n"
                    R.id.sound_2 -> action = "CMD+SOUND2\n"
                    R.id.sound_3 -> action = "CMD+SOUND3\n"
                    R.id.sound_4 -> action = "CMD+SOUND4\n"
                }

                BluetoothService.customCharacteristic?.setValue(action)
                BluetoothService.bluetoothGatt?.writeCharacteristic(BluetoothService.customCharacteristic)

            }

        }

    }

    class SteeringClass : View.OnClickListener {
        private val UP = "CMD+UP\n"
        private val RIGHT = "CMD+RIGHT\n"
        private val DOWN = "CMD+DOWN\n"
        private val LEFT = "CMD+LEFT\n"
        private val STOP = "CMD+STOP\n"

        private val UPRIGHT = "CMD+UPRIGHT\n"
        private val UPLEFT = "CMD+UPLEFT\n"
        private val DOWNRIGHT = "CMD+DOWNRIGHT\n"
        private val DOWNLEFT = "CMD+DOWNLEFT\n"
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
