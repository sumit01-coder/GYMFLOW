package com.gymflow.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GymMemberDao {

    @Query("SELECT * FROM gym_members ORDER BY name ASC")
    fun getAllMembers(): Flow<List<GymMemberEntity>>

    @Query("SELECT * FROM gym_members WHERE id = :memberId")
    suspend fun getMemberById(memberId: String): GymMemberEntity?

    @Query("SELECT * FROM gym_members WHERE id = :memberId LIMIT 1")
    fun getMemberFlow(memberId: String): Flow<GymMemberEntity?>

    @Query("SELECT * FROM gym_members WHERE endDateEpochMs <= :thresholdEpochMs AND endDateEpochMs > :currentEpochMs ORDER BY endDateEpochMs ASC")
    fun getExpiringSoonMembers(thresholdEpochMs: Long, currentEpochMs: Long): Flow<List<GymMemberEntity>>

    @Query("SELECT * FROM gym_members WHERE endDateEpochMs <= :currentEpochMs ORDER BY endDateEpochMs ASC")
    fun getExpiredMembers(currentEpochMs: Long): Flow<List<GymMemberEntity>>

    @Query("SELECT * FROM gym_members WHERE endDateEpochMs <= :thresholdEpochMs ORDER BY endDateEpochMs ASC")
    suspend fun getExpiringOrExpiredMembersSync(thresholdEpochMs: Long): List<GymMemberEntity>

    @Query("SELECT * FROM gym_members WHERE paymentStatus != 'PAID' ORDER BY endDateEpochMs ASC")
    fun getPendingPaymentMembers(): Flow<List<GymMemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: GymMemberEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(members: List<GymMemberEntity>)

    @Update
    suspend fun updateMember(member: GymMemberEntity)

    @Query("DELETE FROM gym_members WHERE id = :memberId")
    suspend fun deleteMember(memberId: String)

    @Query("DELETE FROM gym_members")
    suspend fun deleteAllMembers()

    @Query("DELETE FROM gym_members WHERE id LIKE 'seed-%' OR name IN ('Aarav Sharma', 'Neha Gupta', 'Priya Patel', 'Rahul Verma', 'Vikram Singh')")
    suspend fun purgeLegacyDummyMembers()

    @Query("UPDATE gym_members SET lastRenewalNotificationEpochMs = :timestamp WHERE id = :memberId")
    suspend fun updateRenewalNotificationSent(memberId: String, timestamp: Long)

    @Query("UPDATE gym_members SET isActive = 0, paymentStatus = 'EXPIRED', endDateEpochMs = :expiredEpochMs, notes = :notes WHERE id = :memberId")
    suspend fun revokeMembership(memberId: String, expiredEpochMs: Long, notes: String = "Revoked by trainer")

    @Query("DELETE FROM gym_members WHERE (:trainerId != '' AND id = :trainerId) OR (:trainerEmail != '' AND LOWER(email) = LOWER(:trainerEmail))")
    suspend fun removeTrainerFromMembers(trainerId: String, trainerEmail: String)

    @Query("SELECT COUNT(*) FROM gym_members")
    suspend fun getMemberCount(): Int
}
