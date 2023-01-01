package universe.earth.india.hanishkvc.sensork

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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

class LocationMa {
    private var permissionOk: Boolean = false
    private var locationManager: LocationManager? = null
    var locationLog = arrayListOf<String>()
    private var mutex = Mutex()

    fun checkPermissionStatus(mainActivity: MainActivity) {
        val locPerm = mainActivity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        permissionOk = locPerm == PackageManager.PERMISSION_GRANTED
    }

    fun requestLocations(mainActivity: MainActivity) {
        locationManager = mainActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var msg = "LocationMa:Not Ok"
        locationManager?.let {
            try {
                it.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1.0F, mainActivity)
                msg = "LocationMa:GpsProvider:Success: location updates request"
            } catch (e: SecurityException) {
                msg = "LocationMa:GpsProvider:Failed: location updates request"
            }
        }
        Log.i(TAG, msg)
        Toast.makeText(mainActivity, msg, Toast.LENGTH_SHORT).show()
    }

    fun cancelLocations(mainActivity: MainActivity) {
        locationManager?.let {
            it.removeUpdates(mainActivity)
            Log.i(TAG, "LocationMa:GpsProvider: stop location updates")
        }
    }

    suspend fun locationEvent(location: Location): String {
        val sData = "GpsProvider ${location.time} ${location.latitude} ${location.longitude} ${location.altitude}"
        mutex.withLock {
            locationLog.add(sData)
        }
        Log.i(TAG, "LocationEvent: $sData")
        return sData
    }

    suspend fun getTextDataAndClear(): String {
        var sData = ""
        val numEntries = locationLog.size
        for(i in 0 until numEntries) {
            sData += "${locationLog[i]}\n"
        }
        mutex.withLock {
            for(i in 0 until numEntries) {
                locationLog.removeAt(0)
            }
        }
        return sData
    }

}

class SensorsMa(private val sensorsType: Int) {
    private var sensorManager: SensorManager? = null
    var sensorsList: ArrayList<Sensor> = arrayListOf()
    var theSensor: Sensor? = null
    private var elMutex: Mutex = Mutex()
    private var eventLog = arrayListOf<String>()
    private var savedUpTo: Int = 0
    private var ef0: FDataStats = FDataStats()
    private var ef1: FDataStats = FDataStats()
    private var ef2: FDataStats = FDataStats()
    var locationMa: LocationMa = LocationMa()

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

    suspend fun sensorEvent(se: SensorEvent): String {
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
        elMutex.withLock { eventLog.add(sData) }
        Log.i(TAG, "SensorEvent: $sData")
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
        info += "\n"
        if (locationMa.locationLog.size > 0) {
            info += "\tLoc [${locationMa.locationLog.last()}]\n"
        }
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
                    for (i in savedUpTo until newUpTo) {
                        fSave.appendText(eventLog[i]+"\n")
                    }
                    elMutex.withLock {
                        for(i in 0 until newUpTo) {
                            eventLog.removeAt(0)
                        }
                    }
                    savedUpTo = 0
                }
                val sLocationData = locationMa.getTextDataAndClear()
                fSave.appendText(sLocationData)
            }
        }
    }

}