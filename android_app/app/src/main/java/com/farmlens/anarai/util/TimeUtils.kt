package com.farmlens.anarai.util

import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {
    /**
     * Parses a UTC ISO-8601 string (e.g., from Supabase) and returns it formatted
     * in the device's local timezone.
     * Example input: "2023-10-25T12:34:56.123456+00:00"
     */
    fun formatSupabaseTime(utcString: String?): String {
        if (utcString.isNullOrEmpty()) return "Just now"

        try {
            // Supabase returns timestamps like: 2023-10-25T12:34:56.123456+00:00
            // We use a regex or substring to drop the fractional seconds for simpler parsing
            // if needed, or we just parse the basic part.
            val basicIsoString = if (utcString.contains(".")) {
                utcString.substringBefore(".") + "Z"
            } else if (utcString.contains("+")) {
                utcString.substringBefore("+") + "Z"
            } else {
                utcString.replace("+00:00", "Z")
            }

            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val date = parser.parse(basicIsoString) ?: return "Just now"

            // Now format it to the user's local timezone
            val formatter = SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault())
            formatter.timeZone = TimeZone.getDefault()
            return formatter.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback just in case parsing fails
            return utcString.substringBefore("T")
        }
    }
}
