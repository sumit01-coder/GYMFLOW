package com.gymflow.core.util

import java.net.URLEncoder
import java.util.Locale
import kotlin.random.Random

object UpiPaymentHelper {

    fun generateOrderId(): String {
        val timestamp = System.currentTimeMillis().toString().takeLast(8)
        val suffix = Random.nextInt(100, 999)
        return "ORD-$timestamp-$suffix"
    }

    fun buildUpiPaymentUri(
        upiId: String,
        payeeName: String,
        amount: Double,
        orderId: String
    ): String {
        val cleanUpi = upiId.trim()
        val cleanName = URLEncoder.encode(payeeName.trim().ifBlank { "GymFlow Fitness" }, "UTF-8")
        val formattedAmount = String.format(Locale.US, "%.2f", amount)
        val note = URLEncoder.encode("GymFlow Pass $orderId", "UTF-8")
        return "upi://pay?pa=$cleanUpi&pn=$cleanName&am=$formattedAmount&cu=INR&tn=$note&tr=$orderId"
    }

    fun isValidUpiId(upiId: String): Boolean {
        val trimmed = upiId.trim()
        return trimmed.contains("@") && trimmed.length in 5..64 && !trimmed.contains(" ")
    }

    fun isValidUtr(utr: String): Boolean {
        val trimmed = utr.trim()
        return trimmed.length in 8..30 && trimmed.all { it.isLetterOrDigit() }
    }
}
