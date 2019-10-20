package pl.mateuszkalinowski.robotremotecontroller

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

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
    }
}
