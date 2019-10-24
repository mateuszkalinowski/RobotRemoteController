package pl.mateuszkalinowski.robotremotecontroller.steering

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper.UP

import pl.mateuszkalinowski.robotremotecontroller.R

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

        upButton.setOnClickListener(SteeringClass())
        downButton.setOnClickListener(SteeringClass())
        leftButton.setOnClickListener(SteeringClass())
        rightButton.setOnClickListener(SteeringClass())

        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SteeringViewModel::class.java)
        // TODO: Use the ViewModel
    }


    class SteeringClass : View.OnClickListener {
        private val UP = 0
        private val RIGHT = 1
        private val DOWN = 2
        private val LEFT = 3

        override fun onClick(v: View?) {

            val action: Int

            when (v?.id) {
                R.id.button_up -> action = UP
                R.id.button_right -> action = RIGHT
                R.id.button_down -> action = DOWN
                R.id.button_left -> action = LEFT
            }

           // Toast.makeText(activity,"Bluetooth is not supported",Toast.LENGTH_SHORT).show()

    }

    }


}
