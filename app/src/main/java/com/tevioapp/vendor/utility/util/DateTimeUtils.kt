package com.tevioapp.vendor.utility.util

import com.tevioapp.vendor.utility.AppConstants
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateTimeUtils {

    // ISO 8601 Date Format Constant
    private const val ISO_8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    private const val ISO_8601_FORMAT_2 = "yyyy-MM-dd'T'HH:mm:ss'Z'"
    private const val ISO_OFFSET_WITH_SECONDS = "yyyy-MM-dd'T'HH:mm:ssXXX"

    fun utcToLocalCalendar(utcTime: String?): Calendar? {
        if (utcTime.isNullOrEmpty()) return null

        val formats = listOf(
            ISO_8601_FORMAT, ISO_8601_FORMAT_2, ISO_OFFSET_WITH_SECONDS
        )

        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.US)
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                val date = sdf.parse(utcTime)
                return Calendar.getInstance().apply { time = date!! }
            } catch (_: Exception) {
                // try next format
            }
        }

        return null
    }

    /**
     * Converts a local time string in ISO 8601 format to a UTC Calendar object.
     * @param localTime Local time string in ISO 8601 format, e.g., "2024-12-03T18:30:00"
     * @param format Local time string in FORMAT_DAY_MONTH_TIME format, e.g., "2024-12-03T18:30:00"
     * @return UTC Calendar object or null if parsing fails
     */
    fun localToUtcCalendar(localTime: String, format: String?): Calendar? {
        if (localTime.isEmpty()) return null

        return try {
            val sdf = SimpleDateFormat(format, Locale.getDefault())
            val localCalendar = Calendar.getInstance()
            localCalendar.time = sdf.parse(localTime)!!
            val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            utcCalendar.timeInMillis = localCalendar.timeInMillis
            utcCalendar
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    /**
     * Converts a UTC time string to a utc Calendar object.
     * @param utcTime UTC time string in ISO 8601 format, e.g., "2024-12-03T18:30:00.000Z"
     * @return Local Calendar object or null if parsing fails
     */
    fun utcStringToUTCCalendar(utcTime: String?): Calendar? {
        if (utcTime.isNullOrEmpty()) return null
        return try {
            val sdf = SimpleDateFormat(ISO_8601_FORMAT, Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC") // Ensure parsing as UTC
            }
            val date = sdf.parse(utcTime)
            Calendar.getInstance().apply {
                time = date!!
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Converts a Calendar (assumed to be in local time) to a UTC Calendar.
     * @param calendar Local Calendar object
     * @return UTC Calendar object
     */
    fun calenderToUtcCalendar(calendar: Calendar): Calendar {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = calendar.timeInMillis
        }
    }

    /**
     * Converts a UTC Calendar to a UTC timestamp string in ISO 8601 format.
     * @param calendar UTC Calendar object
     * @return UTC timestamp string, e.g., "2024-12-03T18:30:00.000Z"
     */
    fun utcCalendarToTimestamp(calendar: Calendar?): String? {
        val date = calendar?.time ?: return null
        val sdf = SimpleDateFormat(ISO_8601_FORMAT, Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(date)
    }


    /**
     * Converts a local Calendar to a UTC timestamp string in ISO 8601 format.
     * @param calendar Local Calendar object
     * @return UTC timestamp string, e.g., "2024-12-03T18:30:00.000Z"
     */
    fun localCalendarToUtcTimestamp(calendar: Calendar): String {
        val utcCalendar = calenderToUtcCalendar(calendar)
        return utcCalendarToTimestamp(utcCalendar).orEmpty()
    }

    /**
     * Converts a given UTC Calendar to the local time zone.
     *
     * This method takes a nullable UTC Calendar instance and creates a new Calendar instance
     * set to the local time zone, using the same time in milliseconds. If the input is null,
     * the method returns null.
     * @param utcCalendar The input Calendar instance in UTC time zone, or null.
     * @return A new Calendar instance in the local time zone with the same time, or null if input is null.
     */
    fun convertUtcToLocal(utcCalendar: Calendar?): Calendar? {
        if (utcCalendar == null) return null
        val localTimeZone = TimeZone.getDefault()
        val localCalendar = Calendar.getInstance(localTimeZone)
        localCalendar.timeInMillis = utcCalendar.timeInMillis
        return localCalendar
    }

    fun convertLocalToUtc(localCalendar: Calendar): Calendar {
        localCalendar.timeZone = TimeZone.getTimeZone("UTC")
        return localCalendar
    }

    /**
     * Converts a Calendar to a formatted string based on the input format and time zone.
     * @param calendar Input Calendar object
     * @param format Desired date format (e.g., "yyyy-MM-dd HH:mm:ss")
     * @param timeZone TimeZone to be applied (defaults to Calendar's time zone)
     * @return Formatted date string
     */
    fun calendarToFormattedString(
        calendar: Calendar, format: String, timeZone: TimeZone = calendar.timeZone
    ): String? {
        return calendarToFormattedString(calendar.time, format, timeZone)
    }

    fun calendarToFormattedString(
        date: Date, format: String, timeZone: TimeZone
    ): String? {
        return try {
            val sdf = SimpleDateFormat(format, Locale.US)
            sdf.timeZone = timeZone
            sdf.format(date)
        } catch (e: Exception) {
            null
        }
    }

    fun String?.utcToLocalString(format: String): String? {
        val calender = utcToLocalCalendar(this)
        if (calender != null) {
            return calendarToFormattedString(calender, format)
        }
        return null
    }

    fun getUtcCalenderInstance(date: Date? = null): Calendar {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            if (date != null) {
                time = date
            }
        }
    }

    fun getUtcTimeStamp(): String {
        return utcCalendarToTimestamp(getUtcCalenderInstance()).orEmpty()
    }


    /**
     * 0 if the dates are the same.
     * A positive value if calendar1 is after calendar2.
     * A negative value if calendar1 is before calendar2.
     */
    fun compareDate(calendar1: Calendar, calendar2: Calendar): Int {
        val yearComparison = calendar1[Calendar.YEAR].compareTo(calendar2[Calendar.YEAR])
        if (yearComparison != 0) return yearComparison

        val monthComparison = calendar1[Calendar.MONTH].compareTo(calendar2[Calendar.MONTH])
        if (monthComparison != 0) return monthComparison

        return calendar1[Calendar.DAY_OF_MONTH].compareTo(calendar2[Calendar.DAY_OF_MONTH])
    }

    /**
     * Returns a formatted string representing the estimated time based on the given timestamp.
     * <p>
     * Rules:
     * - If the timestamp corresponds to today → returns "hh:mm a"
     * - If the timestamp corresponds to tomorrow → returns "Tomorrow hh:mm a"
     * - If the timestamp corresponds to yesterday → returns "Yesterday hh:mm a"
     * - Otherwise → returns "MMM dd yyyy - h:mm a"
     *
     * @param timeStamp The UTC timestamp string to format (expected in ISO-8601 format).
     * @return A formatted date/time string based on the day comparison.
     */

    fun getDisplayDateTime(timeStamp: String?): String? {
        if (timeStamp.isNullOrEmpty()) return null
        return try {
            val calendar = utcToLocalCalendar(timeStamp) ?: return timeStamp
            val now = Calendar.getInstance()
            when (compareDate(calendar, now)) {
                0 -> { // Same day
                    calendarToFormattedString(calendar, AppConstants.FORMAT_TIME)
                }

                1 -> { // Future date → check if tomorrow
                    if (compareDate(
                            calendar, now.apply { add(Calendar.DAY_OF_YEAR, 1) }) == 0
                    ) {
                        "Tomorrow ${calendarToFormattedString(calendar, AppConstants.FORMAT_TIME)}"
                    } else {
                        calendarToFormattedString(calendar, AppConstants.FORMAT_DATE_TIME_SHORT)
                    }
                }

                -1 -> { // Past date → check if yesterday
                    if (compareDate(
                            calendar, now.apply { add(Calendar.DAY_OF_YEAR, -1) }) == 0
                    ) {
                        "Yesterday ${calendarToFormattedString(calendar, AppConstants.FORMAT_TIME)}"
                    } else {
                        calendarToFormattedString(calendar, AppConstants.FORMAT_DATE_TIME_SHORT)
                    }
                }

                else -> {
                    calendarToFormattedString(calendar, AppConstants.FORMAT_DATE_TIME_SHORT)
                }
            }
        } catch (e: Exception) {
            return timeStamp
        }
    }

    fun timestampToUtcIso(timestamp: Long): String {
        val sdf = SimpleDateFormat(ISO_8601_FORMAT, Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date(timestamp))
    }
}


