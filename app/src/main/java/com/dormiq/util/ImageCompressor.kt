package com.dormiq.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.math.max

object ImageCompressor {
    fun compressUri(context: Context, uri: Uri, maxDimension: Int = 1280, quality: Int = 75): ByteArray? {
        val contentResolver = context.contentResolver
        var inputStream: InputStream? = null
        return try {
            // 1. Decode bounds to avoid Out-Of-Memory exceptions
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            inputStream = contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            // 2. Calculate optimal inSampleSize (scale down factor)
            val currentWidth = options.outWidth
            val currentHeight = options.outHeight
            var inSampleSize = 1
            if (currentWidth > maxDimension || currentHeight > maxDimension) {
                val halfWidth = currentWidth / 2
                val halfHeight = currentHeight / 2
                while ((halfWidth / inSampleSize) >= maxDimension && (halfHeight / inSampleSize) >= maxDimension) {
                    inSampleSize *= 2
                }
            }

            // 3. Decode scaled Bitmap
            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }
            inputStream = contentResolver.openInputStream(uri)
            val scaledBitmap = BitmapFactory.decodeStream(inputStream, null, decodeOptions) ?: return null
            inputStream?.close()

            // 4. Precise resize if still larger than maxDimension
            val longestEdge = max(scaledBitmap.width, scaledBitmap.height)
            val finalBitmap = if (longestEdge > maxDimension) {
                val scale = maxDimension.toFloat() / longestEdge
                Bitmap.createScaledBitmap(
                    scaledBitmap,
                    (scaledBitmap.width * scale).toInt(),
                    (scaledBitmap.height * scale).toInt(),
                    true
                )
            } else {
                scaledBitmap
            }

            // 5. Compress to highly optimized JPEG byte array
            val outputStream = ByteArrayOutputStream()
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            val result = outputStream.toByteArray()
            
            // Cleanup references
            if (finalBitmap != scaledBitmap) {
                finalBitmap.recycle()
            }
            scaledBitmap.recycle()
            
            result
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            inputStream?.close()
        }
    }
}
