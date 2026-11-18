package com.gymflow.domain.repository

import com.gymflow.domain.model.GymMember
import kotlinx.coroutines.flow.Flow

interface GymMemberRepository {
    fun getAllMembers(): Flow<List<GymMember>>
    fun getExpiringSoonMembers(): Flow<List<GymMember>>
    fun getExpiredMembers(): Flow<List<GymMember>>
    fun getPendingPaymentMembers(): Flow<List<GymMember>>
    suspend fun getMemberById(memberId: String): GymMember?
    suspend fun addMember(member: GymMember)
    suspend fun updateMember(member: GymMember)
    suspend fun renewMembership(memberId: String, additionalMonths: Int, fee: Double, paymentMethod: String)
    suspend fun deleteMember(memberId: String)
    suspend fun markRenewalReminderSent(memberId: String)
    suspend fun seedInitialMembersIfEmpty()
}
