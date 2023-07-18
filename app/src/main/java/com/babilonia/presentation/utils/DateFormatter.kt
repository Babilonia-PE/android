package com.babilonia.presentation.utils

import com.babilonia.EmptyConstants
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.format.DateTimeFormat

object DateFormatter {

    private const val DEFAULT_RESPONSE_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ"
    private const val FULL_DATE = "d MMM yyyy"

    fun toDateTime(time: String): DateTime {
        return DateTime.parse(time, DateTimeFormat.forPattern(DEFAULT_RESPONSE_DATE_FORMAT))
    }

    fun toResponseDate(dateTime: DateTime): String {
        return dateTime.toString(DEFAULT_RESPONSE_DATE_FORMAT)
    }

    fun daysBetween(startTime: String, endTime: String): Int {
        val start = toDateTime(startTime)
        val end = toDateTime(endTime)
        return Days.daysBetween(start.toLocalDate(), end.toLocalDate()).days
    }

    fun daysLeft(endTime: String): Int {
        val start = DateTime.now()
        val end = toDateTime(endTime)
        return Days.daysBetween(start.toLocalDate(), end.toLocalDate()).days
    }

    fun toFullDate(time: String?): String {
        return if (time != null) {
            toDateTime(time).toString(FULL_DATE)
        } else {
            EmptyConstants.EMPTY_STRING
        }
    }
}