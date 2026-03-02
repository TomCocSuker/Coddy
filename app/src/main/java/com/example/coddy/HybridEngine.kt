package com.example.coddy

import android.content.ContentResolver
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.zip.CRC32

/**
 * File Hybridization Engine.
 *
 * Creates polyglot files that can be opened as different formats:
 * - JPEG/PNG/GIF + ZIP/RAR/7z: simple concatenation (image read from start, archive from end)
 * - PNG + any file: inserts data as a private PNG chunk ("cdDy") before IEND
 *
 * Extraction reverses the process.
 */
object HybridEngine {

    // PNG signature: 137 80 78 71 13 10 26 10
    private val PNG_SIGNATURE = byteArrayOf(
        0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    )

    // JPEG SOI marker
    private val JPEG_SOI = byteArrayOf(0xFF.toByte(), 0xD8.toByte())
    // JPEG EOI marker
    private val JPEG_EOI = byteArrayOf(0xFF.toByte(), 0xD9.toByte())

    // GIF trailer
    private const val GIF_TRAILER: Byte = 0x3B

    // Our custom PNG chunk type
    private val CHUNK_TYPE = "cdDy".toByteArray(Charsets.US_ASCII)

    enum class CoverFormat { JPEG, PNG, GIF, UNKNOWN }
    enum class HybridMode { CONCAT, PNG_CHUNK }

    /**
     * Detect the format of an image from its raw bytes.
     */
    fun detectFormat(data: ByteArray): CoverFormat {
        if (data.size < 8) return CoverFormat.UNKNOWN
        if (data[0] == JPEG_SOI[0] && data[1] == JPEG_SOI[1]) return CoverFormat.JPEG
        if (data.take(8).toByteArray().contentEquals(PNG_SIGNATURE)) return CoverFormat.PNG
        if (data.size >= 6) {
            val header = String(data, 0, 6, Charsets.US_ASCII)
            if (header == "GIF87a" || header == "GIF89a") return CoverFormat.GIF
        }
        return CoverFormat.UNKNOWN
    }

    /**
     * Hybridize [coverBytes] (image) with [secretBytes] (any file).
     *
     * For JPEG/GIF + archive: simple concatenation.
     * For PNG + file via chunk: inserts a custom "cdDy" chunk before IEND.
     *
     * @param mode CONCAT for simple concatenation, PNG_CHUNK for PNG private chunk injection.
     * @return The hybrid file bytes.
     */
    fun hybridize(coverBytes: ByteArray, secretBytes: ByteArray, mode: HybridMode): ByteArray {
        return when (mode) {
            HybridMode.CONCAT -> concat(coverBytes, secretBytes)
            HybridMode.PNG_CHUNK -> injectPngChunk(coverBytes, secretBytes)
        }
    }

    /**
     * Extract the secret file from a hybrid.
     *
     * @param mode CONCAT → extract bytes after image end marker; PNG_CHUNK → extract "cdDy" chunk.
     */
    fun extract(hybridBytes: ByteArray, mode: HybridMode): ByteArray? {
        return when (mode) {
            HybridMode.CONCAT -> extractConcat(hybridBytes)
            HybridMode.PNG_CHUNK -> extractPngChunk(hybridBytes)
        }
    }

    // ───────────────────── CONCATENATION ─────────────────────

    // 8-byte marker between cover and secret: "CDDY" + 4-byte cover size (big-endian)
    private val CONCAT_MAGIC = "CDDY".toByteArray(Charsets.US_ASCII)

    private fun concat(cover: ByteArray, secret: ByteArray): ByteArray {
        val out = ByteArrayOutputStream(cover.size + 8 + secret.size)
        out.write(cover)
        // Write marker: magic + cover size
        out.write(CONCAT_MAGIC)
        out.write(ByteBuffer.allocate(4).putInt(cover.size).array())
        out.write(secret)
        return out.toByteArray()
    }

    private fun extractConcat(hybrid: ByteArray): ByteArray? {
        // Strategy 1: Find our CDDY marker
        val markerPos = findCddyMarker(hybrid)
        if (markerPos != null) {
            val secretStart = markerPos + 8 // skip "CDDY" + 4-byte size
            if (secretStart < hybrid.size) {
                return hybrid.copyOfRange(secretStart, hybrid.size)
            }
        }

        // Strategy 2: Find archive signature as fallback
        val archiveStart = findArchiveStart(hybrid)
        if (archiveStart != null && archiveStart < hybrid.size) {
            return hybrid.copyOfRange(archiveStart, hybrid.size)
        }

        return null
    }

    private fun findCddyMarker(data: ByteArray): Int? {
        // Search for "CDDY" marker
        if (data.size < 8) return null
        for (i in 0..data.size - 8) {
            if (data[i] == CONCAT_MAGIC[0] && data[i + 1] == CONCAT_MAGIC[1] &&
                data[i + 2] == CONCAT_MAGIC[2] && data[i + 3] == CONCAT_MAGIC[3]
            ) {
                // Verify: the 4 bytes after "CDDY" should equal the position (cover size == i)
                val storedSize = ByteBuffer.wrap(data, i + 4, 4).int
                if (storedSize == i) {
                    return i
                }
            }
        }
        return null
    }

    /**
     * Fallback: find the start of a known archive format.
     * Searches from a minimum offset to skip image headers.
     */
    private fun findArchiveStart(data: ByteArray): Int? {
        val minOffset = 100 // skip past image header
        if (data.size < minOffset + 4) return null

        // ZIP: PK\x03\x04
        val zipSig = byteArrayOf(0x50, 0x4B, 0x03, 0x04)
        // RAR4: Rar!\x1A\x07\x00
        val rarSig = byteArrayOf(0x52, 0x61, 0x72, 0x21, 0x1A, 0x07)
        // 7z: 7z\xBC\xAF\x27\x1C
        val sevenzSig = byteArrayOf(0x37, 0x7A, 0xBC.toByte(), 0xAF.toByte(), 0x27, 0x1C)

        for (i in minOffset..data.size - 4) {
            // Check ZIP
            if (data[i] == zipSig[0] && data[i + 1] == zipSig[1] &&
                data[i + 2] == zipSig[2] && data[i + 3] == zipSig[3]
            ) {
                return i
            }
            // Check RAR (need at least 6 bytes)
            if (i + 6 <= data.size &&
                data[i] == rarSig[0] && data[i + 1] == rarSig[1] &&
                data[i + 2] == rarSig[2] && data[i + 3] == rarSig[3] &&
                data[i + 4] == rarSig[4] && data[i + 5] == rarSig[5]
            ) {
                return i
            }
            // Check 7z (need at least 6 bytes)
            if (i + 6 <= data.size &&
                data[i] == sevenzSig[0] && data[i + 1] == sevenzSig[1] &&
                data[i + 2] == sevenzSig[2] && data[i + 3] == sevenzSig[3] &&
                data[i + 4] == sevenzSig[4] && data[i + 5] == sevenzSig[5]
            ) {
                return i
            }
        }
        return null
    }

    // ───────────────────── PNG CHUNK INJECTION ─────────────────────

    /**
     * Injects [secret] as a private "cdDy" chunk before the IEND chunk.
     */
    private fun injectPngChunk(pngBytes: ByteArray, secret: ByteArray): ByteArray {
        val format = detectFormat(pngBytes)
        require(format == CoverFormat.PNG) { "Cover must be a PNG file for chunk injection" }

        // Find IEND position (the length field, 4 bytes before "IEND" text)
        val iendTextPos = findIendTextPosition(pngBytes)
            ?: throw IllegalArgumentException("Invalid PNG: IEND chunk not found")
        val iendStart = iendTextPos - 4 // start of the length field

        val out = ByteArrayOutputStream(pngBytes.size + secret.size + 12)

        // Write everything before IEND
        out.write(pngBytes, 0, iendStart)

        // Write our custom chunk
        writeChunk(out, CHUNK_TYPE, secret)

        // Write IEND chunk (from iendStart to end)
        out.write(pngBytes, iendStart, pngBytes.size - iendStart)

        return out.toByteArray()
    }

    /**
     * Extracts data from the "cdDy" private chunk.
     */
    private fun extractPngChunk(pngBytes: ByteArray): ByteArray? {
        val format = detectFormat(pngBytes)
        if (format != CoverFormat.PNG) return null

        var offset = 8 // skip PNG signature

        while (offset + 8 <= pngBytes.size) {
            val length = ByteBuffer.wrap(pngBytes, offset, 4).int
            val type = String(pngBytes, offset + 4, 4, Charsets.US_ASCII)

            if (type == "cdDy") {
                if (offset + 8 + length > pngBytes.size) return null
                return pngBytes.copyOfRange(offset + 8, offset + 8 + length)
            }

            // Skip to next chunk: length field(4) + type(4) + data(length) + CRC(4)
            offset += 4 + 4 + length + 4

            if (type == "IEND") break
        }

        return null
    }

    private fun findIendTextPosition(data: ByteArray): Int? {
        val iend = "IEND".toByteArray(Charsets.US_ASCII)
        for (i in 0..data.size - 4) {
            if (data[i] == iend[0] && data[i + 1] == iend[1] &&
                data[i + 2] == iend[2] && data[i + 3] == iend[3]
            ) {
                return i
            }
        }
        return null
    }

    private fun writeChunk(out: ByteArrayOutputStream, type: ByteArray, data: ByteArray) {
        // Length (4 bytes big-endian)
        val lenBuf = ByteBuffer.allocate(4).putInt(data.size).array()
        out.write(lenBuf)

        // Type (4 bytes)
        out.write(type)

        // Data
        out.write(data)

        // CRC32 of type + data
        val crc = CRC32()
        crc.update(type)
        crc.update(data)
        val crcBuf = ByteBuffer.allocate(4).putInt(crc.value.toInt()).array()
        out.write(crcBuf)
    }

    /**
     * Read all bytes from a content URI.
     */
    fun readBytes(resolver: ContentResolver, uri: Uri): ByteArray? {
        return try {
            resolver.openInputStream(uri)?.use { it.readBytes() }
        } catch (_: Exception) {
            null
        }
    }
}
