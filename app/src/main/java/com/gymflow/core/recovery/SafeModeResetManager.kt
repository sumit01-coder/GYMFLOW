package com.gymflow.core.recovery

import android.content.Context
import android.util.Log
import java.io.File

object SafeModeResetManager {

    private const val TAG = "SafeModeResetManager"

    /**
     * Purges corrupted local SQLite databases and temporary cache files.
     * Preserves secure SharedPreferences (such as authentication tokens)
     * so that the user's account session remains intact while Cloud Firestore
     * automatically re-hydrates healthy data upon restart.
     */
    fun performSafeModeReset(context: Context): Boolean {
        return try {
            val appCtx = context.applicationContext

            // 1. Delete Room database files (gymflow.db, -wal, -shm)
            val dbFolder = appCtx.getDatabasePath("gymflow.db").parentFile
            if (dbFolder != null && dbFolder.exists()) {
                val dbFiles = dbFolder.listFiles { file ->
                    file.name.startsWith("gymflow.db")
                }
                dbFiles?.forEach { file ->
                    val deleted = file.delete()
                    Log.i(TAG, "Deleted database file: ${file.name} (success=$deleted)")
                }
            }

            // 2. Clear application cache directory
            deleteRecursively(appCtx.cacheDir)
            appCtx.externalCacheDir?.let { deleteRecursively(it) }

            // 3. Clear Room temporary files and update download cache
            val updatesDir = File(appCtx.cacheDir, "updates")
            if (updatesDir.exists()) {
                deleteRecursively(updatesDir)
            }

            Log.i(TAG, "Safe mode local reset completed successfully.")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed executing safe mode reset", e)
            false
        }
    }

    private fun deleteRecursively(fileOrDir: File?): Boolean {
        if (fileOrDir == null || !fileOrDir.exists()) return true
        if (fileOrDir.isDirectory) {
            fileOrDir.listFiles()?.forEach { child ->
                deleteRecursively(child)
            }
        }
        return fileOrDir.delete()
    }
}
