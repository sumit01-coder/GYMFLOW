package com.gymflow.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MembershipOrderDao {
    @Query("SELECT * FROM membership_orders WHERE memberId = :memberId ORDER BY createdAtEpochMs DESC")
    fun getOrdersForMemberFlow(memberId: String): Flow<List<MembershipOrderEntity>>

    @Query("SELECT * FROM membership_orders WHERE orderId = :orderId LIMIT 1")
    suspend fun getOrderById(orderId: String): MembershipOrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOrder(order: MembershipOrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOrders(orders: List<MembershipOrderEntity>)

    @Query("DELETE FROM membership_orders WHERE orderId = :orderId")
    suspend fun deleteOrder(orderId: String)
}
