package com.gymflow.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class MembershipDuration(val months: Int, val displayName: String) {
    ONE_MONTH(1, "1 Month"),
    TWO_MONTHS(2, "2 Months"),
    THREE_MONTHS(3, "3 Months (Quarterly)"),
    SIX_MONTHS(6, "6 Months (Half-Yearly)"),
    NINE_MONTHS(9, "9 Months"),
    TWELVE_MONTHS(12, "12 Months (1 Year)"),
    EIGHTEEN_MONTHS(18, "18 Months (Pro)");

    companion object {
        fun fromMonths(months: Int): MembershipDuration {
            return entries.find { it.months == months } ?: ONE_MONTH
        }
    }
}

@Serializable
enum class PaymentStatus(val displayName: String) {
    PAID("Paid"),
    PARTIALLY_PAID("Partially Paid"),
    PENDING("Pending"),
    EXPIRED("Expired"),
    GRACE_PERIOD("Grace Period")
}

@Serializable
enum class PaymentMethod(val displayName: String) {
    CASH("Cash"),
    UPI("UPI"),
    CARD("Card / Net Banking")
}

@Serializable
data class GymMember(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val email: String,
    val assignedTrainerId: String? = null,
    val assignedTrainerName: String? = null,
    val duration: MembershipDuration = MembershipDuration.ONE_MONTH,
    val startDateEpochMs: Long = System.currentTimeMillis(),
    val endDateEpochMs: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000),
    val feeAmount: Double = 0.0,
    val amountPaid: Double = 0.0,
    val paymentStatus: PaymentStatus = PaymentStatus.PAID,
    val paymentMethod: PaymentMethod = PaymentMethod.UPI,
    val upiIdOrPaymentLink: String? = null,
    val notes: String = "",
    val isActive: Boolean = true,
    val lastRenewalNotificationEpochMs: Long? = null
) {
    val remainingDays: Int
        get() {
            val diffMs = endDateEpochMs - System.currentTimeMillis()
            return if (diffMs <= 0) 0 else (diffMs / (1000L * 60 * 60 * 24)).toInt()
        }

    val isExpired: Boolean
        get() = System.currentTimeMillis() > endDateEpochMs

    val isExpiringSoon: Boolean
        get() {
            val days = remainingDays
            return !isExpired && days <= 7
        }
}
