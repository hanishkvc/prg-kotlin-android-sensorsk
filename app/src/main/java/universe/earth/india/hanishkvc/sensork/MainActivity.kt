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
import java.util.*
import kotlin.concurrent.*

const val TAG = "SensorK"
const val HEADING_SENSORS = "Sensors"

class MainActivity : ComponentActivity(), SensorEventListener {
    val bMultipleSensors: Boolean = false
    lateinit var sensorMa: SensorMa
    lateinit var refreshMe: MutableState<Int>
    var timerTask: TimerTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        refreshMe = mutableStateOf(0)
        sensorMa = SensorMa(Sensor.TYPE_ALL)
        sensorMa.setSensorManager(getSystemService(SENSOR_SERVICE) as SensorManager)
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
        Log.w(TAG, "OnStart called")
        sensorMa.theSensor?.let {
            sensorMa.monitorAddSensor(this)
        }
        setContent {
            SensorKTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    DrawMainContent(HEADING_SENSORS, sensorMa, this, refreshMe)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        Log.w(TAG, "OnStop called")
        sensorMa.monitorStopAll(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.w(TAG, "OnSaveInstanceState called")
        sensorMa.theSensor?.let {
            outState.putString("sensor_name", it.name)
            Log.w(TAG, "OnSaveInstanceState: Saving ${it.name}")
        }
    }

    @Composable
    fun refreshGUI() {
        currentComposer.composition.recompose()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        Log.w(TAG, "OnRestoreInstanceState called $savedInstanceState")
        savedInstanceState ?: return
        val sensorName = savedInstanceState.getString("sensor_name") ?: return
        var selSensor: Sensor? = null
        for (curSensor in sensorMa.sensorsList) {
            if (curSensor.name == sensorName) {
                selSensor = curSensor
            }
        }
        selSensor?.let {
            handleSensorSelection(this, sensorMa, selSensor)
            refreshMe.value += 1
            Log.i(TAG, "Restoring: Previously selected sensor $selSensor")
        }
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
            if (it.timerTask == null) {
                it.timerTask = Timer().schedule(5000,5000) {
                    it.refreshMe.value += 1
                }
            }
        }
    }
}

@Composable
fun DrawMainContent(
    name: String,
    sensorsMa: SensorMa?,
    mainActivity: MainActivity?,
    refreshMe: MutableState<Int>
) {
    if (refreshMe.value < 0) return
    MainContent(name, sensorsMa, mainActivity)
}

@Composable
fun MainContent(
    name: String,
    sensorsMa: SensorMa?,
    mainActivity: MainActivity?,
) {
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