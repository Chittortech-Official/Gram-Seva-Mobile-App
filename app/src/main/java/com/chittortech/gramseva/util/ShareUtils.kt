package com.chittortech.gramseva.util

import android.content.Context
import android.content.Intent
import com.chittortech.gramseva.data.models.MandiItem

object ShareUtils {

    fun shareMandiItem(context: Context, item: MandiItem) {
        val message = buildString {
            appendLine("🌾 *ग्राम-सेवा (GramSeva) — मंडी भाव* 🌾")
            appendLine("🗓 ${item.arrivalDate}")
            appendLine("🏢 मंडी: *${item.market}* (${item.state})")
            appendLine("🌿 फसल: *${item.commodity}* (${item.variety})")
            appendLine("💰 *मॉडल भाव:* ₹${item.modalPrice} / क्विंटल")
            appendLine("📊 रेंज: ₹${item.minPrice} - ₹${item.maxPrice}")
            appendLine("📈 बाजार रुख: ${item.trend}")
            if (item.arrivalsTons != null && item.arrivalsTons > 0) {
                appendLine("🚛 आवक: ~${item.arrivalsTons} टन")
            } else {
                appendLine("🏷️ गुणवत्ता/ग्रेड: ${item.grade}")
            }
            appendLine()
            appendLine("📍 *चित्तौड़टेक (ChittorTech) ग्राम-सेवा मोबाइल ऐप*")
            appendLine("📱 किसानों का सच्चा साथी | सटीक भाव व मौसम अलर्ट")
        }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
        val chooser = Intent.createChooser(sendIntent, "मंडी भाव शेयर करें (WhatsApp)")
        context.startActivity(chooser)
    }

    fun shareFullMandiBulletin(context: Context, items: List<MandiItem>) {
        val message = buildString {
            appendLine("🌾 *ग्राम-सेवा (GramSeva) — आज का संपूर्ण मंडी बुलेटिन* 🌾")
            appendLine("🗓 तारीख: आज के ताजा भाव | राजस्थान व एमपी मुख्य मंडियां")
            appendLine("─────────────────────────")
            items.take(8).forEach { item ->
                appendLine("• *${item.commodity}* | ${item.market}")
                appendLine("  भाव: *₹${item.modalPrice}* /क्विं (₹${item.minPrice}-₹${item.maxPrice}) [${item.trend}]")
            }
            appendLine("─────────────────────────")
            appendLine("📍 *चित्तौड़टेक (ChittorTech) ग्राम-सेवा ऐप*")
            appendLine("📲 किसानों की सेवा में सदैव समर्पित!")
        }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
        val chooser = Intent.createChooser(sendIntent, "पूरा मंडी बुलेटिन शेयर करें")
        context.startActivity(chooser)
    }
}

