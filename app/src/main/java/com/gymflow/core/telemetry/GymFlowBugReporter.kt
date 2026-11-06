package com.gymflow.core.telemetry

import android.app.ActivityManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.gymflow.BuildConfig
import com.gymflow.core.updater.PackageSignatureVerifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

enum class BugCategory {
    APP_UPDATE,
    UNCAUGHT_CRASH,
    DELTA_PATCH,
    DATABASE_SYNC,
    NETWORK,
    USER_REPORT,
    SYSTEM
}

enum class BugSeverity {
    FATAL,
    ERROR,
    WARNING,
    INFO
}

/**
 * Enterprise Bug & Crash Telemetry Reporter.
 * Captures in-app bugs, update incompatibilities, and fatal crashes, storing complete
 * diagnostic payloads into Firebase Firestore under `/bug_reports`.
 */
object GymFlowBugReporter {

    private const val TAG = "GymFlowBugReporter"
    private const val FIRESTORE_COLLECTION = "bug_reports"
    private val scope = CoroutineScope(Dispatchers.IO)

    @Volatile
    private var appContext: Context? = null

    /**
     * Initializes the telemetry engine with application context and attaches the global crash handler.
     */
    fun initialize(context: Context) {
        appContext = context.applicationContext
        installGlobalCrashHandler()
        Log.i(TAG, "GymFlowBugReporter initialized with Firebase Firestore telemetry.")
    }

    /**
     * Reports any general application error or exception.
     */
    fun reportBug(
        category: BugCategory,
        title: String,
        error: Throwable? = null,
        severity: BugSeverity = BugSeverity.ERROR,
        metadata: Map<String, Any?> = emptyMap()
    ) {
        val context = appContext
        Log.e(TAG, "[$category] $title", error)

        scope.launch {
            try {
                val reportData = buildDiagnosticPayload(
                    context = context,
                    category = category,
                    severity = severity,
                    title = title,
                    error = error,
                    metadata = metadata
                )

                FirebaseFirestore.getInstance()
                    .collection(FIRESTORE_COLLECTION)
                    .document(reportData["reportId"] as String)
                    .set(reportData)
                    .addOnSuccessListener {
                        Log.d(TAG, "Diagnostic report logged to Firestore: ${reportData["reportId"]}")
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Failed writing bug report to Firestore: ${e.message}")
                    }
            } catch (e: Exception) {
                Log.w(TAG, "Error generating bug report: ${e.message}")
            }
        }
    }

    /**
     * Specialized reporter for app update and package installation errors.
     */
    fun reportUpdateError(
        title: String,
        error: Throwable? = null,
        targetApk: File? = null,
        metadata: Map<String, Any?> = emptyMap()
    ) {
        val extraData = metadata.toMutableMap()
        val context = appContext
        if (context != null && targetApk != null && targetApk.exists()) {
            val compat = PackageSignatureVerifier.verifyApkCompatibility(context, targetApk)
            extraData["targetApkPath"] = targetApk.absolutePath
            extraData["targetApkSizeBytes"] = targetApk.length()
            extraData["isCompatible"] = compat.isCompatible
            extraData["installedSignatureSha256"] = compat.installedSignatureSha256 ?: "none"
            extraData["targetSignatureSha256"] = compat.targetSignatureSha256 ?: "none"
            extraData["installedVersionCode"] = compat.installedVersionCode
            extraData["targetVersionCode"] = compat.targetVersionCode
            compat.errorMessage?.let { extraData["compatibilityErrorMessage"] = it }
        }

        reportBug(
            category = BugCategory.APP_UPDATE,
            title = title,
            error = error,
            severity = BugSeverity.ERROR,
            metadata = extraData
        )
    }

    /**
     * Allows users to manually submit bug reports / feedback from UI.
     */
    fun reportUserFeedback(
        userDescription: String,
        contactEmail: String? = null,
        metadata: Map<String, Any?> = emptyMap()
    ) {
        val extraData = metadata.toMutableMap()
        contactEmail?.let { extraData["contactEmail"] = it }

        reportBug(
            category = BugCategory.USER_REPORT,
            title = "User Submitted Report: ${userDescription.take(50)}",
            error = Exception("User Feedback: $userDescription"),
            severity = BugSeverity.INFO,
            metadata = extraData
        )
    }

    private fun installGlobalCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                Log.e(TAG, "CRITICAL: Uncaught exception in thread ${thread.name}", throwable)
                // Synchronously attempt to write crash report
                val context = appContext
                val payload = buildDiagnosticPayload(
                    context = context,
                    category = BugCategory.UNCAUGHT_CRASH,
                    severity = BugSeverity.FATAL,
                    title = "Fatal Crash in thread '${thread.name}': ${throwable.message}",
                    error = throwable,
                    metadata = mapOf("threadName" to thread.name, "threadId" to thread.id)
                )

                // Dispatch to Firestore (and wait briefly up to 1.5 seconds)
                val firestore = FirebaseFirestore.getInstance()
                val latch = java.util.concurrent.CountDownLatch(1)
                firestore.collection(FIRESTORE_COLLECTION)
                    .document(payload["reportId"] as String)
                    .set(payload)
                    .addOnCompleteListener { latch.countDown() }
                latch.await(1500, java.util.concurrent.TimeUnit.MILLISECONDS)
            } catch (e: Exception) {
                Log.e(TAG, "Failed handling uncaught exception telemetry", e)
            } finally {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }

    private fun buildDiagnosticPayload(
        context: Context?,
        category: BugCategory,
        severity: BugSeverity,
        title: String,
        error: Throwable?,
        metadata: Map<String, Any?>
    ): Map<String, Any?> {
        val reportId = UUID.randomUUID().toString()
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val timestampIso = isoFormat.format(Date())
        val timestampEpochMs = System.currentTimeMillis()

        val userId = try {
            FirebaseAuth.getInstance().currentUser?.uid ?: "unauthenticated"
        } catch (_: Exception) {
            "unauthenticated"
        }

        val installedSigSha256 = if (context != null) {
            PackageSignatureVerifier.getInstalledAppSignatureSha256(context) ?: "unknown"
        } else "unknown"

        val storageFreeMb = getFreeStorageMb()
        val memoryAvailableMb = getAvailableMemoryMb(context)
        val networkStatus = getNetworkStatus(context)

        val payload = mutableMapOf<String, Any?>(
            "reportId" to reportId,
            "timestampIso" to timestampIso,
            "timestampEpochMs" to timestampEpochMs,
            "category" to category.name,
            "severity" to severity.name,
            "title" to title,
            "errorMessage" to (error?.message ?: title),
            "stackTrace" to (error?.stackTraceToString() ?: ""),
            "appVersionName" to BuildConfig.VERSION_NAME,
            "appVersionCode" to BuildConfig.VERSION_CODE,
            "deviceModel" to Build.MODEL,
            "deviceManufacturer" to Build.MANUFACTURER,
            "deviceBrand" to Build.BRAND,
            "osVersion" to "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            "apiLevel" to Build.VERSION.SDK_INT,
            "packageSignatureSha256" to installedSigSha256,
            "storageFreeMb" to storageFreeMb,
            "memoryAvailableMb" to memoryAvailableMb,
            "networkStatus" to networkStatus,
            "userId" to userId
        )

        if (metadata.isNotEmpty()) {
            payload["metadata"] = metadata
        }

        return payload
    }

    private fun getFreeStorageMb(): Long {
        return try {
            val stat = StatFs(Environment.getDataDirectory().path)
            (stat.availableBlocksLong * stat.blockSizeLong) / (1024 * 1024)
        } catch (_: Exception) {
            -1L
        }
    }

    private fun getAvailableMemoryMb(context: Context?): Long {
        if (context == null) return -1L
        return try {
            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            actManager?.getMemoryInfo(memInfo)
            memInfo.availMem / (1024 * 1024)
        } catch (_: Exception) {
            -1L
        }
    }

    private fun getNetworkStatus(context: Context?): String {
        if (context == null) return "UNKNOWN"
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val network = cm?.activeNetwork ?: return "DISCONNECTED"
            val caps = cm.getNetworkCapabilities(network) ?: return "DISCONNECTED"
            when {
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WIFI"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "CELLULAR"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ETHERNET"
                else -> "CONNECTED_OTHER"
            }
        } catch (_: Exception) {
            "UNKNOWN"
        }
    }
}

