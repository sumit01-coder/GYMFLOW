package com.gymflow.core.updater

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import java.io.File
import java.security.MessageDigest

/**
 * Validates APK cryptographic signatures and compatibility before triggering package installation.
 * Prevents the Android OS "App not installed as package conflicts with an existing package" failure.
 */
object PackageSignatureVerifier {

    data class CompatibilityResult(
        val isCompatible: Boolean,
        val installedSignatureSha256: String?,
        val targetSignatureSha256: String?,
        val installedVersionCode: Long,
        val targetVersionCode: Long,
        val errorMessage: String? = null
    )

    /**
     * Inspects whether [targetApkFile] can be cleanly updated over the currently running application.
     */
    fun verifyApkCompatibility(context: Context, targetApkFile: File): CompatibilityResult {
        if (!targetApkFile.exists() || targetApkFile.length() == 0L) {
            return CompatibilityResult(
                isCompatible = false,
                installedSignatureSha256 = null,
                targetSignatureSha256 = null,
                installedVersionCode = 0L,
                targetVersionCode = 0L,
                errorMessage = "Target APK file does not exist or is empty"
            )
        }

        val pm = context.packageManager
        val installedPkg = context.packageName

        // 1. Read installed package info
        val installedInfo = try {
            getPackageInfoCompat(pm, installedPkg, PackageManager.GET_SIGNING_CERTIFICATES or PackageManager.GET_SIGNATURES)
        } catch (e: Exception) {
            return CompatibilityResult(
                isCompatible = false,
                installedSignatureSha256 = null,
                targetSignatureSha256 = null,
                installedVersionCode = 0L,
                targetVersionCode = 0L,
                errorMessage = "Failed reading installed app info: ${e.message}"
            )
        }

        // 2. Read target APK info from archive
        val targetInfo = try {
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                PackageManager.GET_SIGNING_CERTIFICATES
            } else {
                @Suppress("DEPRECATION")
                PackageManager.GET_SIGNATURES
            }
            pm.getPackageArchiveInfo(targetApkFile.absolutePath, flags)
        } catch (e: Exception) {
            null
        }

        if (targetInfo == null) {
            return CompatibilityResult(
                isCompatible = false,
                installedSignatureSha256 = getSignatureSha256(installedInfo),
                targetSignatureSha256 = null,
                installedVersionCode = getVersionCode(installedInfo),
                targetVersionCode = 0L,
                errorMessage = "Android Package Manager could not parse APK archive. The APK file might be corrupted or incomplete."
            )
        }

        val installedSignSha256 = getSignatureSha256(installedInfo)
        val targetSignSha256 = getSignatureSha256(targetInfo)
        val installedVerCode = getVersionCode(installedInfo)
        val targetVerCode = getVersionCode(targetInfo)

        // 3. Compare Signatures
        if (!installedSignSha256.isNullOrBlank() && !targetSignSha256.isNullOrBlank()) {
            if (!installedSignSha256.equals(targetSignSha256, ignoreCase = true)) {
                return CompatibilityResult(
                    isCompatible = false,
                    installedSignatureSha256 = installedSignSha256,
                    targetSignatureSha256 = targetSignSha256,
                    installedVersionCode = installedVerCode,
                    targetVersionCode = targetVerCode,
                    errorMessage = "Signing Certificate Mismatch! The update APK was signed with a different key than the installed app. Installed: $installedSignSha256, Target: $targetSignSha256"
                )
            }
        }

        // 4. Version Code Downgrade Check
        if (targetVerCode < installedVerCode) {
            return CompatibilityResult(
                isCompatible = false,
                installedSignatureSha256 = installedSignSha256,
                targetSignatureSha256 = targetSignSha256,
                installedVersionCode = installedVerCode,
                targetVersionCode = targetVerCode,
                errorMessage = "Downgrade Rejected! Target APK versionCode ($targetVerCode) is lower than currently installed versionCode ($installedVerCode)."
            )
        }

        return CompatibilityResult(
            isCompatible = true,
            installedSignatureSha256 = installedSignSha256,
            targetSignatureSha256 = targetSignSha256,
            installedVersionCode = installedVerCode,
            targetVersionCode = targetVerCode,
            errorMessage = null
        )
    }

    fun getInstalledAppSignatureSha256(context: Context): String? {
        return try {
            val pm = context.packageManager
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                PackageManager.GET_SIGNING_CERTIFICATES
            } else {
                @Suppress("DEPRECATION")
                PackageManager.GET_SIGNATURES
            }
            val info = getPackageInfoCompat(pm, context.packageName, flags)
            getSignatureSha256(info)
        } catch (_: Exception) {
            null
        }
    }

    private fun getPackageInfoCompat(pm: PackageManager, packageName: String, flags: Int): PackageInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageInfo(packageName, flags)
        }
    }

    private fun getVersionCode(info: PackageInfo): Long {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            info.versionCode.toLong()
        }
    }

    private fun getSignatureSha256(info: PackageInfo): String? {
        val signatures: Array<Signature>? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.signingInfo?.let { signingInfo ->
                if (signingInfo.hasMultipleSigners()) {
                    signingInfo.apkContentsSigners
                } else {
                    signingInfo.signingCertificateHistory
                }
            }
        } else {
            @Suppress("DEPRECATION")
            info.signatures
        }

        val firstSig = signatures?.firstOrNull() ?: return null
        return calculateSha256(firstSig.toByteArray())
    }

    private fun calculateSha256(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(bytes)
        return hash.joinToString("") { "%02X".format(it) }
    }
}

