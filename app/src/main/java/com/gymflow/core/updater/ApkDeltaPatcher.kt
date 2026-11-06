package com.gymflow.core.updater

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.security.MessageDigest

/**
 * High-performance, self-contained APK Delta Patcher.
 *
 * Applies a binary delta patch against the currently installed APK (`context.applicationInfo.sourceDir`)
 * to reconstruct the updated target APK with zero JNI dependencies.
 * Validates cryptographic SHA-256 checksums to ensure 100% bit-for-bit integrity before installation.
 */
object ApkDeltaPatcher {

    private const val MAGIC_HEADER = "GFDIFF10"
    private const val CMD_COPY = 0x01.toByte()
    private const val CMD_DATA = 0x02.toByte()
    private const val CMD_EOF = 0x00.toByte()

    /**
     * Reconstructs [targetApkFile] by applying [patchFile] to [sourceApkFile].
     *
     * @param sourceApkFile The currently installed base.apk on device.
     * @param patchFile The downloaded delta patch file.
     * @param targetApkFile The destination output APK file.
     * @param expectedSha256 Optional expected SHA-256 hex string to verify output integrity.
     * @param onProgress Progress callback reporting 0.0f to 1.0f.
     */
    suspend fun applyPatch(
        sourceApkFile: File,
        patchFile: File,
        targetApkFile: File,
        expectedSha256: String? = null,
        onProgress: ((progress: Float) -> Unit)? = null
    ): Result<File> = withContext(Dispatchers.IO) {
        if (!sourceApkFile.exists() || sourceApkFile.length() == 0L) {
            return@withContext Result.failure(IllegalArgumentException("Source base APK does not exist or is empty"))
        }
        if (!patchFile.exists() || patchFile.length() == 0L) {
            return@withContext Result.failure(IllegalArgumentException("Patch file does not exist or is empty"))
        }

        if (targetApkFile.exists()) {
            targetApkFile.delete()
        }

        var sourceRaf: RandomAccessFile? = null
        var patchIn: BufferedInputStream? = null
        var targetOut: BufferedOutputStream? = null

        try {
            sourceRaf = RandomAccessFile(sourceApkFile, "r")
            patchIn = BufferedInputStream(FileInputStream(patchFile), 65536)
            targetOut = BufferedOutputStream(FileOutputStream(targetApkFile), 65536)

            // 1. Verify Magic Header
            val headerBuf = ByteArray(8)
            val headerRead = patchIn.read(headerBuf)
            if (headerRead != 8 || String(headerBuf, Charsets.US_ASCII) != MAGIC_HEADER) {
                return@withContext Result.failure(IllegalStateException("Invalid patch format: magic header mismatch"))
            }

            // 2. Read Target APK Expected Length
            val expectedLength = readLong(patchIn)
            val patchFileSize = patchFile.length()
            var writtenBytes = 0L
            val copyBuffer = ByteArray(65536)
            val dataBuffer = ByteArray(65536)

            var lastProgressReport = 0L

            while (true) {
                val cmdInt = patchIn.read()
                if (cmdInt == -1 || cmdInt.toByte() == CMD_EOF) {
                    break
                }

                when (cmdInt.toByte()) {
                    CMD_COPY -> {
                        // Copy from base.apk: offset (Long), length (Int)
                        val offset = readLong(patchIn)
                        val length = readInt(patchIn)
                        sourceRaf.seek(offset)

                        var remaining = length
                        while (remaining > 0) {
                            val chunk = remaining.coerceAtMost(copyBuffer.size)
                            sourceRaf.readFully(copyBuffer, 0, chunk)
                            targetOut.write(copyBuffer, 0, chunk)
                            remaining -= chunk
                            writtenBytes += chunk
                        }
                    }
                    CMD_DATA -> {
                        // Insert raw bytes from patch: length (Int), data bytes
                        val length = readInt(patchIn)
                        var remaining = length
                        while (remaining > 0) {
                            val chunk = remaining.coerceAtMost(dataBuffer.size)
                            val read = patchIn.read(dataBuffer, 0, chunk)
                            if (read == -1) throw IllegalStateException("Unexpected end of patch stream during CMD_DATA")
                            targetOut.write(dataBuffer, 0, read)
                            remaining -= read
                            writtenBytes += read
                        }
                    }
                    else -> {
                        throw IllegalStateException("Unknown command byte in patch: $cmdInt")
                    }
                }

                val now = System.currentTimeMillis()
                if (expectedLength > 0 && (now - lastProgressReport > 50 || writtenBytes == expectedLength)) {
                    lastProgressReport = now
                    val progress = (writtenBytes.toFloat() / expectedLength.toFloat()).coerceIn(0f, 1f)
                    onProgress?.invoke(progress)
                }
            }

            targetOut.flush()

            if (expectedLength > 0 && writtenBytes != expectedLength) {
                targetApkFile.delete()
                return@withContext Result.failure(
                    IllegalStateException("Reconstructed APK size ($writtenBytes) does not match expected size ($expectedLength)")
                )
            }

            // 3. Cryptographic Checksum Verification
            if (!expectedSha256.isNullOrBlank()) {
                val actualSha256 = calculateSha256(targetApkFile)
                if (!actualSha256.equals(expectedSha256.trim(), ignoreCase = true)) {
                    targetApkFile.delete()
                    return@withContext Result.failure(
                        IllegalStateException("Reconstructed APK SHA-256 verification failed (expected: $expectedSha256, actual: $actualSha256)")
                    )
                }
            }

            Result.success(targetApkFile)
        } catch (e: Exception) {
            targetApkFile.delete()
            Result.failure(e)
        } finally {
            try { sourceRaf?.close() } catch (_: Exception) {}
            try { patchIn?.close() } catch (_: Exception) {}
            try { targetOut?.close() } catch (_: Exception) {}
        }
    }

    /**
     * Resolves the device's currently installed base APK file path.
     */
    fun getInstalledBaseApk(context: Context): File? {
        val path = context.applicationInfo.sourceDir ?: return null
        val file = File(path)
        return if (file.exists() && file.canRead()) file else null
    }

    /**
     * Calculates the SHA-256 hex string of a file.
     */
    fun calculateSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { fis ->
            val buf = ByteArray(65536)
            var n: Int
            while (fis.read(buf).also { n = it } != -1) {
                digest.update(buf, 0, n)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun readLong(stream: BufferedInputStream): Long {
        val b0 = stream.read().toLong()
        val b1 = stream.read().toLong()
        val b2 = stream.read().toLong()
        val b3 = stream.read().toLong()
        val b4 = stream.read().toLong()
        val b5 = stream.read().toLong()
        val b6 = stream.read().toLong()
        val b7 = stream.read().toLong()
        return ((b0 and 0xFF) shl 56) or
                ((b1 and 0xFF) shl 48) or
                ((b2 and 0xFF) shl 40) or
                ((b3 and 0xFF) shl 32) or
                ((b4 and 0xFF) shl 24) or
                ((b5 and 0xFF) shl 16) or
                ((b6 and 0xFF) shl 8) or
                (b7 and 0xFF)
    }

    private fun readInt(stream: BufferedInputStream): Int {
        val b0 = stream.read()
        val b1 = stream.read()
        val b2 = stream.read()
        val b3 = stream.read()
        return ((b0 and 0xFF) shl 24) or
                ((b1 and 0xFF) shl 16) or
                ((b2 and 0xFF) shl 8) or
                (b3 and 0xFF)
    }
}

