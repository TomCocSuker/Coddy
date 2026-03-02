package com.example.coddy

import android.graphics.Bitmap
import android.graphics.Color

/**
 * LSB Steganography Engine.
 *
 * Hides data in the Least Significant Bit of every R, G, B channel.
 * Layout: [32-bit byte-length header][payload bytes]
 * Capacity per image = width × height × 3 bits  (÷ 8 for bytes, − 4 for header).
 */
object SteganographyEngine {

    /** Maximum number of payload bytes this image can hold. */
    fun capacity(bitmap: Bitmap): Int {
        val totalBits = bitmap.width.toLong() * bitmap.height * 3
        val availableBits = totalBits - 32          // minus 32-bit header
        return (availableBits / 8).coerceAtLeast(0).toInt()
    }

    // ───────────────────────── ENCODE (text) ─────────────────────────

    /**
     * Encodes a UTF-8 [message] into [source] using LSB steganography.
     */
    fun encode(source: Bitmap, message: String): Bitmap {
        return encodeBytes(source, message.toByteArray(Charsets.UTF_8))
    }

    // ───────────────────────── DECODE (text) ─────────────────────────

    /**
     * Decodes a UTF-8 message previously embedded by [encode].
     */
    fun decode(bitmap: Bitmap): String? {
        val bytes = decodeBytes(bitmap) ?: return null
        return try {
            String(bytes, Charsets.UTF_8)
        } catch (_: Exception) {
            null
        }
    }

    // ───────────────────────── ENCODE BYTES ─────────────────────────

    /**
     * Encodes raw [data] bytes into [source] using LSB steganography on R, G, B channels.
     *
     * @return a new Bitmap with the data embedded.
     * @throws IllegalArgumentException if the data won't fit.
     */
    fun encodeBytes(source: Bitmap, data: ByteArray): Bitmap {
        val cap = capacity(source)
        require(data.size <= cap) {
            "Data too long: ${data.size} bytes, capacity $cap bytes"
        }

        // Total bits = 32 header + payload
        val totalBits = 32 + data.size * 8

        // Build flat bit array
        val bits = BooleanArray(totalBits)

        // Header: 32-bit big-endian length
        val length = data.size
        for (i in 0 until 32) {
            bits[i] = ((length shr (31 - i)) and 1) == 1
        }

        // Payload
        for (byteIdx in data.indices) {
            val b = data[byteIdx].toInt() and 0xFF
            for (bitIdx in 0 until 8) {
                bits[32 + byteIdx * 8 + bitIdx] = ((b shr (7 - bitIdx)) and 1) == 1
            }
        }

        // Mutable copy
        val result = source.copy(Bitmap.Config.ARGB_8888, true)
        val w = result.width
        var bitPointer = 0

        // Read all pixels into an array for performance
        val pixelCount = result.width * result.height
        val pixels = IntArray(pixelCount)
        result.getPixels(pixels, 0, w, 0, 0, result.width, result.height)

        for (i in 0 until pixelCount) {
            if (bitPointer >= totalBits) break
            var pixel = pixels[i]
            val a = Color.alpha(pixel)

            // Red channel
            if (bitPointer < totalBits) {
                val r = (Color.red(pixel) and 0xFE) or (if (bits[bitPointer]) 1 else 0)
                pixel = Color.argb(a, r, Color.green(pixel), Color.blue(pixel))
                bitPointer++
            }
            // Green channel
            if (bitPointer < totalBits) {
                val g = (Color.green(pixel) and 0xFE) or (if (bits[bitPointer]) 1 else 0)
                pixel = Color.argb(a, Color.red(pixel), g, Color.blue(pixel))
                bitPointer++
            }
            // Blue channel
            if (bitPointer < totalBits) {
                val b2 = (Color.blue(pixel) and 0xFE) or (if (bits[bitPointer]) 1 else 0)
                pixel = Color.argb(a, Color.red(pixel), Color.green(pixel), b2)
                bitPointer++
            }

            pixels[i] = pixel
        }

        result.setPixels(pixels, 0, w, 0, 0, result.width, result.height)
        return result
    }

    // ───────────────────────── DECODE BYTES ─────────────────────────

    /**
     * Decodes raw bytes previously embedded by [encodeBytes].
     *
     * @return the decoded byte array, or null if the header is invalid.
     */
    fun decodeBytes(bitmap: Bitmap): ByteArray? {
        val w = bitmap.width
        val pixelCount = bitmap.width * bitmap.height
        val pixels = IntArray(pixelCount)
        bitmap.getPixels(pixels, 0, w, 0, 0, bitmap.width, bitmap.height)

        // We need at least 32 bits for the header → 11 pixels minimum
        if (pixelCount < 11) return null

        // Read 32-bit header
        var length = 0
        for (i in 0 until 32) {
            val bit = readBit(pixels, i)
            length = (length shl 1) or bit
        }

        // Sanity check
        if (length <= 0 || length > capacity(bitmap)) return null

        // Read payload bytes
        val totalBits = 32 + length * 8
        val maxBits = pixelCount.toLong() * 3
        if (totalBits > maxBits) return null

        val bytes = ByteArray(length)
        for (byteIdx in 0 until length) {
            var b = 0
            for (bitIdx in 0 until 8) {
                val globalBit = 32 + byteIdx * 8 + bitIdx
                val bit = readBit(pixels, globalBit)
                b = (b shl 1) or bit
            }
            bytes[byteIdx] = b.toByte()
        }

        return bytes
    }

    /** Reads a single bit from the pixel array at a given bit-index. */
    private fun readBit(pixels: IntArray, bitIndex: Int): Int {
        val pixelIdx = bitIndex / 3
        return when (bitIndex % 3) {
            0 -> Color.red(pixels[pixelIdx]) and 1
            1 -> Color.green(pixels[pixelIdx]) and 1
            2 -> Color.blue(pixels[pixelIdx]) and 1
            else -> 0
        }
    }
}
