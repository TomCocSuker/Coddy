package com.example.coddy

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaCodecList
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Media Converter Engine — uses AndroidX Media3 Transformer for video
 * and Android BitmapFactory for images. No external FFmpeg needed.
 */
object MediaConverterEngine {

    data class MediaInfo(
        val duration: String,
        val resolution: String,
        val codec: String,
        val bitrate: String,
        val format: String,
        val fileSize: Long
    )

    /**
     * Get media information using MediaMetadataRetriever.
     */
    fun getMediaInfo(context: Context, uri: Uri): MediaInfo? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)

            val durationMs = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_DURATION
            )?.toLongOrNull() ?: 0L

            val width = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH
            ) ?: "?"

            val height = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT
            ) ?: "?"

            val bitrate = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_BITRATE
            ) ?: "unknown"

            val mime = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_MIMETYPE
            ) ?: "unknown"

            retriever.release()

            val fileSize = try {
                context.contentResolver.openFileDescriptor(uri, "r")?.use {
                    it.statSize
                } ?: 0L
            } catch (_: Exception) { 0L }

            MediaInfo(
                duration = "%.1f".format(durationMs / 1000.0),
                resolution = "${width}x${height}",
                codec = mime,
                bitrate = bitrate,
                format = mime.substringAfter("/"),
                fileSize = fileSize
            )
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Get image information.
     */
    fun getImageInfo(context: Context, uri: Uri): MediaInfo? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }

            val mime = context.contentResolver.getType(uri) ?: "unknown"
            val fileSize = try {
                context.contentResolver.openFileDescriptor(uri, "r")?.use {
                    it.statSize
                } ?: 0L
            } catch (_: Exception) { 0L }

            MediaInfo(
                duration = "N/A",
                resolution = "${options.outWidth}x${options.outHeight}",
                codec = mime,
                bitrate = "N/A",
                format = mime.substringAfter("/"),
                fileSize = fileSize
            )
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Transcode video using Media3 Transformer.
     *
     * @param context Application context
     * @param inputUri Input video URI
     * @param outputPath Output file path
     * @param videoMime Target video MIME (e.g., MimeTypes.VIDEO_H264)
     * @param audioMime Target audio MIME (e.g., MimeTypes.AUDIO_AAC)
     * @param width Target width (0 = original)
     * @param height Target height (0 = original)
     * @param videoBitrateKbps Target video bitrate in kbps (0 = default)
     * @param removeAudio Strip audio track
     */
    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    suspend fun transcodeVideo(
        context: Context,
        inputUri: Uri,
        outputPath: String,
        videoMime: String = MimeTypes.VIDEO_H264,
        audioMime: String = MimeTypes.AUDIO_AAC,
        width: Int = 0,
        height: Int = 0,
        videoBitrateKbps: Int = 0,
        removeAudio: Boolean = false
    ): Boolean = suspendCancellableCoroutine { cont ->
        try {
            val transformerBuilder = Transformer.Builder(context)
                .setVideoMimeType(videoMime)

            if (!removeAudio) {
                transformerBuilder.setAudioMimeType(audioMime)
            }

            val transformer = transformerBuilder.build()

            val mediaItem = MediaItem.fromUri(inputUri)
            val editedMediaItem = EditedMediaItem.Builder(mediaItem)
                .setRemoveAudio(removeAudio)
                .build()

            transformer.addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    if (cont.isActive) cont.resume(true)
                }

                override fun onError(
                    composition: Composition,
                    exportResult: ExportResult,
                    exportException: androidx.media3.transformer.ExportException
                ) {
                    if (cont.isActive) cont.resumeWithException(exportException)
                }
            })

            transformer.start(editedMediaItem, outputPath)

            cont.invokeOnCancellation {
                transformer.cancel()
            }
        } catch (e: Exception) {
            if (cont.isActive) cont.resumeWithException(e)
        }
    }

    /**
     * Extract audio from video using MediaExtractor + MediaMuxer.
     */
    fun extractAudio(
        context: Context,
        inputUri: Uri,
        outputPath: String
    ): Boolean {
        return try {
            val extractor = MediaExtractor()
            extractor.setDataSource(context, inputUri, null)

            // Find audio track
            var audioTrackIndex = -1
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: continue
                if (mime.startsWith("audio/")) {
                    audioTrackIndex = i
                    break
                }
            }

            if (audioTrackIndex < 0) return false

            extractor.selectTrack(audioTrackIndex)
            val audioFormat = extractor.getTrackFormat(audioTrackIndex)

            // Determine output format
            val outputFormat = when {
                outputPath.endsWith(".mp4", true) || outputPath.endsWith(".m4a", true) ->
                    MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
                outputPath.endsWith(".webm", true) ->
                    MediaMuxer.OutputFormat.MUXER_OUTPUT_WEBM
                else -> MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
            }

            val muxer = MediaMuxer(outputPath, outputFormat)
            val muxerTrackIndex = muxer.addTrack(audioFormat)
            muxer.start()

            val buffer = java.nio.ByteBuffer.allocate(1024 * 1024) // 1MB buffer
            val bufferInfo = android.media.MediaCodec.BufferInfo()

            while (true) {
                val sampleSize = extractor.readSampleData(buffer, 0)
                if (sampleSize < 0) break

                bufferInfo.offset = 0
                bufferInfo.size = sampleSize
                bufferInfo.presentationTimeUs = extractor.sampleTime
                bufferInfo.flags = extractor.sampleFlags

                muxer.writeSampleData(muxerTrackIndex, buffer, bufferInfo)
                extractor.advance()
            }

            muxer.stop()
            muxer.release()
            extractor.release()
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Convert an image (resize, change format, adjust quality).
     */
    fun convertImage(
        context: Context,
        inputUri: Uri,
        outputPath: String,
        width: Int? = null,
        height: Int? = null,
        quality: Int = 85
    ): Boolean {
        return try {
            val originalBitmap = context.contentResolver.openInputStream(inputUri)?.use {
                BitmapFactory.decodeStream(it)
            } ?: return false

            // Resize if needed
            val bitmap = if (width != null || height != null) {
                val targetW = width ?: (originalBitmap.width * (height!! / originalBitmap.height.toFloat())).toInt()
                val targetH = height ?: (originalBitmap.height * (width!! / originalBitmap.width.toFloat())).toInt()
                Bitmap.createScaledBitmap(originalBitmap, targetW, targetH, true)
            } else {
                originalBitmap
            }

            // Determine output format
            val format = when {
                outputPath.endsWith(".png", true) -> Bitmap.CompressFormat.PNG
                outputPath.endsWith(".webp", true) -> Bitmap.CompressFormat.WEBP_LOSSY
                else -> Bitmap.CompressFormat.JPEG
            }

            FileOutputStream(outputPath).use { out ->
                bitmap.compress(format, quality, out)
            }

            if (bitmap != originalBitmap) bitmap.recycle()
            originalBitmap.recycle()
            true
        } catch (_: Exception) {
            false
        }
    }
}
