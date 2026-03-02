package com.example.coddy

import android.app.Application
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {

    // ── UI state ──
    var selectedImageUri by mutableStateOf<Uri?>(null)
        private set
    var selectedBitmap by mutableStateOf<Bitmap?>(null)
        private set
    var resultBitmap by mutableStateOf<Bitmap?>(null)
        private set
    var decodedText by mutableStateOf<String?>(null)
        private set
    var isProcessing by mutableStateOf(false)
        private set
    var statusMessage by mutableStateOf<String?>(null)
        private set
    var capacityBytes by mutableIntStateOf(0)
        private set

    // ── Pixel Merging state ──
    var secretBitmap by mutableStateOf<Bitmap?>(null)
        private set
    var extractedBitmap by mutableStateOf<Bitmap?>(null)
        private set

    // ── PicAsFile state ──
    var decodedBitmap by mutableStateOf<Bitmap?>(null)
        private set

    // ── ZeroWidth state ──
    var zwResultText by mutableStateOf<String?>(null)
        private set
    var zwDecodedText by mutableStateOf<String?>(null)
        private set

    // ── Hybrid state ──
    var coverBytes by mutableStateOf<ByteArray?>(null)
        private set
    var secretFileBytes by mutableStateOf<ByteArray?>(null)
        private set
    var hybridResult by mutableStateOf<ByteArray?>(null)
        private set
    var coverFormat by mutableStateOf("")
        private set

    // ── Media Converter state ──
    var mediaInfo by mutableStateOf<MediaConverterEngine.MediaInfo?>(null)
        private set
    var conversionDone by mutableStateOf(false)
        private set

    // ── Crypto state ──
    var cryptoResultText by mutableStateOf<String?>(null)
        private set

    // ── Image selection ──

    fun onImageSelected(uri: Uri?) {
        uri ?: return
        selectedImageUri = uri
        resultBitmap = null
        decodedText = null
        statusMessage = null

        viewModelScope.launch {
            val bmp = withContext(Dispatchers.IO) {
                loadBitmap(uri)
            }
            selectedBitmap = bmp
            capacityBytes = if (bmp != null) SteganographyEngine.capacity(bmp) else 0
        }
    }

    // ── Encode ──

    fun encode(message: String) {
        val bmp = selectedBitmap ?: run {
            statusMessage = getApp().getString(R.string.error_no_image)
            return
        }
        if (message.isBlank()) {
            statusMessage = getApp().getString(R.string.error_empty_message)
            return
        }
        val msgBytes = message.toByteArray(Charsets.UTF_8)
        if (msgBytes.size > capacityBytes) {
            statusMessage = getApp().getString(R.string.error_message_too_long)
            return
        }

        isProcessing = true
        statusMessage = getApp().getString(R.string.processing)

        viewModelScope.launch {
            try {
                val encoded = withContext(Dispatchers.Default) {
                    SteganographyEngine.encode(bmp, message)
                }
                resultBitmap = encoded
                statusMessage = getApp().getString(R.string.encode_success)
            } catch (e: Exception) {
                statusMessage = e.message ?: "Encoding error"
            } finally {
                isProcessing = false
            }
        }
    }

    // ── Decode ──

    fun decode() {
        val bmp = selectedBitmap ?: run {
            statusMessage = getApp().getString(R.string.error_no_image)
            return
        }

        isProcessing = true
        statusMessage = getApp().getString(R.string.processing)

        viewModelScope.launch {
            try {
                val text = withContext(Dispatchers.Default) {
                    SteganographyEngine.decode(bmp)
                }
                if (text != null) {
                    decodedText = text
                    statusMessage = getApp().getString(R.string.decode_success)
                } else {
                    decodedText = null
                    statusMessage = getApp().getString(R.string.error_decode_failed)
                }
            } catch (e: Exception) {
                statusMessage = e.message ?: "Decoding error"
            } finally {
                isProcessing = false
            }
        }
    }

    // ── Save to gallery ──

    fun saveToGallery() {
        val bmp = resultBitmap ?: return

        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) {
                saveBitmapToMediaStore(bmp)
            }
            statusMessage = if (success) {
                getApp().getString(R.string.saved_to_gallery)
            } else {
                getApp().getString(R.string.error_save_failed)
            }
        }
    }

    // ── Reset ──

    fun reset() {
        selectedImageUri = null
        selectedBitmap = null
        resultBitmap = null
        decodedText = null
        statusMessage = null
        capacityBytes = 0
        secretBitmap = null
        extractedBitmap = null
        decodedBitmap = null
        zwResultText = null
        zwDecodedText = null
        coverBytes = null
        secretFileBytes = null
        hybridResult = null
        coverFormat = ""
        inputMediaUri = null
        mediaInfo = null
        conversionDone = false
        cryptoResultText = null
    }

    // ── Pixel Merging ──

    fun onSecretImageSelected(uri: Uri?) {
        uri ?: return
        viewModelScope.launch {
            secretBitmap = withContext(Dispatchers.IO) { loadBitmap(uri) }
        }
    }

    fun mergeImages() {
        val container = selectedBitmap ?: run {
            statusMessage = getApp().getString(R.string.error_no_image)
            return
        }
        val secret = secretBitmap ?: run {
            statusMessage = getApp().getString(R.string.error_no_secret)
            return
        }
        if (secret.width > container.width || secret.height > container.height) {
            statusMessage = getApp().getString(R.string.error_secret_too_large)
            return
        }

        isProcessing = true
        statusMessage = getApp().getString(R.string.processing)

        viewModelScope.launch {
            try {
                val merged = withContext(Dispatchers.Default) {
                    PixelMergingEngine.merge(container, secret)
                }
                resultBitmap = merged
                statusMessage = getApp().getString(R.string.merge_success)
            } catch (e: Exception) {
                statusMessage = e.message ?: "Merge error"
            } finally {
                isProcessing = false
            }
        }
    }

    fun extractImage() {
        val bmp = selectedBitmap ?: run {
            statusMessage = getApp().getString(R.string.error_no_image)
            return
        }

        isProcessing = true
        statusMessage = getApp().getString(R.string.processing)

        viewModelScope.launch {
            try {
                val extracted = withContext(Dispatchers.Default) {
                    PixelMergingEngine.extract(bmp)
                }
                extractedBitmap = extracted
                resultBitmap = extracted
                statusMessage = getApp().getString(R.string.extract_success)
            } catch (e: Exception) {
                statusMessage = e.message ?: "Extract error"
            } finally {
                isProcessing = false
            }
        }
    }

    // ── Picture as File ──

    fun encodePicture() {
        val container = selectedBitmap ?: run {
            statusMessage = getApp().getString(R.string.error_no_image)
            return
        }
        val secret = secretBitmap ?: run {
            statusMessage = getApp().getString(R.string.error_no_secret)
            return
        }

        isProcessing = true
        statusMessage = getApp().getString(R.string.processing)

        viewModelScope.launch {
            try {
                val encoded = withContext(Dispatchers.Default) {
                    // Compress secret image to WebP bytes
                    val stream = java.io.ByteArrayOutputStream()
                    secret.compress(Bitmap.CompressFormat.WEBP_LOSSY, 75, stream)
                    val imageBytes = stream.toByteArray()

                    // Check capacity
                    val cap = SteganographyEngine.capacity(container)
                    if (imageBytes.size > cap) {
                        throw IllegalArgumentException(
                            getApp().getString(R.string.error_pic_too_large, imageBytes.size / 1024, cap / 1024)
                        )
                    }

                    SteganographyEngine.encodeBytes(container, imageBytes)
                }
                resultBitmap = encoded
                statusMessage = getApp().getString(R.string.pic_encode_success)
            } catch (e: Exception) {
                statusMessage = e.message ?: "Encoding error"
            } finally {
                isProcessing = false
            }
        }
    }

    fun decodePicture() {
        val bmp = selectedBitmap ?: run {
            statusMessage = getApp().getString(R.string.error_no_image)
            return
        }

        isProcessing = true
        statusMessage = getApp().getString(R.string.processing)

        viewModelScope.launch {
            try {
                val bitmap = withContext(Dispatchers.Default) {
                    val bytes = SteganographyEngine.decodeBytes(bmp)
                        ?: throw IllegalArgumentException(
                            getApp().getString(R.string.error_pic_decode_failed)
                        )
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        ?: throw IllegalArgumentException(
                            getApp().getString(R.string.error_pic_decode_failed)
                        )
                }
                decodedBitmap = bitmap
                resultBitmap = bitmap
                statusMessage = getApp().getString(R.string.pic_decode_success)
            } catch (e: Exception) {
                statusMessage = e.message ?: "Decoding error"
            } finally {
                isProcessing = false
            }
        }
    }

    // ── Zero-Width Characters ──

    fun encodeZeroWidth(coverText: String, secret: String) {
        if (coverText.isBlank()) {
            statusMessage = getApp().getString(R.string.error_empty_cover)
            return
        }
        if (secret.isBlank()) {
            statusMessage = getApp().getString(R.string.error_empty_message)
            return
        }

        try {
            zwResultText = ZeroWidthEngine.encode(coverText, secret)
            statusMessage = getApp().getString(R.string.zw_encode_success)
        } catch (e: Exception) {
            statusMessage = e.message ?: "Encoding error"
        }
    }

    fun decodeZeroWidth(text: String) {
        if (text.isBlank()) {
            statusMessage = getApp().getString(R.string.error_empty_message)
            return
        }

        val decoded = ZeroWidthEngine.decode(text)
        if (decoded != null) {
            zwDecodedText = decoded
            statusMessage = getApp().getString(R.string.zw_decode_success)
        } else {
            zwDecodedText = null
            statusMessage = getApp().getString(R.string.error_zw_decode_failed)
        }
    }

    // ── Hybridization ──

    fun onCoverFilePicked(uri: Uri?) {
        uri ?: return
        viewModelScope.launch {
            val bytes = withContext(Dispatchers.IO) {
                HybridEngine.readBytes(getApp().contentResolver, uri)
            }
            if (bytes != null) {
                coverBytes = bytes
                val fmt = HybridEngine.detectFormat(bytes)
                coverFormat = fmt.name
            } else {
                statusMessage = getApp().getString(R.string.error_read_failed)
            }
        }
    }

    fun onSecretFilePicked(uri: Uri?) {
        uri ?: return
        viewModelScope.launch {
            val bytes = withContext(Dispatchers.IO) {
                HybridEngine.readBytes(getApp().contentResolver, uri)
            }
            if (bytes != null) {
                secretFileBytes = bytes
            } else {
                statusMessage = getApp().getString(R.string.error_read_failed)
            }
        }
    }

    fun hybridize(useChunkMode: Boolean) {
        val cover = coverBytes ?: run {
            statusMessage = getApp().getString(R.string.error_no_cover)
            return
        }
        val secret = secretFileBytes ?: run {
            statusMessage = getApp().getString(R.string.error_no_secret_file)
            return
        }
        val format = HybridEngine.detectFormat(cover)
        if (format == HybridEngine.CoverFormat.UNKNOWN) {
            statusMessage = getApp().getString(R.string.error_unsupported_format)
            return
        }
        if (useChunkMode && format != HybridEngine.CoverFormat.PNG) {
            statusMessage = getApp().getString(R.string.error_not_png)
            return
        }

        isProcessing = true
        statusMessage = getApp().getString(R.string.processing)

        viewModelScope.launch {
            try {
                val mode = if (useChunkMode) HybridEngine.HybridMode.PNG_CHUNK
                           else HybridEngine.HybridMode.CONCAT
                val result = withContext(Dispatchers.Default) {
                    HybridEngine.hybridize(cover, secret, mode)
                }
                hybridResult = result
                statusMessage = getApp().getString(R.string.hybrid_success)
            } catch (e: Exception) {
                statusMessage = e.message ?: "Hybridization error"
            } finally {
                isProcessing = false
            }
        }
    }

    fun extractHybrid(useChunkMode: Boolean) {
        val cover = coverBytes ?: run {
            statusMessage = getApp().getString(R.string.error_no_cover)
            return
        }

        isProcessing = true
        statusMessage = getApp().getString(R.string.processing)

        viewModelScope.launch {
            try {
                val mode = if (useChunkMode) HybridEngine.HybridMode.PNG_CHUNK
                           else HybridEngine.HybridMode.CONCAT
                val result = withContext(Dispatchers.Default) {
                    HybridEngine.extract(cover, mode)
                }
                if (result != null && result.isNotEmpty()) {
                    hybridResult = result
                    statusMessage = getApp().getString(R.string.extract_file_success)
                } else {
                    statusMessage = getApp().getString(R.string.error_extract_file_failed)
                }
            } catch (e: Exception) {
                statusMessage = e.message ?: "Extraction error"
            } finally {
                isProcessing = false
            }
        }
    }

    fun saveHybridToDownloads(extension: String) {
        val data = hybridResult ?: return

        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) {
                saveToDownloads(data, extension)
            }
            statusMessage = if (success) {
                getApp().getString(R.string.saved_to_downloads)
            } else {
                getApp().getString(R.string.error_save_failed)
            }
        }
    }

    // ── Media Converter ──

    var inputMediaUri by mutableStateOf<Uri?>(null)
        private set

    fun onMediaFilePicked(uri: Uri?, isImage: Boolean = false) {
        uri ?: return
        inputMediaUri = uri
        conversionDone = false

        viewModelScope.launch {
            val context = getApp()
            val info = withContext(Dispatchers.IO) {
                if (isImage) {
                    MediaConverterEngine.getImageInfo(context, uri)
                } else {
                    MediaConverterEngine.getMediaInfo(context, uri)
                }
            }
            mediaInfo = info
        }
    }

    fun convertMedia(
        outputExtension: String,
        videoMime: String = "video/avc",
        audioMime: String = "audio/mp4a-latm",
        width: Int = 0,
        height: Int = 0,
        videoBitrateKbps: Int = 0,
        imageWidth: Int? = null,
        imageHeight: Int? = null,
        imageQuality: Int = 85,
        isImage: Boolean = false,
        extractAudioOnly: Boolean = false,
        removeAudio: Boolean = false
    ) {
        val uri = inputMediaUri ?: run {
            statusMessage = "No file selected"
            return
        }

        isProcessing = true
        statusMessage = getApp().getString(R.string.processing)

        viewModelScope.launch {
            try {
                val context = getApp()
                val outputFile = java.io.File.createTempFile(
                    "coddy_out_", ".$outputExtension", context.cacheDir
                )

                val success = when {
                    extractAudioOnly -> withContext(Dispatchers.IO) {
                        MediaConverterEngine.extractAudio(
                            context, uri, outputFile.absolutePath
                        )
                    }
                    isImage -> withContext(Dispatchers.IO) {
                        MediaConverterEngine.convertImage(
                            context, uri, outputFile.absolutePath,
                            width = imageWidth, height = imageHeight,
                            quality = imageQuality
                        )
                    }
                    else -> MediaConverterEngine.transcodeVideo(
                        context, uri, outputFile.absolutePath,
                        videoMime = videoMime, audioMime = audioMime,
                        width = width, height = height,
                        videoBitrateKbps = videoBitrateKbps,
                        removeAudio = removeAudio
                    )
                }

                if (success) {
                    val saved = withContext(Dispatchers.IO) {
                        saveToDownloads(outputFile.readBytes(), outputExtension)
                    }
                    conversionDone = true
                    statusMessage = if (saved) {
                        getApp().getString(R.string.saved_to_downloads)
                    } else {
                        "Converted but failed to save"
                    }
                    outputFile.delete()
                } else {
                    statusMessage = "Conversion failed"
                }
            } catch (e: Exception) {
                statusMessage = e.message ?: "Conversion error"
            } finally {
                isProcessing = false
            }
        }
    }

    // ── Crypto ──

    fun encryptCrypto(text: String, pass: String) {
        if (pass.isBlank()) {
            statusMessage = getApp().getString(R.string.error_empty_password)
            return
        }
        if (text.isBlank()) {
            statusMessage = getApp().getString(R.string.error_empty_crypto_text)
            return
        }
        isProcessing = true
        statusMessage = getApp().getString(R.string.processing)
        viewModelScope.launch {
            try {
                cryptoResultText = withContext(Dispatchers.Default) {
                    CryptoEngine.encrypt(text, pass)
                }
                statusMessage = getApp().getString(R.string.crypto_encrypt_success)
            } catch (e: Exception) {
                statusMessage = e.message ?: "Encryption error"
            } finally {
                isProcessing = false
            }
        }
    }

    fun decryptCrypto(text: String, pass: String) {
        if (pass.isBlank()) {
            statusMessage = getApp().getString(R.string.error_empty_password)
            return
        }
        if (text.isBlank()) {
            statusMessage = getApp().getString(R.string.error_empty_crypto_text)
            return
        }
        isProcessing = true
        statusMessage = getApp().getString(R.string.processing)
        viewModelScope.launch {
            try {
                cryptoResultText = withContext(Dispatchers.Default) {
                    CryptoEngine.decrypt(text, pass)
                }
                statusMessage = getApp().getString(R.string.crypto_decrypt_success)
            } catch (e: Exception) {
                statusMessage = e.message ?: "Decryption error"
            } finally {
                isProcessing = false
            }
        }
    }

    // ── Helpers ──

    fun showStatusMessage(message: String) {
        statusMessage = message
    }

    private fun getApp(): Application = getApplication()

    private fun loadBitmap(uri: Uri): Bitmap? {
        return try {
            val source = ImageDecoder.createSource(getApp().contentResolver, uri)
            val bmp = ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = false
                // Force sRGB to prevent color space conversion altering LSBs
                decoder.setTargetColorSpace(
                    android.graphics.ColorSpace.get(android.graphics.ColorSpace.Named.SRGB)
                )
            }
            // Ensure ARGB_8888 for consistent pixel access
            if (bmp.config != Bitmap.Config.ARGB_8888) {
                bmp.copy(Bitmap.Config.ARGB_8888, false)
            } else {
                bmp
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun saveBitmapToMediaStore(bitmap: Bitmap): Boolean {
        return try {
            val resolver = getApp().contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "coddy_${System.currentTimeMillis()}.png")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Coddy")
            }

            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: return false

            resolver.openOutputStream(uri)?.use { os ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun saveToDownloads(data: ByteArray, extension: String): Boolean {
        return try {
            val resolver = getApp().contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "coddy_${System.currentTimeMillis()}.$extension")
                put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Coddy")
            }

            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                ?: return false

            resolver.openOutputStream(uri)?.use { os ->
                os.write(data)
            }
            true
        } catch (_: Exception) {
            false
        }
    }
}
