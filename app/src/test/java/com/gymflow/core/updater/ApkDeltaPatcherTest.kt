package com.gymflow.core.updater

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

class ApkDeltaPatcherTest {

    @Test
    fun testSyntheticPatchApplication() = runBlocking {
        // Create synthetic source file
        val tempDir = File(System.getProperty("java.io.tmpdir"), "patcher_test_${System.currentTimeMillis()}")
        tempDir.mkdirs()

        try {
            val sourceFile = File(tempDir, "source.bin")
            val targetFile = File(tempDir, "target.bin")
            val patchFile = File(tempDir, "patch.bin")
            val reconstructedFile = File(tempDir, "reconstructed.bin")

            // Source data: 100 KB
            val sourceData = ByteArray(100 * 1024) { (it % 251).toByte() }
            FileOutputStream(sourceFile).use { it.write(sourceData) }

            // Target data: modified at offset 10,000 with 500 new bytes
            val targetData = sourceData.clone()
            for (i in 10000 until 10500) {
                targetData[i] = ((i * 7) % 256).toByte()
            }
            FileOutputStream(targetFile).use { it.write(targetData) }

            val expectedSha256 = ApkDeltaPatcher.calculateSha256(targetFile)

            // Construct GFDIFF10 patch manually:
            // 1. Copy 0..10000 from source
            // 2. Insert 500 bytes of data
            // 3. Copy 10500..102400 from source
            FileOutputStream(patchFile).use { out ->
                out.write("GFDIFF10".toByteArray(Charsets.US_ASCII))
                // Length: 102400 (Long)
                val len = targetData.size.toLong()
                for (shift in 56 downTo 0 step 8) {
                    out.write(((len ushr shift) and 0xFF).toInt())
                }

                // CMD_COPY: offset 0, len 10000
                out.write(0x01)
                writeLong(out, 0L)
                writeInt(out, 10000)

                // CMD_DATA: len 500
                out.write(0x02)
                writeInt(out, 500)
                out.write(targetData, 10000, 500)

                // CMD_COPY: offset 10500, len 91900
                out.write(0x01)
                writeLong(out, 10500L)
                writeInt(out, 91900)

                // CMD_EOF
                out.write(0x00)
            }

            val result = ApkDeltaPatcher.applyPatch(
                sourceApkFile = sourceFile,
                patchFile = patchFile,
                targetApkFile = reconstructedFile,
                expectedSha256 = expectedSha256
            )

            assertTrue("Patch application should succeed: ${result.exceptionOrNull()?.message}", result.isSuccess)
            val output = result.getOrThrow()
            assertEquals("Output size must match target size", targetFile.length(), output.length())
            assertEquals("Output SHA-256 must match target SHA-256", expectedSha256, ApkDeltaPatcher.calculateSha256(output))
        } finally {
            tempDir.deleteRecursively()
        }
    }

    private fun writeLong(out: FileOutputStream, value: Long) {
        for (shift in 56 downTo 0 step 8) {
            out.write(((value ushr shift) and 0xFF).toInt())
        }
    }

    private fun writeInt(out: FileOutputStream, value: Int) {
        for (shift in 24 downTo 0 step 8) {
            out.write(((value ushr shift) and 0xFF).toInt())
        }
    }
}

