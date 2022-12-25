package universe.earth.india.hanishkvc.sensork

import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var updateStatusCounter by remember {
        mutableStateOf( 0 )
    }
    Column {
        Text(
            text = name,
            fontSize = 20.sp,
            fontStyle = FontStyle.Normal,
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.CenterHorizontally)
        )
        Divider(color = Color.Black)
        if (sensorsMa != null) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .weight(0.6F, true)
                    .verticalScroll(rememberScrollState())
            ) {
                for (item in sensorsMa.sensorsList) {
                    Button(
                        onClick = {
                            sensorsMa.setSensor(item)
                            updateStatusCounter += 1
                        },
                    ) {
                        Text(text=item.name)
                    }
                }
            }
        }
        Divider(color = Color.Black)
        if (updateStatusCounter > 0) {
            sensorsMa?.status()?.let { Text(text = it, modifier = Modifier.weight(0.4f)) }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SensorKTheme {
        MainContent("Android", null)
    }
}