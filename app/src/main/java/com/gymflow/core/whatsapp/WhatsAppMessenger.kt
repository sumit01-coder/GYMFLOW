package com.gymflow.core.whatsapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.gymflow.domain.model.GymMember
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object WhatsAppMessenger {

    private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    fun cleanPhoneNumber(rawPhone: String, defaultCountryCode: String = "91"): String {
        val digitsOnly = rawPhone.replace(Regex("[^0-9]"), "")
        return when {
            digitsOnly.length == 10 -> "$defaultCountryCode$digitsOnly"
            digitsOnly.startsWith("0") && digitsOnly.length == 11 -> "$defaultCountryCode${digitsOnly.drop(1)}"
            else -> digitsOnly
        }
    }

    fun generateRenewalMessage(
        member: GymMember,
        gymName: String = "GymFlow Fitness Center",
        upiId: String? = null
    ): String {
        val formattedExpiry = dateFormatter.format(Date(member.endDateEpochMs))
        val days = member.remainingDays
        val upiNotice = if (!upiId.isNullOrBlank()) "\n💳 Pay securely via UPI: $upiId" else ""

        return when {
            member.isExpired -> {
                """
                🏋️ *${gymName} - Membership Expired Alert*
                
                Hi ${member.name},
                Your ${member.duration.displayName} membership expired on *${formattedExpiry}*.
                
                We'd love to keep you on track toward your fitness goals! Please renew today to maintain uninterrupted access to the gym floor, trainer guidance, and workout tracking.$upiNotice
                
                Please reply to this message or visit the front desk for instant renewal.
                Stay strong! 💪
                """.trimIndent()
            }
            days <= 3 -> {
                """
                ⚠️ *${gymName} - Urgent Renewal Notice*
                
                Hello ${member.name},
                Friendly reminder: Your ${member.duration.displayName} gym membership is expiring in *${days} days* on *${formattedExpiry}*.
                
                Renew now to secure your current plan benefits and uninterrupted training sessions.$upiNotice
                
                See you at the gym! 🚀
                """.trimIndent()
            }
            else -> {
                """
                👋 *${gymName} - Membership Update*
                
                Hi ${member.name},
                Your ${member.duration.displayName} gym membership is set to expire on *${formattedExpiry}* (${days} days remaining).
                
                To renew early or discuss upcoming training schedules, feel free to reply right here or stop by the front desk.$upiNotice
                
                Keep crushing your workouts! 🏋️‍♂️
                """.trimIndent()
            }
        }
    }

    fun generateWelcomeMessage(
        member: GymMember,
        gymName: String = "GymFlow Fitness Center"
    ): String {
        val formattedStart = dateFormatter.format(Date(member.startDateEpochMs))
        val formattedExpiry = dateFormatter.format(Date(member.endDateEpochMs))
        val trainerLine = if (!member.assignedTrainerName.isNullOrBlank()) {
            "\n👨‍🏫 *Assigned Coach/Trainer:* ${member.assignedTrainerName}"
        } else ""

        return """
        🎉 *Welcome to ${gymName}!*
        
        Hi ${member.name},
        Your registration is complete!
        
        📅 *Membership:* ${member.duration.displayName}
        🗓️ *Active From:* $formattedStart
        ⏳ *Valid Until:* $formattedExpiry$trainerLine
        
        Download and open the GymFlow app to track your workouts, log nutrition, and monitor daily progress.
        
        Let's achieve greatness together! 💥
        """.trimIndent()
    }

    fun openChat(
        context: Context,
        rawPhone: String,
        message: String
    ): Boolean {
        val cleanedPhone = cleanPhoneNumber(rawPhone)
        if (cleanedPhone.isBlank()) {
            Toast.makeText(context, "Invalid phone number", Toast.LENGTH_SHORT).show()
            return false
        }

        val encodedMessage = try {
            URLEncoder.encode(message, "UTF-8")
        } catch (e: Exception) {
            message
        }

        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanedPhone&text=$encodedMessage")

        val whatsappIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.whatsapp")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(whatsappIntent)
            true
        } catch (e: Exception) {
            // If standard WhatsApp package launch fails, try general intent or WhatsApp Business
            val fallbackIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(fallbackIntent)
                true
            } catch (fallbackEx: Exception) {
                Toast.makeText(context, "WhatsApp is not installed on this device", Toast.LENGTH_LONG).show()
                false
            }
        }
    }
}
