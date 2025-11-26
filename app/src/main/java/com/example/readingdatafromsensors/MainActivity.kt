package com.example.readingdatafromsensors

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.readingdatafromsensors.ui.theme.ReadingDataFromSensorsTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        var keepSplashScreen = true

        splashScreen.setKeepOnScreenCondition { keepSplashScreen }

        lifecycleScope.launch {
            delay(3000)
            keepSplashScreen = false
        }
        

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReadingDataFromSensorsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    StartScreen()
                }
                }
            }
        }
    }

class SensorViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    var pressureReadings = mutableStateListOf<Float>()
    var temperatureReadings = mutableStateListOf<Float>()
    var lightReadings = mutableStateListOf<Float>()

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
    private val temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
    private val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)


    init {
        if (pressureSensor != null) {
            sensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }

        if (temperatureSensor != null) {
            sensorManager.registerListener(this, temperatureSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }

        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_PRESSURE -> {
                val newPressure = event.values[0]

                pressureReadings.add(newPressure)

                if (pressureReadings.size > 100)
                {
                    pressureReadings.removeAt(0)
                }
            }

            Sensor.TYPE_AMBIENT_TEMPERATURE -> {
                val newTemperature = event.values[0]

                temperatureReadings.add(newTemperature)

                if (temperatureReadings.size > 100) {
                    temperatureReadings.removeAt(0)
                }
            }

            Sensor.TYPE_LIGHT -> {
                val newLight = event.values[0]

                lightReadings.add(newLight)

                if (lightReadings.size > 100) {
                    lightReadings.removeAt(0)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(this)
    }
}

object Screen {
    const val zeroScreen = "choice_screen"
    const val firstScreen = "first_screen"
    const val secondScreen = "second_screen"
    const val thirdScreen = "third_screen"
}

@Composable
fun StartScreen(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val viewModel : SensorViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.zeroScreen,
        modifier = modifier
    ) {
        composable(route = Screen.zeroScreen) {
            ZeroScreen(
                onNavigateToFirst = { navController.navigate(route = Screen.firstScreen) },
                onNavigateToSecond = { navController.navigate(route = Screen.secondScreen) },
                onNavigateToThird = { navController.navigate(route = Screen.thirdScreen) }
            )
        }
        composable(route = Screen.firstScreen) {
            FirstScreen(
                pressureList = viewModel.pressureReadings,
                onGoBack = {
                    if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
                }
            )
        }

        composable(route = Screen.secondScreen) {
            SecondScreen(
                temperatureList = viewModel.temperatureReadings,
                onGoBack = {
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    }
                }
            )
        }

        composable(Screen.thirdScreen) {
            ThirdScreen(
                lightList = viewModel.lightReadings,
                onGoBack = {
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    }
                }
            )
        }
    }
}

@Composable
fun ZeroScreen(
    onNavigateToFirst: () -> Unit,
    onNavigateToSecond: () -> Unit,
    onNavigateToThird: () -> Unit
) {
    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Choose your sensor: ")
        Button(onClick = onNavigateToFirst) {
            Text("First Sensor")
        }
        Button(onClick = onNavigateToSecond) {
            Text("Second Sensor")
        }
        Button(onClick = onNavigateToThird) {
            Text("Third Sensor")
        }
    }
}

@Composable
fun FirstScreen(
    onGoBack: () -> Unit,
    pressureList: SnapshotStateList<Float>,
) {
    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        val currentPressure = pressureList.lastOrNull() ?: 0f
        Text(text = "Pressure sensor, the current pressure is: $currentPressure hPa")
        Button(onClick = onGoBack) {
            Text("Go Back")
        }
    }
}

@Composable
fun SecondScreen(
    onGoBack: () -> Unit,
    temperatureList: SnapshotStateList<Float>
) {
    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val currentTemperature = temperatureList.lastOrNull() ?: 0f

        Text(text = "Current temperature is: $currentTemperature C")
        Button(onClick = onGoBack) {
            Text("Go Back")
        }
    }
}

@Composable
fun ThirdScreen(
    onGoBack: () -> Unit,
    lightList: SnapshotStateList<Float>
) {
    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val currentLight = lightList.lastOrNull() ?: 0f

        Text(text = "The current light reading is: $currentLight lx")
        Button(onClick = onGoBack) {
            Text("Go Back")
        }
    }
}
