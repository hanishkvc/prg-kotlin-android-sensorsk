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

    fun status(): String {
        if (theSensor == null) {
            return "Sensor not yet selected"
        }
        return "Sensor ${theSensor?.name} selected"
    }

}
