package universe.earth.india.hanishkvc.sensork

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.absoluteValue

data class FDataStats(var sum: Float = 0F, var abssum: Float = 0F, var min: Float = Float.POSITIVE_INFINITY, var max: Float = Float.NEGATIVE_INFINITY) {
    var count: Int = 0

    fun update(value: Float) {
        sum += value
        abssum += value.absoluteValue
        if (value < min) {
            min = value
        }
        if (value > max) {
            max = value
        }
        count += 1
    }

    fun avg(): Float {
        return sum/count
    }

}

class SensorMa(private val sensorsType: Int) {
    private var sensorManager: SensorManager? = null
    var sensorsList: ArrayList<Sensor> = arrayListOf()
    var theSensor: Sensor? = null
    private var eventLog = arrayListOf<String>()
    var savedUpTo: Int = 0
    private var ef0: FDataStats = FDataStats()
    private var ef1: FDataStats = FDataStats()
    private var ef2: FDataStats = FDataStats()

    @JvmName("setSensorManager1")
    fun setSensorManager(sensorManager: SensorManager) {
        this.sensorManager = sensorManager
        this.sensorManager.also { updateSensors() }
    }

    private fun updateSensors() {
        val sensorsListCheck = sensorManager?.getSensorList(this.sensorsType) ?: return
        for (sensor in sensorsListCheck) {
            if (sensor.isWakeUpSensor) {
                continue
            }
            if (sensor.name.contains("uncal", true)) {
                continue
            }
            sensorsList.add(sensor)
        }
    }

    fun setSensor(sensor: Sensor) {
        theSensor = sensor
    }

    /**
     * Start listening to events from either the passed sensor or any previously set sensor
     */
    fun monitorAddSensor(activity: MainActivity, sensor: Sensor? = null) {
        val addSensor = sensor ?: theSensor
        addSensor?.let {
            sensorManager?.registerListener(activity, addSensor, SensorManager.SENSOR_DELAY_NORMAL)
            Log.i(TAG, "SensorMA: Adding Listener for ${it.name}")
        }
    }

    /**
     * Stop listening to events wrt either the passed sensor or any previously set sensor
     */
    fun monitorRemoveSensor(activity: MainActivity, sensor: Sensor? = null) {
        val remSensor = sensor ?: theSensor
        remSensor?.let {
            sensorManager?.unregisterListener(activity, remSensor)
            Log.i(TAG, "SensorMA: Removing Listener for ${it.name}")
        }
    }

    /**
     * Stop listening to sensors fully
     *
     * NOTE: THis assumes that Android will automatically unregister the individual sensors
     * added for listening previously.
     */
    fun monitorStopAll(activity: MainActivity) {
        sensorManager?.unregisterListener(activity)
        Log.i(TAG, "SensorMA: Removing all Listeners")
    }

    fun sensorEvent(se: SensorEvent): String {
        val sName = se.sensor.name.replace(' ', '-')
        var sData = "$sName ${se.timestamp}"
        for ((i,f) in se.values.withIndex()) {
            when(i) {
                0 -> ef0.update(f)
                1 -> ef1.update(f)
                2 -> ef2.update(f)
            }
            sData += " $f"
        }
        eventLog.add(sData)
        return sData
    }

    fun status(): String {
        if (theSensor == null) {
            return "Sensor not yet selected"
        }
        var info = "Sensor ${theSensor?.name} selected\n"
        info += "\tminDelay [${theSensor?.minDelay}], maxDelay [${theSensor?.maxDelay}]\n"
        info += "\tresolution [${theSensor?.resolution}]\n"
        info += "\tmaxRange [${theSensor?.maximumRange}]\n"
        info += "\tpower [${theSensor?.power}]\n"
        info += "\tvendor [${theSensor?.vendor}], version [${theSensor?.version}]\n"
        info += "\n"
        info += "\tDAvg [${ef0.avg()}, ${ef1.avg()}, ${ef2.avg()}]\n"
        info += "\tDMin [${ef0.min}, ${ef1.min}, ${ef2.min}]\n"
        info += "\tDMax [${ef0.max}, ${ef1.max}, ${ef2.max}]\n"
        info += "\tDCount [${ef0.count}, ${ef1.count}, ${ef2.count}]\n"
        return info
    }

    suspend fun save_events(fpath: String) {
        withContext(Dispatchers.IO) {
            val fSave = File(fpath)
            while (true) {
                delay(5000)
                var saveLog = false
                if (savedUpTo < (eventLog.size - 1000)) {
                    saveLog = true
                }
                if (saveLog) {
                    val newUpTo = eventLog.size
                    for (i in savedUpTo..newUpTo) {
                        fSave.appendText(eventLog[i])
                    }
                    savedUpTo = newUpTo
                }
            }
        }
    }

}
