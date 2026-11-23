package com.gymflow.core.database

import com.gymflow.domain.model.MembershipOrder
import com.gymflow.domain.model.OrderStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class MembershipOrderEntityTest {

    @Test
    fun domainToEntityAndBack_preservesAllPricingAndStatus() {
        val domainOrder = MembershipOrder(
            orderId = "order_12345",
            memberId = "member_1",
            memberName = "Sumit",
            memberEmail = "sumit@gymflow.com",
            memberPhone = "9876543210",
            trainerId = "trainer_1",
            trainerName = "Coach John",
            gymName = "Iron Fitness",
            planDurationMonths = 3,
            planDurationDisplayName = "3 Months Pass",
            amountInr = 4500.0,
            paymentMethod = "UPI",
            targetUpiId = "coach@upi",
            utrNumber = "UTR99887766",
            orderStatus = OrderStatus.PENDING_VERIFICATION,
            createdAtEpochMs = 1700000000000L,
            approvedAtEpochMs = null,
            notes = "Test renewal order"
        )

        val entity = MembershipOrderEntity.fromDomain(domainOrder, isSynced = true)
        assertEquals("order_12345", entity.orderId)
        assertEquals(4500.0, entity.amountInr, 0.001)
        assertEquals("PENDING_VERIFICATION", entity.orderStatus)
        assertEquals(true, entity.isSynced)

        val convertedBack = entity.toDomain()
        assertEquals(domainOrder.orderId, convertedBack.orderId)
        assertEquals(domainOrder.amountInr, convertedBack.amountInr, 0.001)
        assertEquals(domainOrder.orderStatus, convertedBack.orderStatus)
        assertEquals("coach@upi", convertedBack.targetUpiId)
        assertEquals("UTR99887766", convertedBack.utrNumber)
    }

    @Test
    fun unknownOrderStatus_defaultsToPendingVerificationGracefully() {
        val entity = MembershipOrderEntity(
            orderId = "order_999",
            memberId = "member_1",
            memberName = "Athlete",
            memberEmail = "athlete@gymflow.com",
            memberPhone = "1234567890",
            trainerId = "trainer_1",
            trainerName = "Coach",
            gymName = "GymFlow",
            planDurationMonths = 1,
            planDurationDisplayName = "1 Month Pass",
            amountInr = 1500.0,
            paymentMethod = "UPI",
            targetUpiId = "gym@upi",
            utrNumber = null,
            orderStatus = "UNKNOWN_STATUS_STRING",
            createdAtEpochMs = 1700000000000L,
            approvedAtEpochMs = null,
            notes = "",
            isSynced = false
        )

        val domain = entity.toDomain()
        assertEquals(OrderStatus.PENDING_VERIFICATION, domain.orderStatus)
    }
}
