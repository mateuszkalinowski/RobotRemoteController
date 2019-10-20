package pl.mateuszkalinowski.robotremotecontroller.steering

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

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
        return inflater.inflate(R.layout.steering_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SteeringViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
