package com.rvbhati.purestatus.engine

import android.content.Context
import android.content.SharedPreferences

object UserPreferences {
    private const val PREF_NAME = "pure_status_prefs"
    private const val KEY_WHATSAPP_NUMBER = "key_whatsapp_number"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun getWhatsAppNumber(context: Context): String {
        return getPrefs(context).getString(KEY_WHATSAPP_NUMBER, "") ?: ""
    }

    fun setWhatsAppNumber(context: Context, number: String) {
        val cleanNumber = number.replace("+", "").replace(" ", "").replace("-", "").trim()
        val formattedNumber = if (cleanNumber.length == 10) "91$cleanNumber" else cleanNumber
        getPrefs(context).edit().putString(KEY_WHATSAPP_NUMBER, formattedNumber).apply()
    }

    fun hasWhatsAppNumber(context: Context): Boolean {
        return getWhatsAppNumber(context).isNotBlank()
    }
}
