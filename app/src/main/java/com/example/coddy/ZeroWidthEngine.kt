package com.example.coddy

/**
 * Zero-Width Characters Steganography Engine.
 *
 * Uses invisible mathematical operators that survive Android/Telegram processing:
 * - U+2060 (Word Joiner)            → bit 0
 * - U+2062 (Invisible Times)        → bit 1
 * - U+2063 (Invisible Separator)    → start delimiter
 * - U+2064 (Invisible Plus)         → end delimiter
 *
 * Algorithm (Encode):
 * 1. Each character of the secret → UTF-16 code point → 16 bits.
 * 2. Bits mapped: 0 → U+2060, 1 → U+2062.
 * 3. Payload wrapped: U+2063 ... U+2064.
 * 4. Inserted in the middle of cover text (not at edges — Telegram trims edges).
 *
 * Algorithm (Decode):
 * 1. Find text between U+2063 and U+2064 delimiters.
 * 2. Read only U+2060 (0) and U+2062 (1), ignore everything else.
 * 3. First 16 bits → message length. Then read that many 16-bit characters.
 */
object ZeroWidthEngine {

    private const val CHAR_ZERO  = '\u2060'  // Word Joiner → bit 0
    private const val CHAR_ONE   = '\u2062'  // Invisible Times → bit 1
    private const val CHAR_START = '\u2063'  // Invisible Separator → start
    private const val CHAR_END   = '\u2064'  // Invisible Plus → end

    private const val BITS_PER_CHAR = 16

    /**
     * Encode [secret] into [coverText].
     */
    fun encode(coverText: String, secret: String): String {
        require(coverText.isNotEmpty()) { "Cover text must not be empty" }
        require(secret.isNotEmpty()) { "Secret message must not be empty" }
        require(secret.length <= 65535) { "Secret too long (max 65535 chars)" }

        val payload = StringBuilder()
        payload.append(CHAR_START)

        // First 16 bits: message length
        val length = secret.length
        for (bit in BITS_PER_CHAR - 1 downTo 0) {
            payload.append(if ((length shr bit) and 1 == 1) CHAR_ONE else CHAR_ZERO)
        }

        // Each character as 16 bits
        for (ch in secret) {
            val code = ch.code
            for (bit in BITS_PER_CHAR - 1 downTo 0) {
                payload.append(if ((code shr bit) and 1 == 1) CHAR_ONE else CHAR_ZERO)
            }
        }

        payload.append(CHAR_END)

        // Insert in the middle of cover text — Telegram trims invisible chars at edges
        val mid = coverText.length / 2
        return coverText.substring(0, mid) + payload.toString() + coverText.substring(mid)
    }

    /**
     * Decode a secret message from [text].
     *
     * Finds delimiters U+2063...U+2064, reads only U+2060 (0) and U+2062 (1) between them.
     * Ignores all other characters.
     */
    fun decode(text: String): String? {
        // Find payload between delimiters
        val regex = "$CHAR_START(.*?)$CHAR_END".toRegex(RegexOption.DOT_MATCHES_ALL)
        val match = regex.find(text) ?: return null
        val payloadRaw = match.groupValues[1]

        // Extract only our bit characters
        val bits = StringBuilder()
        for (ch in payloadRaw) {
            when (ch) {
                CHAR_ZERO -> bits.append('0')
                CHAR_ONE  -> bits.append('1')
                // Ignore everything else (messenger formatting, etc.)
            }
        }

        // Need at least 16 bits for the length header
        if (bits.length < BITS_PER_CHAR) return null

        // Read length from first 16 bits
        var msgLength = 0
        for (bit in 0 until BITS_PER_CHAR) {
            msgLength = (msgLength shl 1) or (if (bits[bit] == '1') 1 else 0)
        }

        if (msgLength <= 0 || msgLength > 65535) return null

        // Check we have enough bits
        val totalBitsNeeded = BITS_PER_CHAR + msgLength * BITS_PER_CHAR
        if (bits.length < totalBitsNeeded) return null

        // Decode characters
        val result = StringBuilder()
        for (i in 0 until msgLength) {
            val offset = BITS_PER_CHAR + i * BITS_PER_CHAR
            var code = 0
            for (bit in 0 until BITS_PER_CHAR) {
                code = (code shl 1) or (if (bits[offset + bit] == '1') 1 else 0)
            }
            result.append(code.toChar())
        }

        return if (result.isNotEmpty()) result.toString() else null
    }

    /**
     * Strip all invisible characters from [text], returning only visible text.
     */
    fun stripInvisible(text: String): String {
        return text.filterNot {
            it == CHAR_ZERO || it == CHAR_ONE || it == CHAR_START || it == CHAR_END
        }
    }
}
