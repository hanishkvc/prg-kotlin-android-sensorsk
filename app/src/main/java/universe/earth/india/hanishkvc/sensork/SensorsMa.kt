package universe.earth.india.hanishkvc.sensork

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.absoluteValue

const val SAVE_MIN_RECORDS = 1000
const val SAVE_CHECK_TIME = 10000L

data class FDataStats(var sum: Float = 0F, var abssum: Float = 0F, var min: Float = Float.POSITIVE_INFINITY, var max: Float = Float.NEGATIVE_INFINITY) {
    var count: Int = 0
    private var prevValue: Double = Double.NaN

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

    fun updateAbs2Rel(value: Double) {
        if (prevValue.isNaN()) {
            prevValue = value
        } else {
            val delta = value - prevValue
            update(delta.toFloat())
            prevValue = value
        }
    }

    fun avg(): Float {
        return sum/count
    }

}

class LocationMa {
    private var permissionOk: Boolean = false
    private var locationManager: LocationManager? = null
    private var locationLog = arrayListOf<String>()
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

    fun status(): String {
        if (locationLog.size > 0) {
            return "\tLoc [${locationLog.last()}]\n"
        }
        return ""
    }

}

const val DIV_NANO2MILLI = 1000000
class SensorMa(val theSensor: Sensor) {
    private var elMutex: Mutex = Mutex()
    private var eventFLog = arrayListOf<FloatArray>()
    var eventFLogBackup = arrayListOf<FloatArray>()
    private var eventLog = arrayListOf<String>()
    private var seValues = arrayListOf<FDataStats>()
    private val bootEpochTime = System.currentTimeMillis() - (SystemClock.elapsedRealtimeNanos()/DIV_NANO2MILLI)
    private var timeStats = FDataStats()

    init {
        Log.i(TAG, "SensorMa:Init: CurEpochTime:${System.currentTimeMillis()}, CurElapsedTime:${SystemClock.elapsedRealtimeNanos()/DIV_NANO2MILLI}")
        Log.i(TAG, "SensorMa:Init: BootEpochTime:$bootEpochTime")
    }

    private fun seValuesUpdate(index: Int, value: Float) {
        if (index >= seValues.size) {
            for(i in seValues.size until index+1) {
                seValues.add(FDataStats())
            }
        }
        seValues[index].update(value)
    }

    fun getSEValuesMinMax(): Pair<Float, Float> {
        var min = Float.POSITIVE_INFINITY
        var max = Float.NEGATIVE_INFINITY
        for (sev in seValues) {
            if (min > sev.min) {
                min = sev.min
            }
            if (max < sev.max) {
                max = sev.max
            }
        }
        return Pair(min, max)
    }

    suspend fun sensorEvent(se: SensorEvent): String {
        val sName = se.sensor.name.replace(' ', '-')
        val curEpochTime = bootEpochTime + (se.timestamp/DIV_NANO2MILLI)
        timeStats.updateAbs2Rel(curEpochTime.toDouble())
        var sData = "$sName $curEpochTime"
        for ((i,f) in se.values.withIndex()) {
            seValuesUpdate(i, f)
            sData += " $f"
        }
        elMutex.withLock {
            eventLog.add(sData)
            eventFLog.add(se.values.clone())
        }
        Log.i(TAG, "SensorEvent: $sData")
        return sData
    }

    fun status(): String {
        var info = "Sensor ${theSensor.name} selected\n"
        info += "\tminDelay [${theSensor.minDelay}], maxDelay [${theSensor.maxDelay}]\n"
        info += "\tresolution [${theSensor.resolution}]\n"
        info += "\tmaxRange [${theSensor.maximumRange}]\n"
        info += "\tpower [${theSensor.power}]\n"
        info += "\tvendor [${theSensor.vendor}], version [${theSensor.version}]\n"
        info += "\n"
        var sMin = "DMin"
        var sAvg = "DAvg"
        var sMax = "DMax"
        var sCnt = "DCnt"
        for(i in 0 until seValues.size) {
            sMin += " ${seValues[i].min}"
            sAvg += " ${seValues[i].avg()}"
            sMax += " ${seValues[i].max}"
            sCnt += " ${seValues[i].count}"
        }
        info += "\t$sMin\n"
        info += "\t$sAvg\n"
        info += "\t$sMax\n"
        info += "\t$sCnt\n"
        info += "\tTime ${timeStats.min} ${timeStats.avg()} ${timeStats.max}\n"
        return info
    }

    suspend fun getTextDataAndClear(minRecords: Int = 1000): String {
        if (eventLog.size < minRecords) {
            return ""
        }
        var sData = ""
        val numEntries = eventLog.size
        for(i in 0 until numEntries) {
            sData += "${eventLog[i]}\n"
        }
        elMutex.withLock {
            for(i in 0 until numEntries) {
                eventLog.removeAt(0)
                eventFLog.removeAt(0)
            }
        }
        return sData
    }

    suspend fun updateEventFLogBackup(): ArrayList<FloatArray> {
        elMutex.withLock {
            eventFLogBackup = arrayListOf<FloatArray>()
            for (sev in eventFLog) {
                eventFLogBackup.add(sev.clone())
            }
        }
        return eventFLogBackup
    }

}

class SensorsMa(private val sensorsType: Int) {
    private var sensorManager: SensorManager? = null
    var sensorsList: ArrayList<Sensor> = arrayListOf()
    var sensorMa: SensorMa? = null
    var locationMa: LocationMa = LocationMa()
    private var lastSaveTimeStamp: String = ""
    private var fSave: File? = null
    private var saveMutex = Mutex()

    @JvmName("setSensorManager1")
    fun setSensorManager(sensorManager: SensorManager) {
        this.sensorManager = sensorManager
        this.sensorManager.also { updateSensorsList() }
    }

    private fun updateSensorsList() {
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

    suspend fun setSensorMa(sensor: Sensor, mainActivity: MainActivity?) {
        if (mainActivity == null) return
        sensorMa?.let {
            if (it.theSensor.name != sensor.name) {
                if (!mainActivity.bMultipleSensors) {
                    Toast.makeText(mainActivity, "Removing sensor ${it.theSensor.name}", Toast.LENGTH_SHORT).show()
                    monitorRemoveSensor(mainActivity)
                }
            }
        }
        sensorMa = SensorMa(sensor)
        monitorAddSensor(mainActivity)
        Toast.makeText(mainActivity, "Added sensor ${sensorMa!!.theSensor.name}", Toast.LENGTH_SHORT).show()
    }

    suspend fun clearSensorMa(mainActivity: MainActivity) {
        sensorMa?.let {
            Toast.makeText(mainActivity, "Removing sensor ${it.theSensor.name}", Toast.LENGTH_SHORT).show()
            monitorRemoveSensor(mainActivity)
            sensorMa = null
        }
    }

    /**
     * Start listening to events from either the passed sensor or any previously set sensor
     */
    fun monitorAddSensor(activity: MainActivity, sensor: Sensor? = null) {
        val addSensor = sensor ?: sensorMa?.theSensor
        addSensor?.let {
            sensorManager?.registerListener(activity, addSensor, SensorManager.SENSOR_DELAY_NORMAL)
            Log.i(TAG, "SensorsMA: Adding Listener for ${it.name}")
        }
    }

    /**
     * Stop listening to events wrt either the passed sensor or any previously set sensor
     */
    private suspend fun monitorRemoveSensor(activity: MainActivity, sensor: Sensor? = null) {
        val remSensor = sensor ?: sensorMa?.theSensor
        remSensor?.let {
            sensorManager?.unregisterListener(activity, remSensor)
            Log.i(TAG, "SensorsMA: Removing Listener for ${it.name}")
            saveEvents(1)
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
        Log.i(TAG, "SensorsMA: Removing all Listeners")
    }


    fun status(): String {
        if (sensorMa == null) {
            return "Sensor not yet selected"
        }
        var info = ""
        info += sensorMa?.status()
        info += "LastSave: $lastSaveTimeStamp\n"
        info += locationMa.status()
        return info
    }

    private suspend fun saveEvents(saveMinRecords:Int = SAVE_MIN_RECORDS) {
        fSave ?: return
        saveMutex.withLock {
            sensorMa?.let {
                val sSensorData = it.getTextDataAndClear(saveMinRecords)
                if (sSensorData.isNotEmpty()) {
                    fSave!!.appendText(sSensorData)
                    lastSaveTimeStamp = sTimeStampHuman(true)
                }
            }
            val sLocationData = locationMa.getTextDataAndClear()
            fSave!!.appendText(sLocationData)
        }
    }

    suspend fun saveEventsLoop(fPath: String) {
        withContext(Dispatchers.IO) {
            fSave = File(fPath)
            while (true) {
                delay(SAVE_CHECK_TIME)
                saveEvents()
            }
        }
    }

}
