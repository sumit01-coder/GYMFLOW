package com.gymflow.data.repository

import com.gymflow.core.database.GymMemberDao
import com.gymflow.core.database.GymMemberEntity
import com.gymflow.domain.model.GymMember
import com.gymflow.domain.model.MembershipDuration
import com.gymflow.domain.model.PaymentMethod
import com.gymflow.domain.model.PaymentStatus
import com.gymflow.domain.repository.GymMemberRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GymMemberRepositoryImpl @Inject constructor(
    private val gymMemberDao: GymMemberDao
) : GymMemberRepository {

    override fun getAllMembers(): Flow<List<GymMember>> {
        return gymMemberDao.getAllMembers().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getExpiringSoonMembers(): Flow<List<GymMember>> {
        val now = System.currentTimeMillis()
        val sevenDaysAhead = now + (7L * 24 * 60 * 60 * 1000)
        return gymMemberDao.getExpiringSoonMembers(sevenDaysAhead, now).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getExpiredMembers(): Flow<List<GymMember>> {
        val now = System.currentTimeMillis()
        return gymMemberDao.getExpiredMembers(now).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getPendingPaymentMembers(): Flow<List<GymMember>> {
        return gymMemberDao.getPendingPaymentMembers().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getMemberById(memberId: String): GymMember? {
        return gymMemberDao.getMemberById(memberId)?.toDomain()
    }

    override suspend fun addMember(member: GymMember) {
        gymMemberDao.insertMember(GymMemberEntity.fromDomain(member))
    }

    override suspend fun updateMember(member: GymMember) {
        gymMemberDao.updateMember(GymMemberEntity.fromDomain(member))
    }

    override suspend fun renewMembership(
        memberId: String,
        additionalMonths: Int,
        fee: Double,
        paymentMethod: String
    ) {
        val existing = gymMemberDao.getMemberById(memberId) ?: return
        val currentEnd = existing.endDateEpochMs
        val now = System.currentTimeMillis()
        val baseDate = if (currentEnd > now) currentEnd else now

        val calendar = Calendar.getInstance().apply {
            timeInMillis = baseDate
            add(Calendar.MONTH, additionalMonths)
        }
        val newEnd = calendar.timeInMillis

        val updated = existing.copy(
            durationMonths = additionalMonths,
            startDateEpochMs = baseDate,
            endDateEpochMs = newEnd,
            feeAmount = fee,
            amountPaid = fee,
            paymentStatus = PaymentStatus.PAID.name,
            paymentMethod = paymentMethod
        )
        gymMemberDao.updateMember(updated)
    }

    override suspend fun deleteMember(memberId: String) {
        gymMemberDao.deleteMember(memberId)
    }

    override suspend fun markRenewalReminderSent(memberId: String) {
        gymMemberDao.updateRenewalNotificationSent(memberId, System.currentTimeMillis())
    }

    override suspend fun seedInitialMembersIfEmpty() {
        if (gymMemberDao.getMemberCount() > 0) return

        val now = System.currentTimeMillis()
        val dayMs = 24L * 60 * 60 * 1000

        val seedMembers = listOf(
            GymMember(
                id = UUID.randomUUID().toString(),
                name = "Aarav Sharma",
                phoneNumber = "+91 9876543210",
                email = "aarav.sharma@example.com",
                assignedTrainerName = "Coach Marcus",
                duration = MembershipDuration.THREE_MONTHS,
                startDateEpochMs = now - (85L * dayMs),
                endDateEpochMs = now + (5L * dayMs), // Expiring in 5 days!
                feeAmount = 4500.0,
                amountPaid = 4500.0,
                paymentStatus = PaymentStatus.PAID,
                paymentMethod = PaymentMethod.UPI,
                notes = "Focus on cutting and endurance"
            ),
            GymMember(
                id = UUID.randomUUID().toString(),
                name = "Priya Patel",
                phoneNumber = "+91 9822334455",
                email = "priya.patel@example.com",
                assignedTrainerName = "Coach Marcus",
                duration = MembershipDuration.ONE_MONTH,
                startDateEpochMs = now - (31L * dayMs),
                endDateEpochMs = now - (1L * dayMs), // Expired yesterday!
                feeAmount = 1800.0,
                amountPaid = 1800.0,
                paymentStatus = PaymentStatus.EXPIRED,
                paymentMethod = PaymentMethod.CASH,
                notes = "Requested morning batch"
            ),
            GymMember(
                id = UUID.randomUUID().toString(),
                name = "Rahul Verma",
                phoneNumber = "+91 9711223344",
                email = "rahul.v@example.com",
                assignedTrainerName = "Elena Vance",
                duration = MembershipDuration.SIX_MONTHS,
                startDateEpochMs = now - (30L * dayMs),
                endDateEpochMs = now + (150L * dayMs), // Active
                feeAmount = 8500.0,
                amountPaid = 8500.0,
                paymentStatus = PaymentStatus.PAID,
                paymentMethod = PaymentMethod.UPI,
                notes = "Powerlifting prep"
            ),
            GymMember(
                id = UUID.randomUUID().toString(),
                name = "Neha Gupta",
                phoneNumber = "+91 9988776655",
                email = "neha.gupta@example.com",
                assignedTrainerName = "Coach Marcus",
                duration = MembershipDuration.TWELVE_MONTHS,
                startDateEpochMs = now - (360L * dayMs),
                endDateEpochMs = now + (2L * dayMs), // Expiring in 2 days!
                feeAmount = 15000.0,
                amountPaid = 15000.0,
                paymentStatus = PaymentStatus.PAID,
                paymentMethod = PaymentMethod.CARD,
                notes = "Annual membership renewal due"
            ),
            GymMember(
                id = UUID.randomUUID().toString(),
                name = "Vikram Singh",
                phoneNumber = "+91 9811224466",
                email = "vikram.singh@example.com",
                assignedTrainerName = "Elena Vance",
                duration = MembershipDuration.TWO_MONTHS,
                startDateEpochMs = now - (15L * dayMs),
                endDateEpochMs = now + (45L * dayMs),
                feeAmount = 3200.0,
                amountPaid = 1500.0,
                paymentStatus = PaymentStatus.PARTIALLY_PAID,
                paymentMethod = PaymentMethod.UPI,
                notes = "Balance Rs 1700 pending"
            )
        )

        gymMemberDao.insertAll(seedMembers.map { GymMemberEntity.fromDomain(it) })
    }
}
