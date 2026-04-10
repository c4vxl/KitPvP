package de.c4vxl.kitpvp.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object TimeUtils {
    /**
     * Returns the current time as a string
     */
    val now: String get() =
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss (yyyy/MM/dd)"))
}