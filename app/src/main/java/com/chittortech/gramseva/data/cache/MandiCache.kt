package com.chittortech.gramseva.data.cache

import android.content.Context
import com.chittortech.gramseva.data.models.MandiItem
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

object MandiCache {
    private const val PREF_NAME = "gramseva_mandi_cache_v2"
    private const val KEY_PREFIX = "mandi_items_"

    // In-memory cache for instant 0ms access across tab switches
    val memoryCache = ConcurrentHashMap<String, List<MandiItem>>()

    fun getCachedMandiPrices(context: Context, state: String = "Rajasthan"): List<MandiItem>? {
        val cleanState = state.trim().ifBlank { "Rajasthan" }
        // 1. Check in-memory cache first (instant)
        memoryCache[cleanState]?.let {
            if (it.isNotEmpty()) return it
        }

        // 2. Check SharedPreferences persistent cache
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString("$KEY_PREFIX$cleanState", null) ?: return null

        try {
            val jsonArray = JSONArray(jsonStr)
            if (jsonArray.length() == 0) return null

            val list = mutableListOf<MandiItem>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    MandiItem(
                        id = obj.optString("id", "mandi_$i"),
                        commodity = obj.optString("commodity", ""),
                        market = obj.optString("market", ""),
                        district = obj.optString("district", ""),
                        state = obj.optString("state", cleanState),
                        variety = obj.optString("variety", "FAQ"),
                        grade = obj.optString("grade", "FAQ"),
                        minPrice = obj.optInt("minPrice", 0),
                        maxPrice = obj.optInt("maxPrice", 0),
                        modalPrice = obj.optInt("modalPrice", 0),
                        arrivalDate = obj.optString("arrivalDate", "आज"),
                        trend = obj.optString("trend", "स्थिर (लाइव)"),
                        isHotCrop = obj.optBoolean("isHotCrop", false),
                        arrivalsTons = if (obj.has("arrivalsTons") && !obj.isNull("arrivalsTons")) obj.optDouble("arrivalsTons") else null
                    )
                )
            }
            if (list.isNotEmpty()) {
                memoryCache[cleanState] = list
                return list
            }
        } catch (e: Exception) {
            // Ignore parse errors
        }
        return null
    }

    fun saveMandiPrices(context: Context, state: String = "Rajasthan", items: List<MandiItem>) {
        if (items.isEmpty()) return
        val cleanState = state.trim().ifBlank { "Rajasthan" }
        // Update in-memory cache
        memoryCache[cleanState] = items

        // Update persistent SharedPreferences
        try {
            val jsonArray = JSONArray()
            for (item in items) {
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("commodity", item.commodity)
                    put("market", item.market)
                    put("district", item.district)
                    put("state", item.state)
                    put("variety", item.variety)
                    put("grade", item.grade)
                    put("minPrice", item.minPrice)
                    put("maxPrice", item.maxPrice)
                    put("modalPrice", item.modalPrice)
                    put("arrivalDate", item.arrivalDate)
                    put("trend", item.trend)
                    put("isHotCrop", item.isHotCrop)
                    if (item.arrivalsTons != null) {
                        put("arrivalsTons", item.arrivalsTons)
                    }
                }
                jsonArray.put(obj)
            }

            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString("$KEY_PREFIX$cleanState", jsonArray.toString()).apply()
        } catch (e: Exception) {
            // Ignore write errors
        }
    }

    fun getMemoryCache(state: String = "Rajasthan"): List<MandiItem>? {
        return memoryCache[state.trim().ifBlank { "Rajasthan" }]
    }
}

