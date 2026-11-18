package com.gymflow.feature.trainer

import com.gymflow.core.whatsapp.WhatsAppMessenger
import com.gymflow.domain.model.GymMember
import com.gymflow.domain.model.MembershipDuration
import com.gymflow.domain.model.PaymentMethod
import com.gymflow.domain.model.PaymentStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GymMemberManagementTest {

    @Test
    fun testPhoneNumberSanitization() {
        assertEquals("919876543210", WhatsAppMessenger.cleanPhoneNumber("9876543210"))
        assertEquals("919876543210", WhatsAppMessenger.cleanPhoneNumber("+91 98765-43210"))
        assertEquals("919876543210", WhatsAppMessenger.cleanPhoneNumber("09876543210"))
    }

    @Test
    fun testMembershipDurationMonthsMapping() {
        assertEquals(1, MembershipDuration.ONE_MONTH.months)
        assertEquals(2, MembershipDuration.TWO_MONTHS.months)
        assertEquals(3, MembershipDuration.THREE_MONTHS.months)
        assertEquals(6, MembershipDuration.SIX_MONTHS.months)
        assertEquals(9, MembershipDuration.NINE_MONTHS.months)
        assertEquals(12, MembershipDuration.TWELVE_MONTHS.months)
        assertEquals(18, MembershipDuration.EIGHTEEN_MONTHS.months)

        assertEquals(MembershipDuration.TWELVE_MONTHS, MembershipDuration.fromMonths(12))
        assertEquals(MembershipDuration.EIGHTEEN_MONTHS, MembershipDuration.fromMonths(18))
    }

    @Test
    fun testMemberExpiryAndRemainingDaysCalculation() {
        val now = System.currentTimeMillis()
        val dayMs = 24L * 60 * 60 * 1000

        val activeMember = GymMember(
            id = "test-1",
            name = "John Doe",
            phoneNumber = "9876543210",
            email = "john@example.com",
            duration = MembershipDuration.SIX_MONTHS,
            startDateEpochMs = now - (30L * dayMs),
            endDateEpochMs = now + (150L * dayMs),
            paymentStatus = PaymentStatus.PAID
        )

        assertFalse(activeMember.isExpired)
        assertFalse(activeMember.isExpiringSoon)
        assertTrue(activeMember.remainingDays > 140)

        val expiringSoonMember = GymMember(
            id = "test-2",
            name = "Jane Smith",
            phoneNumber = "9876543211",
            email = "jane@example.com",
            duration = MembershipDuration.ONE_MONTH,
            startDateEpochMs = now - (26L * dayMs),
            endDateEpochMs = now + (4L * dayMs),
            paymentStatus = PaymentStatus.PAID
        )

        assertFalse(expiringSoonMember.isExpired)
        assertTrue(expiringSoonMember.isExpiringSoon)
        assertTrue(expiringSoonMember.remainingDays in 3..4)

        val expiredMember = GymMember(
            id = "test-3",
            name = "Bob Miller",
            phoneNumber = "9876543212",
            email = "bob@example.com",
            duration = MembershipDuration.ONE_MONTH,
            startDateEpochMs = now - (35L * dayMs),
            endDateEpochMs = now - (5L * dayMs),
            paymentStatus = PaymentStatus.EXPIRED
        )

        assertTrue(expiredMember.isExpired)
        assertEquals(0, expiredMember.remainingDays)
    }

    @Test
    fun testWhatsAppRenewalMessageGeneration() {
        val now = System.currentTimeMillis()
        val dayMs = 24L * 60 * 60 * 1000

        val expiringMember = GymMember(
            id = "test-exp",
            name = "Alex Stone",
            phoneNumber = "9876543210",
            email = "alex@example.com",
            duration = MembershipDuration.THREE_MONTHS,
            startDateEpochMs = now - (88L * dayMs),
            endDateEpochMs = now + (2L * dayMs),
            paymentStatus = PaymentStatus.PAID
        )

        val message = WhatsAppMessenger.generateRenewalMessage(
            member = expiringMember,
            gymName = "GymFlow Powerhouse",
            upiId = "gymflow@upi"
        )

        assertTrue("Message should contain member name", message.contains("Alex Stone"))
        assertTrue("Message should contain gym name", message.contains("GymFlow Powerhouse"))
        assertTrue("Message should mention duration", message.contains("3 Months"))
        assertTrue("Message should include UPI ID", message.contains("gymflow@upi"))
    }
}
