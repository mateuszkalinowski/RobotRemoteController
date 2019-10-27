package pl.mateuszkalinowski.robotremotecontroller.steering

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.graphics.Color
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper.UP
import pl.mateuszkalinowski.robotremotecontroller.MainActivity

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

        upButton.setOnClickListener(SteeringClass())
        downButton.setOnClickListener(SteeringClass())
        leftButton.setOnClickListener(SteeringClass())
        rightButton.setOnClickListener(SteeringClass())
        stopButton.setOnClickListener(SteeringClass())
        upleftButton.setOnClickListener(SteeringClass())
        uprightButton.setOnClickListener(SteeringClass())
        downleftButton.setOnClickListener(SteeringClass())
        downrightButton.setOnClickListener(SteeringClass())

        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SteeringViewModel::class.java)
        // TODO: Use the ViewModel
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
