package universe.earth.india.hanishkvc.sensork

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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

const val TAG = "SensorK"

class MainActivity : ComponentActivity(), SensorEventListener {
    val bMultipleSensors: Boolean = false
    lateinit var sensorMa: SensorMa

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onSensorChanged(se: SensorEvent?) {
        se ?: return
        val sData = sensorMa.sensorEvent(se)
        Log.i(TAG, "onSensorChanged: $sData")
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        Log.i(TAG, "onAccuracyChanged: ")
    }

    override fun onStart() {
        super.onStart()
        sensorMa = SensorMa(Sensor.TYPE_ALL)
        sensorMa.setSensorManager(getSystemService(SENSOR_SERVICE) as SensorManager)
        setContent {
            SensorKTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    MainContent("Sensors", sensorMa, this)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        sensorMa.monitorStopAll(this)
    }

}

fun handleSensorSelection(mainActivity: MainActivity?, sensorsMa: SensorMa, selSensor: Sensor?) {
    if (selSensor == null) return
    if (sensorsMa.theSensor != selSensor) {
        mainActivity?.let {
            if (!mainActivity.bMultipleSensors) {
                Toast.makeText(mainActivity, "Removing sensor ${sensorsMa.theSensor?.name}", Toast.LENGTH_SHORT).show()
                sensorsMa.monitorRemoveSensor(mainActivity)
            }
        }
        sensorsMa.setSensor(selSensor)
        mainActivity?.let {
            sensorsMa.monitorAddSensor(mainActivity)
            Toast.makeText(mainActivity, "Added sensor ${sensorsMa.theSensor?.name}", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun MainContent(name: String, sensorsMa: SensorMa?, mainActivity: MainActivity?) {
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
                    .fillMaxWidth()
            ) {
                for (item in sensorsMa.sensorsList) {
                    Button(
                        onClick = {
                            handleSensorSelection(mainActivity, sensorsMa, item)
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
        MainContent("Android", null, null)
    }
}