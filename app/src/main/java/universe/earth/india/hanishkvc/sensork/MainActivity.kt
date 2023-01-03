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
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import universe.earth.india.hanishkvc.sensork.ui.theme.SensorKTheme
import java.util.*
import kotlin.concurrent.schedule
import kotlin.concurrent.timerTask
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

const val TAG = "SensorK"
const val HEADING_SENSORS = "Sensors"


class MainActivity : ComponentActivity(), SensorEventListener, LocationListener {
    val bMultipleSensors: Boolean = false
    private lateinit var sensorsMa: SensorsMa
    lateinit var refreshMe: MutableState<Int>
    var windowHeight: Int = 800
    var uiNavPos: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        refreshMe = mutableStateOf(0)
        sensorsMa = SensorsMa(Sensor.TYPE_ALL)
        sensorsMa.setSensorManager(getSystemService(SENSOR_SERVICE) as SensorManager)
        val fileId = sTimeStampHuman()
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
        val mainActivity = this
        onBackPressedDispatcher.addCallback(this) {
            mainActivity.uiNavPos -= 1
            if (sensorsMa.sensorMa == null) {
                if (mainActivity.uiNavPos <= -2) {
                    finish()
                } else {
                    Toast.makeText(mainActivity, "Press back again, if you want to quit...", Toast.LENGTH_SHORT).show()
                }
            }
            sensorsMa.clearSensorMa(mainActivity)
            refreshMe.value += 1
        }
        windowHeight = windowManager.defaultDisplay.width
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
    sensorsMa.setSensorMa(selSensor, mainActivity)
    mainActivity?.let {
        it.refreshMe.value += 1
    }
}

fun testCanvasDraw(ds: DrawScope) {
    val yMid = 0F
    with(ds) {
        drawLine(Color.Red, start=Offset(0F,yMid), end=Offset(10F,yMid+200))
        drawLine(Color.Red, start=Offset(10F,yMid+200), end=Offset(20F,yMid-300))
        drawLine(Color.Red, start=Offset(20F,yMid-300), end=Offset(30F,yMid+100))
        drawLine(Color.Red, start=Offset(30F,yMid+100), end=Offset(40F,yMid-200))

        drawLine(Color.Red, start=Offset(100F,yMid), end=Offset(100F,yMid+200))
        drawLine(Color.Red, start=Offset(110F,yMid), end=Offset(110F,yMid-300))
        drawLine(Color.Red, start=Offset(200F,yMid), end=Offset(200F,yMid+100))
        drawLine(Color.Red, start=Offset(300F,yMid), end=Offset(300F,yMid-200))
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun PlotData(sensorsMa: SensorsMa?, mainActivity: MainActivity?, columnScope: ColumnScope) {
    sensorsMa?.sensorMa ?: return
    mainActivity ?: return
    Log.d(TAG, "Canvas:ParentPlotData")
    val textMeasure = rememberTextMeasurer()
    var canvasRefresh = remember { mutableStateOf(1) }
    var canvasFullScreen = remember { mutableStateOf(false) }
    LaunchedEffect(mainActivity.refreshMe) {
        while (true) {
            delay(1000)
            val eventFLog = sensorsMa.sensorMa!!.updateEventFLogBackup()
            canvasRefresh.value += 1
            Log.d(TAG, "Canvas:Helper:cR${canvasRefresh.value}: sensorEvents list size ${eventFLog.size}")
        }
    }
    var canvasModifier = if (canvasFullScreen.value) {
        Modifier.fillMaxSize()
    } else {
        Modifier
            .fillMaxWidth()
            .height(mainActivity.windowHeight.times(0.33).dp)
    }
    if (canvasRefresh.value > 0){
        Canvas(
            modifier = canvasModifier.pointerInput(Unit) {
                detectTapGestures (
                    onDoubleTap = { canvasFullScreen.value = !canvasFullScreen.value }
                )
            }
        ) {
            Log.d(TAG, "Canvas:${canvasRefresh.value}: $size")
            val eventFLog = sensorsMa.sensorMa!!.eventFLogBackup
            eventFLog.let {
                //Log.d(TAG, "Canvas: eventFLog.size ${eventFLog.size}")
                val canvasHeight = size.height
                val yMid = canvasHeight/2F
                drawText(textMeasure, sensorsMa.sensorMa!!.theSensor.name)
                val (min,max) = sensorsMa.sensorMa!!.getSEValuesMinMax()
                val dataHeight = (max - min)*1.4F
                val dataWidth = eventFLog.size*1.1F
                val scaleX = size.width/dataWidth
                withTransform({
                    scale(scaleX = 1F, scaleY = canvasHeight/dataHeight)
                    translate(top = yMid)
                }) {
                    for(i in 1 until it.size) {
                        val fva2 = it[i]
                        val fva1 = it[i-1]
                        val fx2 = i.toFloat() * scaleX
                        val fx1 = (i-1).toFloat() * scaleX
                        for(j in fva2.indices) {
                            val fy2 = fva2[j]
                            val fy1 = fva1[j]
                            val color = when (j) {
                                0 -> Color.Red
                                1 -> Color.Green
                                2 -> Color.Blue
                                else -> Color.Black
                            }
                            drawLine(color, start = Offset(x=fx1, y=fy1), end = Offset(x=fx2, y=fy2))
                        }
                    }
                }
            }
        }
        //testCanvasDraw(ds = this)
        ShowTextStatus(sensorsMa, columnScope)
    }
}

@Composable
fun ShowTextStatus(sensorsMa: SensorsMa?, columnScope: ColumnScope) {
    with(columnScope) {
        sensorsMa?.status()?.let {
            Text(
                text = it,
                modifier = Modifier
                    .weight(0.33f)
                    .verticalScroll(rememberScrollState())
            )
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
    if (refreshMe.value < 0)  return
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
                    .weight(0.33F, true)
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                for (item in sensorsMa.sensorsList) {
                    Button(
                        onClick = {
                            handleSensorSelection(mainActivity, sensorsMa, item)
                            updateStatusCounter += 1
                            mainActivity?.uiNavPos = 1
                        },
                    ) {
                        Text(text=item.name)
                    }
                }
            }
        }
        Divider(color = Color.Black)
        PlotData(sensorsMa, mainActivity, this)
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SensorKTheme {
        MainContent("Android", null, null)
    }
}
