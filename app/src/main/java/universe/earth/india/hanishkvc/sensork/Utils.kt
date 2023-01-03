package universe.earth.india.hanishkvc.sensork

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun sTimeStampHuman(withSeconds: Boolean = false): String {
    val curDateTime = LocalDateTime.now()
    //var sDateTimeFormat = "yyyyMMddzzzHHmm"
    var sDateTimeFormat = "yyyyMMddHHmm"
    if (withSeconds) {
        sDateTimeFormat += "ss"
    }
    return DateTimeFormatter.ofPattern(sDateTimeFormat).format(curDateTime)
}
