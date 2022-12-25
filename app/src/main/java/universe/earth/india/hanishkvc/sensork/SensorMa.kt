package universe.earth.india.hanishkvc.sensork

import android.hardware.Sensor
import android.hardware.SensorManager

class SensorMa(private val sensorsType: Int) {
    private var sensorManager: SensorManager? = null
    var sensorsList: ArrayList<Sensor> = arrayListOf()
    var theSensor: Sensor? = null

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

    fun monitorSensor(activity: MainActivity) {
        sensorManager?.registerListener(activity, theSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun monitorStop(activity: MainActivity) {
        sensorManager?.unregisterListener(activity)
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
        return info
    }

}
