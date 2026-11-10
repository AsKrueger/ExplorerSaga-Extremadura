package com.alonso.explorersaga.data.content

import java.time.LocalDate
import java.time.MonthDay

/** Ventana anual "MM-DD" a "MM-DD"; puede cruzar el fin de año (nov → feb). */
data class SeasonWindow(val from: MonthDay, val to: MonthDay) {

    fun contains(date: LocalDate): Boolean {
        val day = MonthDay.from(date)
        return if (!from.isAfter(to)) {
            !day.isBefore(from) && !day.isAfter(to)
        } else {
            !day.isBefore(from) || !day.isAfter(to)
        }
    }

    /** Días que faltan para que empiece (0 si ya está en curso). */
    fun daysUntil(date: LocalDate): Long {
        if (contains(date)) return 0
        var start = from.atYear(date.year)
        if (start.isBefore(date)) start = from.atYear(date.year + 1)
        return java.time.temporal.ChronoUnit.DAYS.between(date, start)
    }

    companion object {
        fun parse(from: String, to: String): SeasonWindow =
            SeasonWindow(MonthDay.parse("--$from"), MonthDay.parse("--$to"))
    }
}
