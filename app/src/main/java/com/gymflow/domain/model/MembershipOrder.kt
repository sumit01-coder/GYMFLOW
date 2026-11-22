package com.gymflow.domain.model

enum class OrderStatus(val displayName: String) {
    PENDING("Pending"),
    PENDING_VERIFICATION("Verification Pending"),
    APPROVED("Approved & Active"),
    REJECTED("Rejected");

    companion object {
        fun fromString(status: String?): OrderStatus {
            return when (status?.uppercase()?.trim()) {
                "APPROVED" -> APPROVED
                "REJECTED" -> REJECTED
                "PENDING_VERIFICATION" -> PENDING_VERIFICATION
                else -> PENDING
            }
        }
    }
}

data class MembershipOrder(
    val orderId: String,
    val memberId: String,
    val memberName: String,
    val memberEmail: String,
    val memberPhone: String,
    val trainerId: String,
    val trainerName: String,
    val gymName: String,
    val planDurationMonths: Int,
    val planDurationDisplayName: String,
    val amountInr: Double,
    val paymentMethod: String = "UPI",
    val targetUpiId: String,
    val utrNumber: String? = null,
    val orderStatus: OrderStatus = OrderStatus.PENDING,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val approvedAtEpochMs: Long? = null,
    val notes: String = ""
)
