package io.glnt.gpms.common.utils

import io.glnt.gpms.model.enums.DiscountRangeType
import io.glnt.gpms.model.enums.WeekType
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

object DateUtil {

    private const val DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss"

    val nowDateTime: String
        get() {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            return sdf.format(Date())
        }

    val nowDateTimeHm: String
        get() {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm")
            return sdf.format(Date())
        }

    val nowDate: String
        get() {
            val sdf = SimpleDateFormat("yyyy-MM-dd")
            return sdf.format(Date())
        }

    val nowTime: String
        get() {
            val sdf = SimpleDateFormat("HH:mm:ss")
            return sdf.format(Date())
        }

    val nowTimeDetail: String
        get() {
            val sdf = SimpleDateFormat("HH:mm:ss.SSS")
            return sdf.format(Date())
        }

    fun getFormatTime(format: String=""): String {
        val ft: String = format
        val sdf = if (!ft.isEmpty()) SimpleDateFormat("format")
        else SimpleDateFormat("yyyyMMddHHmmss")
        return sdf.format(Date())
    }

    fun stringToNowDateTime() : String {
        val sdf = SimpleDateFormat("yyyyMMddHHmmss")
        return sdf.format(Date())
    }

    fun stringToNowDateTimeMS() : String {
        val sdf = SimpleDateFormat("yyyyMMddHHmmssSSS")
        return sdf.format(Date())
    }

    fun stringToLocalDateTime(dateString: String="", pettern: String="yyyyMMddHHmmss") : LocalDateTime {
        return LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern(pettern))
    }

    fun stringToLocalDate(dateString: String="") : LocalDate {
        return LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }

    fun LocalDateTimeToDateString(date: LocalDateTime) : String {
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }

    fun formatDateTime(dateTime: LocalDateTime, timePattern: String = DATE_TIME_PATTERN): String {
        return dateTime.format(DateTimeFormatter.ofPattern(timePattern))
    }

    fun getHourMinuteByLocalDateTime(date: LocalDateTime) : String {
        return date.format(DateTimeFormatter.ofPattern("HHmm"))
    }

    fun getAddDays(date: LocalDate, amount: Long): LocalDate {
        return date.plusDays(amount)
    }

    fun getAddDays(date: LocalDateTime, amount: Long): LocalDateTime {
        return date.plusDays(amount)
    }

    fun getAddMinutes(date: LocalDateTime, amount: Long): LocalDateTime {
        return date.plusMinutes(amount)
    }

    fun getAddSeconds(date: LocalDateTime, amount: Long): LocalDateTime {
        return date.plusSeconds(amount)
    }

    fun diffDays(date1: LocalDateTime, date2: LocalDateTime) : Int {
        return ChronoUnit.DAYS.between(date1, date2).toInt()
    }

    fun diffHours(date1: LocalDateTime, date2: LocalDateTime) : Int {
        return ChronoUnit.HOURS.between(date1, date2).toInt()
    }

    fun diffMins(date1: LocalDateTime, date2: LocalDateTime) : Int {
        return ChronoUnit.MINUTES.between(date1, date2).toInt()
    }

    fun diffSecs(date1: LocalDateTime, date2: LocalDateTime) : Int {
        return ChronoUnit.SECONDS.between(date1, date2).toInt()
    }

    fun beginTimeToLocalDateTime(date: String) : LocalDateTime {
        return LocalDateTime.parse(("$date 00:00:00").toString(), DateTimeFormatter.ofPattern(DATE_TIME_PATTERN))
    }

    fun lastTimeToLocalDateTime(date: String) : LocalDateTime {
        return LocalDateTime.parse(("$date 23:59:59").toString(), DateTimeFormatter.ofPattern(DATE_TIME_PATTERN))
    }

    fun firstDayToLocalDateTime(date: String) : LocalDateTime {
        val day = yearMonthToString(date)
//        val day = LocalDate.parse(stringToLocalDate(date).toString(), DateTimeFormatter.ofPattern("yyyy-MM")).toString()
        return LocalDateTime.parse(("$day-01 00:00:00").toString(), DateTimeFormatter.ofPattern(DATE_TIME_PATTERN))
    }

    fun lastDayToLocalDateTime(date: String) : LocalDateTime {
        val day = yearMonthToString(date)
        val lastDay = day.lengthOfMonth()
        return LocalDateTime.parse(("$day-$lastDay 23:59:59").toString(), DateTimeFormatter.ofPattern(DATE_TIME_PATTERN))
    }

    private fun yearMonthToString(date: String) : YearMonth {
        return YearMonth.from(LocalDate.parse(stringToLocalDate(date).toString()))
    }

    fun makeLocalDateTime(date: String, hour: String, minute: String) : LocalDateTime {
        return LocalDateTime.parse(("$date $hour:$minute:00").toString(), DateTimeFormatter.ofPattern(DATE_TIME_PATTERN))
    }

    fun minusSecLocalDateTime(date: LocalDateTime, sec: Int): LocalDateTime {
        return date.minusSeconds(sec.toLong())
    }

    fun getWeek(date: String?): WeekType?{
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val c = Calendar.getInstance()
        try {
            c.time = format.parse(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        if (c[Calendar.DAY_OF_WEEK] == Calendar.SUNDAY) {
            return WeekType.SUN
        }
        if (c[Calendar.DAY_OF_WEEK] == Calendar.MONDAY) {
            return WeekType.MON
        }
        if (c[Calendar.DAY_OF_WEEK] == Calendar.TUESDAY) {
            return WeekType.TUE
        }
        if (c[Calendar.DAY_OF_WEEK] == Calendar.WEDNESDAY) {
            return WeekType.WED
        }
        if (c[Calendar.DAY_OF_WEEK] == Calendar.THURSDAY) {
            return WeekType.THU
        }
        if (c[Calendar.DAY_OF_WEEK] == Calendar.FRIDAY) {
            return WeekType.FRI
        }
        if (c[Calendar.DAY_OF_WEEK] == Calendar.SATURDAY) {
            return WeekType.SAT
        }
        return null
    }

    fun getWeekRange(date: String?): DiscountRangeType? {
        return when (getWeek(date)) {
            WeekType.SUN,WeekType.SAT -> DiscountRangeType.WEEKEND
            WeekType.MON,WeekType.TUE,WeekType.WED,WeekType.THU,WeekType.FRI -> DiscountRangeType.WEEKDAY
            else -> null
        }
    }

    fun getMinByDates(date1: LocalDateTime, date2: LocalDateTime): LocalDateTime {
        return if (date1 > date2) date2 else date1
    }

    fun getMaxByDates(date1: LocalDateTime, date2: LocalDateTime): LocalDateTime {
        return if (date1 > date2) date1 else date2
    }
}