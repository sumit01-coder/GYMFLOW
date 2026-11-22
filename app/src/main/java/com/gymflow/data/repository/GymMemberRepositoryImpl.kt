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

    override fun getMemberFlow(memberId: String): Flow<GymMember?> {
        return gymMemberDao.getMemberFlow(memberId).map { it?.toDomain() }
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

    override suspend fun revokeMembership(memberId: String, reason: String) {
        gymMemberDao.revokeMembership(memberId, System.currentTimeMillis(), reason)
    }

    override suspend fun removeTrainerFromMembers(trainerId: String, trainerEmail: String) {
        gymMemberDao.removeTrainerFromMembers(trainerId, trainerEmail)
    }

    override suspend fun deleteMember(memberId: String) {
        gymMemberDao.deleteMember(memberId)
    }

    override suspend fun deleteAllMembers() {
        gymMemberDao.deleteAllMembers()
    }

    override suspend fun markRenewalReminderSent(memberId: String) {
        gymMemberDao.updateRenewalNotificationSent(memberId, System.currentTimeMillis())
    }

    override suspend fun seedInitialMembersIfEmpty() {
        // Automatically purge any legacy dummy/seed data that was saved on disk in earlier versions
        gymMemberDao.purgeLegacyDummyMembers()
    }
}
