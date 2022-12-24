package universe.earth.india.hanishkvc.sensork

import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import universe.earth.india.hanishkvc.sensork.ui.theme.SensorKTheme

class MainActivity : ComponentActivity() {
    var sensorManager: SensorManager? = null
    var sensorsList: List<Sensor>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorManager.also {
            sensorsList = it?.getSensorList(Sensor.TYPE_ALL)
        }
        setContent {
            SensorKTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    MainContent("Sensors", this.sensorsList)
                }
            }
        }
    }
}

@Composable
fun MainContent(name: String, sensorsList: List<Sensor>?) {
    Column() {
        Text(text = name, modifier = Modifier.padding(4.dp, 20.dp).align(Alignment.CenterHorizontally))
        Divider(color = Color.Black)
        var sList = "${name}:"
        if (sensorsList != null) {
            Column() {
                for (item in sensorsList) {
                    if (item.name.contains("uncal", true)) {
                        continue
                    }
                    if (item.isWakeUpSensor) {
                        continue
                    }
                    sList += "\n ${item.name}"
                    Surface(modifier = Modifier.padding(10.dp, 2.dp) ) {
                        Text(text=item.name)
                    }
                }
            }
        }
        Divider(color = Color.Black)
        //Text(text = sList)
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SensorKTheme {
        MainContent("Android", null)
    }
}