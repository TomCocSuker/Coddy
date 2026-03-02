# Coddy

Coddy is a comprehensive Android application designed for advanced image, text, and file steganography. Hide and encrypt your private data inside images and text, manipulate image pixels, convert media formats, and encrypt raw text using military-grade AES-256 GCM encryption. It features zero native dependencies for core features and relies on pure Kotlin and modern AndroidX libraries.

## Features (6 Main Tabs)

1. **Zero-Width (Text Steganography) 📝**
   - Hide secret text inside innocent-looking visible cover text using invisible "zero-width" characters.
   - Ideal for covertly sharing secrets via messengers that do not filter invisible unicode characters.

2. **Crypto (AES-256 Encryption) 🔒**
   - Encrypt and decrypt raw text using military-grade AES-256 GCM with PBKDF2 key derivation.
   - Requires a password to decrypt the Base64-encoded encrypted payload safely.

3. **Encode & Decode (LSB Image Steganography) 🖼️**
   - **Encode:** Write secret text directly into the Least Significant Bits (LSB) of an image's Red, Green, and Blue subpixels. Maximum capacity is dynamically calculated.
   - **Decode:** Extract embedded text from previously encoded PNG images.

4. **Pic File (Picture WebP Steganography) 📸**
   - Take a secret picture or image, heavily compress it into a tiny WebP format, and hide the entire resulting file inside another larger cover image using LSB encoding.
   - Perfect for securely transporting uncompressed cover photos containing hidden photos.

5. **Hybrid (File Appending) 📁**
   - Hide an arbitrary small file (e.g., PDF, ZIP, APK) inside a cover image by physically appending its bytes directly to the end of the image file (Concat mode) or embedding it inside an ancillary iTXt PNG chunk (PNG Chunk mode).
   - Allows sharing files disguised as standard images on platforms that don't aggressively re-encode media.

6. **Convert & Merge 🎬🧩**
   - **Convert:** Transcode video (H.264/H.265) and audio (AAC/Opus), resize and compress photos, and extract audio tracks from existing video files natively using `androidx.media3.transformer`.
   - **Merge:** Hide a secret image inside a container image by manipulating the MSB/LSB bits. Viewable visually when extracted.

## Technologies Used
- **Kotlin & Jetpack Compose:** Built fully in modern Kotlin using Declarative UI.
- **AndroidX Media3:** Hardware-accelerated lossless media conversions and transcodings.
- **Coroutines:** Responsive background processing.
- **No Native NDK Dependencies:** Uses JVM/Android native implementations instead of massive C/C++ libraries like FFmpeg or GStreamer to keep the app size minimal (~10MB) and very battery-friendly.

## Build Instructions
Clone the repository and build via Gradle:
```bash
./gradlew assembleDebug
```

## Disclaimer
Note that while Coddy incorporates cryptographic features via AES-256 and various LSB steganographic algorithms, many automatic messengers re-compress images during transit, stripping the LSB metadata. Coddy provides options to use Zero-Width characters or "Send as Document/File" modes on messaging apps to work around this.

## License
MIT License.
