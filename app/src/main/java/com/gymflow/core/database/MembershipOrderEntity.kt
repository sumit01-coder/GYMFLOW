package com.gymflow.core.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gymflow.domain.model.MembershipOrder
import com.gymflow.domain.model.OrderStatus

@Entity(
    tableName = "membership_orders",
    indices = [
        Index("memberId"),
        Index("createdAtEpochMs"),
        Index("orderStatus")
    ]
)
data class MembershipOrderEntity(
    @PrimaryKey val orderId: String,
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
    val paymentMethod: String,
    val targetUpiId: String,
    val utrNumber: String? = null,
    val orderStatus: String,
    val createdAtEpochMs: Long,
    val approvedAtEpochMs: Long? = null,
    val notes: String = "",
    val isSynced: Boolean = false
) {
    fun toDomain(): MembershipOrder = MembershipOrder(
        orderId = orderId,
        memberId = memberId,
        memberName = memberName,
        memberEmail = memberEmail,
        memberPhone = memberPhone,
        trainerId = trainerId,
        trainerName = trainerName,
        gymName = gymName,
        planDurationMonths = planDurationMonths,
        planDurationDisplayName = planDurationDisplayName,
        amountInr = amountInr,
        paymentMethod = paymentMethod,
        targetUpiId = targetUpiId,
        utrNumber = utrNumber,
        orderStatus = try { OrderStatus.valueOf(orderStatus) } catch (_: Exception) { OrderStatus.PENDING_VERIFICATION },
        createdAtEpochMs = createdAtEpochMs,
        approvedAtEpochMs = approvedAtEpochMs,
        notes = notes
    )

    companion object {
        fun fromDomain(order: MembershipOrder, isSynced: Boolean = false): MembershipOrderEntity = MembershipOrderEntity(
            orderId = order.orderId,
            memberId = order.memberId,
            memberName = order.memberName,
            memberEmail = order.memberEmail,
            memberPhone = order.memberPhone,
            trainerId = order.trainerId,
            trainerName = order.trainerName,
            gymName = order.gymName,
            planDurationMonths = order.planDurationMonths,
            planDurationDisplayName = order.planDurationDisplayName,
            amountInr = order.amountInr,
            paymentMethod = order.paymentMethod,
            targetUpiId = order.targetUpiId,
            utrNumber = order.utrNumber,
            orderStatus = order.orderStatus.name,
            createdAtEpochMs = order.createdAtEpochMs,
            approvedAtEpochMs = order.approvedAtEpochMs,
            notes = order.notes,
            isSynced = isSynced
        )
    }
}
