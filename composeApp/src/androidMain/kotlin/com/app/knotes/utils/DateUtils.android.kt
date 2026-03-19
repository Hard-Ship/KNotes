package com.app.knotes.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual fun convertMillisToDate(millis: Long, pattern: String): String {
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    return formatter.format(Date(millis))
}
