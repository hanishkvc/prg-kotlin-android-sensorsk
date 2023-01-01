package universe.earth.india.hanishkvc.sensork

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import universe.earth.india.hanishkvc.sensork.ui.theme.SensorKTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.concurrent.schedule
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

const val TAG = "SensorK"
const val HEADING_SENSORS = "Sensors"

class MainActivity : ComponentActivity(), SensorEventListener, LocationListener {
    val bMultipleSensors: Boolean = false
    private lateinit var sensorsMa: SensorsMa
    lateinit var refreshMe: MutableState<Int>
    var timerTask: TimerTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        refreshMe = mutableStateOf(0)
        sensorsMa = SensorsMa(Sensor.TYPE_ALL)
        sensorsMa.setSensorManager(getSystemService(SENSOR_SERVICE) as SensorManager)
        val curDateTime = LocalDateTime.now()
        val fileId = DateTimeFormatter.ofPattern("yyyyMMddHHmm").format(curDateTime)
        getExternalFilesDir(null)?.let {
            val fPath = Path(it.absolutePath,"events.$fileId.csv.txt")
            val fAPath = fPath.absolutePathString()
            Log.i(TAG, "Save events to $fAPath")
            Toast.makeText(this, "SaveEventsTo: $fAPath", Toast.LENGTH_SHORT).show()
            lifecycleScope.launch(Dispatchers.IO) {
                sensorsMa.saveEventsLoop(fAPath)
            }
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        sensorsMa.locationMa.checkPermissionStatus(this)
        sensorsMa.locationMa.requestLocations(this)
    }

    override fun onSensorChanged(se: SensorEvent?) {
        se ?: return
        lifecycleScope.launch {
            sensorsMa.sensorMa?.sensorEvent(se)
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        Log.i(TAG, "onAccuracyChanged: ")
    }

    override fun onStart() {
        super.onStart()
        Log.w(TAG, "OnStart called")
        sensorsMa.sensorMa?.let {
            sensorsMa.monitorAddSensor(this)
        }
        setContent {
            SensorKTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    DrawMainContent(HEADING_SENSORS, sensorsMa, this, refreshMe)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        Log.w(TAG, "OnStop called")
        sensorsMa.monitorStopAll(this)
        sensorsMa.locationMa.cancelLocations(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.w(TAG, "OnSaveInstanceState called")
        sensorsMa.sensorMa?.let {
            outState.putString("sensor_name", it.theSensor.name)
            Log.w(TAG, "OnSaveInstanceState: Saving ${it.theSensor.name}")
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        Log.w(TAG, "OnRestoreInstanceState called $savedInstanceState")
        savedInstanceState ?: return
        val sensorName = savedInstanceState.getString("sensor_name") ?: return
        var selSensor: Sensor? = null
        for (curSensor in sensorsMa.sensorsList) {
            if (curSensor.name == sensorName) {
                selSensor = curSensor
            }
        }
        selSensor?.let {
            handleSensorSelection(this, sensorsMa, selSensor)
            refreshMe.value += 1
            Log.i(TAG, "Restoring: Previously selected sensor $selSensor")
        }
    }

    override fun onLocationChanged(location: Location) {
        lifecycleScope.launch {
            sensorsMa.locationMa.locationEvent(location)
        }
    }

}

fun handleSensorSelection(mainActivity: MainActivity?, sensorsMa: SensorsMa, selSensor: Sensor?) {
    if (selSensor == null) return
    sensorsMa.setSensor(selSensor, mainActivity)
    mainActivity?.let {
        if (it.timerTask == null) {
            it.timerTask = Timer().schedule(5000,5000) {
                it.refreshMe.value += 1
            }
        }
    }
}

@Composable
fun DrawMainContent(
    name: String,
    sensorsMa: SensorsMa?,
    mainActivity: MainActivity?,
    refreshMe: MutableState<Int>
) {
    if (refreshMe.value < 0) return
    MainContent(name, sensorsMa, mainActivity)
}

@Composable
fun MainContent(
    name: String,
    sensorsMa: SensorsMa?,
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
                    .weight(0.5F, true)
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
            sensorsMa?.status()?.let { Text(text = it, modifier = Modifier.weight(0.5f)) }
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