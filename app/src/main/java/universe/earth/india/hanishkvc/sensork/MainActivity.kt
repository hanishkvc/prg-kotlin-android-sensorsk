package universe.earth.india.hanishkvc.sensork

import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import universe.earth.india.hanishkvc.sensork.ui.theme.SensorKTheme

class MainActivity : ComponentActivity() {
    lateinit var sensorMa: SensorMa

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorMa = SensorMa(Sensor.TYPE_ALL)
        sensorMa.setSensorManager(getSystemService(SENSOR_SERVICE) as SensorManager)
        setContent {
            SensorKTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    MainContent("Sensors", sensorMa)
                }
            }
        }
    }
}

@Composable
fun MainContent(name: String, sensorsMa: SensorMa?) {
    Column {
        Text(text = name, modifier = Modifier.padding(4.dp, 20.dp).align(Alignment.CenterHorizontally))
        Divider(color = Color.Black)
        var sList = "${name}:"
        if (sensorsMa != null) {
            Column {
                for (item in sensorsMa.sensorsList) {
                    sList += "\n ${item.name}"
                    Button(
                        onClick = {

                        },
                        modifier = Modifier.padding(10.dp, 2.dp)
                    ) {
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