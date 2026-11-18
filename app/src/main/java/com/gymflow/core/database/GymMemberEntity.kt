package com.gymflow.core.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gymflow.domain.model.GymMember
import com.gymflow.domain.model.MembershipDuration
import com.gymflow.domain.model.PaymentMethod
import com.gymflow.domain.model.PaymentStatus
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "gym_members",
    indices = [
        Index(value = ["endDateEpochMs"]),
        Index(value = ["paymentStatus"]),
        Index(value = ["assignedTrainerId"])
    ]
)
data class GymMemberEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phoneNumber: String,
    val email: String,
    val assignedTrainerId: String? = null,
    val assignedTrainerName: String? = null,
    val durationMonths: Int = 1,
    val startDateEpochMs: Long = System.currentTimeMillis(),
    val endDateEpochMs: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000),
    val feeAmount: Double = 0.0,
    val amountPaid: Double = 0.0,
    val paymentStatus: String = "PAID",
    val paymentMethod: String = "UPI",
    val upiIdOrPaymentLink: String? = null,
    val notes: String = "",
    val isActive: Boolean = true,
    val lastRenewalNotificationEpochMs: Long? = null
) {
    fun toDomain(): GymMember {
        return GymMember(
            id = id,
            name = name,
            phoneNumber = phoneNumber,
            email = email,
            assignedTrainerId = assignedTrainerId,
            assignedTrainerName = assignedTrainerName,
            duration = MembershipDuration.fromMonths(durationMonths),
            startDateEpochMs = startDateEpochMs,
            endDateEpochMs = endDateEpochMs,
            feeAmount = feeAmount,
            amountPaid = amountPaid,
            paymentStatus = runCatching { PaymentStatus.valueOf(paymentStatus) }.getOrDefault(PaymentStatus.PAID),
            paymentMethod = runCatching { PaymentMethod.valueOf(paymentMethod) }.getOrDefault(PaymentMethod.UPI),
            upiIdOrPaymentLink = upiIdOrPaymentLink,
            notes = notes,
            isActive = isActive,
            lastRenewalNotificationEpochMs = lastRenewalNotificationEpochMs
        )
    }

    companion object {
        fun fromDomain(member: GymMember): GymMemberEntity {
            return GymMemberEntity(
                id = member.id,
                name = member.name,
                phoneNumber = member.phoneNumber,
                email = member.email,
                assignedTrainerId = member.assignedTrainerId,
                assignedTrainerName = member.assignedTrainerName,
                durationMonths = member.duration.months,
                startDateEpochMs = member.startDateEpochMs,
                endDateEpochMs = member.endDateEpochMs,
                feeAmount = member.feeAmount,
                amountPaid = member.amountPaid,
                paymentStatus = member.paymentStatus.name,
                paymentMethod = member.paymentMethod.name,
                upiIdOrPaymentLink = member.upiIdOrPaymentLink,
                notes = member.notes,
                isActive = member.isActive,
                lastRenewalNotificationEpochMs = member.lastRenewalNotificationEpochMs
            )
        }
    }
}
