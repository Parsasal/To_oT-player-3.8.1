package com.example.util

/**
 * High-precision Solar Hijri (Shamsi) Calendar Conversion Utility.
 * Converts Gregorian dates to the exact Persian calendar format strictly as "yyyy/mm/dd"
 * (e.g., "1405/06/03" with two-digit zero padding).
 */
object PersianDateHelper {

    /**
     * Converts Gregorian year, month (1..12), and day (1..31) to Solar Hijri (Jalali).
     * @return Triple(shamsiYear, shamsiMonth, shamsiDay)
     */
    fun gregorianToJalali(gYear: Int, gMonth: Int, gDay: Int): Triple<Int, Int, Int> {
        val gDaysInMonth = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        val jDaysInMonth = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)

        val gy = gYear - 1600
        val gm = gMonth - 1
        val gd = gDay - 1

        var gDayNo = 365 * gy + ((gy + 3) / 4) - ((gy + 99) / 100) + ((gy + 399) / 400)
        for (i in 0 until gm) {
            gDayNo += gDaysInMonth[i]
        }
        if (gm > 1 && ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0))) {
            gDayNo++
        }
        gDayNo += gd

        var jDayNo = gDayNo - 79
        val jNp = jDayNo / 12053
        jDayNo %= 12053

        var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461

        if (jDayNo >= 366) {
            jy += ((jDayNo - 1) / 365)
            jDayNo = (jDayNo - 1) % 365
        }

        var jm = 0
        for (i in 0 until 11) {
            if (jDayNo < jDaysInMonth[i]) {
                jm = i
                break
            }
            jDayNo -= jDaysInMonth[i]
            jm = i + 1
        }
        val jd = jDayNo + 1
        return Triple(jy, jm + 1, jd)
    }

    /**
     * Formats any epoch timestamp strictly as "yyyy/mm/dd" in Solar Hijri (Shamsi) format.
     * Example output: "1405/06/03"
     */
    fun formatShamsiDate(timestamp: Long = System.currentTimeMillis()): String {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = timestamp
        val gy = calendar.get(java.util.Calendar.YEAR)
        val gm = calendar.get(java.util.Calendar.MONTH) + 1
        val gd = calendar.get(java.util.Calendar.DAY_OF_MONTH)

        val (jy, jm, jd) = gregorianToJalali(gy, gm, gd)
        return String.format(java.util.Locale.US, "%04d/%02d/%02d", jy, jm, jd)
    }

    /**
     * Formats date according to active language:
     * - Persian mode: strict "yyyy/mm/dd" Shamsi (e.g. "1405/06/03")
     * - International mode: "yyyy/mm/dd" Gregorian (e.g. "2026/08/25")
     */
    fun formatLocalizedDate(timestamp: Long = System.currentTimeMillis(), isPersian: Boolean = true): String {
        return if (isPersian) {
            formatShamsiDate(timestamp)
        } else {
            val calendar = java.util.Calendar.getInstance()
            calendar.timeInMillis = timestamp
            val gy = calendar.get(java.util.Calendar.YEAR)
            val gm = calendar.get(java.util.Calendar.MONTH) + 1
            val gd = calendar.get(java.util.Calendar.DAY_OF_MONTH)
            String.format(java.util.Locale.US, "%04d/%02d/%02d", gy, gm, gd)
        }
    }
}
