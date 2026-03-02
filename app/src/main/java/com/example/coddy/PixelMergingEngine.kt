package com.example.coddy

import android.graphics.Bitmap
import android.graphics.Color

/**
 * Pixel Merging Engine.
 *
 * Hides a secret image inside a container image using MSB/LSB bit splitting.
 *
 * Each color channel (R, G, B) is 8 bits:
 * - Upper 4 bits (MSB) carry ~90% of the visual information.
 * - Lower 4 bits (LSB) carry fine detail.
 *
 * **Merge**: container_MSB4 | secret_MSB4  →  result looks like container but carries secret.
 * **Extract**: (merged_LSB4) << 4  →  recovers the secret image (with 4-bit color depth).
 *
 * The secret image may be smaller than the container; unaffected container pixels
 * have their LSBs zeroed for consistency.
 */
object PixelMergingEngine {

    /**
     * Merge [secret] into [container].
     *
     * @param container The carrier image.
     * @param secret    The image to hide. Must be ≤ container in both width and height.
     * @return A new Bitmap that looks like [container] but stores [secret] in its lower bits.
     * @throws IllegalArgumentException if secret is larger than container.
     */
    fun merge(container: Bitmap, secret: Bitmap): Bitmap {
        require(secret.width <= container.width && secret.height <= container.height) {
            "Secret image (${secret.width}×${secret.height}) must be ≤ container (${container.width}×${container.height})"
        }

        val w = container.width
        val h = container.height
        val result = container.copy(Bitmap.Config.ARGB_8888, true)

        val containerPixels = IntArray(w * h)
        result.getPixels(containerPixels, 0, w, 0, 0, w, h)

        val sw = secret.width
        val sh = secret.height
        val secretPixels = IntArray(sw * sh)
        secret.getPixels(secretPixels, 0, sw, 0, 0, sw, sh)

        for (y in 0 until h) {
            for (x in 0 until w) {
                val ci = y * w + x
                val cp = containerPixels[ci]
                val a = Color.alpha(cp)

                if (x < sw && y < sh) {
                    // Merge: container MSB4 | secret MSB4
                    val si = y * sw + x
                    val sp = secretPixels[si]

                    val r = (Color.red(cp) and 0xF0) or (Color.red(sp) shr 4)
                    val g = (Color.green(cp) and 0xF0) or (Color.green(sp) shr 4)
                    val b = (Color.blue(cp) and 0xF0) or (Color.blue(sp) shr 4)
                    containerPixels[ci] = Color.argb(a, r, g, b)
                } else {
                    // No secret pixel → zero the LSBs for clean extraction
                    val r = Color.red(cp) and 0xF0
                    val g = Color.green(cp) and 0xF0
                    val b = Color.blue(cp) and 0xF0
                    containerPixels[ci] = Color.argb(a, r, g, b)
                }
            }
        }

        result.setPixels(containerPixels, 0, w, 0, 0, w, h)
        return result
    }

    /**
     * Extract the hidden image from a [merged] bitmap.
     *
     * Takes the lower 4 bits of each channel and shifts them to the upper 4 bits.
     * The resulting image has 4-bit-per-channel color depth.
     *
     * @return A Bitmap containing the recovered secret image (full size of merged, but
     *         pixels outside the original secret area will be black).
     */
    fun extract(merged: Bitmap): Bitmap {
        val w = merged.width
        val h = merged.height
        val pixels = IntArray(w * h)
        merged.getPixels(pixels, 0, w, 0, 0, w, h)

        for (i in pixels.indices) {
            val p = pixels[i]
            // Shift lower 4 bits to upper, duplicate into lower for fuller range
            val r = (Color.red(p) and 0x0F).let { (it shl 4) or it }
            val g = (Color.green(p) and 0x0F).let { (it shl 4) or it }
            val b = (Color.blue(p) and 0x0F).let { (it shl 4) or it }
            pixels[i] = Color.argb(255, r, g, b)
        }

        val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        result.setPixels(pixels, 0, w, 0, 0, w, h)
        return result
    }
}
