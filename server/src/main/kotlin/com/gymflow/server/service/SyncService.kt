package com.gymflow.server.service

import com.gymflow.server.database.DatabaseFactory.dbQuery
import com.gymflow.server.database.MemberProfilesTable
import com.gymflow.server.database.SyncChangesTable
import com.gymflow.server.database.TrainerMembersTable
import com.gymflow.server.model.SyncChangeDto
import com.gymflow.server.model.SyncPullResponse
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import java.util.UUID

class SyncService {

    suspend fun pushChanges(userId: String, changes: List<SyncChangeDto>): Int = dbQuery {
        var count = 0
        for (change in changes) {
            val changeId = if (change.id.isNotBlank()) change.id else UUID.randomUUID().toString()
            val now = if (change.timestamp > 0) change.timestamp else System.currentTimeMillis()

            SyncChangesTable.insert {
                it[id] = changeId
                it[SyncChangesTable.userId] = userId
                it[entityType] = change.entityType
                it[entityId] = change.entityId
                it[version] = change.version
                it[payload] = change.payload
                it[timestamp] = now
            }
            count++
        }
        count
    }

    suspend fun pullChanges(userId: String, sinceTimestamp: Long): SyncPullResponse = dbQuery {
        // Collect related user IDs (e.g. trainer or connected members)
        val relatedUserIds = mutableSetOf(userId)

        // If user is a trainer, also fetch changes from their active members
        val activeMembers = TrainerMembersTable.selectAll()
            .where { (TrainerMembersTable.trainerId eq userId) and (TrainerMembersTable.status eq "ACTIVE") }
            .map { it[TrainerMembersTable.memberId] }
        relatedUserIds.addAll(activeMembers)

        // If user is a member, also fetch changes from their connected trainer
        val memberProfile = MemberProfilesTable.selectAll()
            .where { MemberProfilesTable.userId eq userId }
            .singleOrNull()
        memberProfile?.get(MemberProfilesTable.trainerId)?.let {
            relatedUserIds.add(it)
        }

        val rows = SyncChangesTable.selectAll()
            .where {
                (SyncChangesTable.userId inList relatedUserIds) and (SyncChangesTable.timestamp greater sinceTimestamp)
            }
            .orderBy(SyncChangesTable.timestamp, SortOrder.ASC)
            .toList()

        val changes = rows.map {
            SyncChangeDto(
                id = it[SyncChangesTable.id],
                userId = it[SyncChangesTable.userId],
                entityType = it[SyncChangesTable.entityType],
                entityId = it[SyncChangesTable.entityId],
                version = it[SyncChangesTable.version],
                payload = it[SyncChangesTable.payload],
                timestamp = it[SyncChangesTable.timestamp]
            )
        }

        SyncPullResponse(
            changes = changes,
            lastSyncTimestamp = System.currentTimeMillis()
        )
    }
}
