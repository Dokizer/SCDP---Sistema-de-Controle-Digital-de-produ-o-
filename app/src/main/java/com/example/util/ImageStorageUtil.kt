package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object ImageStorageUtil {

    private const val PHOTO_DIR = "dating_photos"

    fun getPhotosDirectory(context: Context): File {
        val dir = File(context.filesDir, PHOTO_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Creates a new permanent file in internal storage and returns a content URI for camera output.
     * Also returns the file so we know where it will be saved.
     */
    fun createPermanentCameraOutput(context: Context): Pair<File, Uri> {
        val dir = getPhotosDirectory(context)
        val file = File(dir, "dating_pkg_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return Pair(file, uri)
    }

    /**
     * Saves an image from any Uri (e.g. Gallery / PhotoPicker) into our permanent app storage.
     * Returns the permanent file absolute path.
     */
    fun saveUriToInternalStorage(context: Context, sourceUri: Uri): String? {
        return try {
            val dir = getPhotosDirectory(context)
            val destFile = File(dir, "dating_pkg_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg")
            
            val inputStream: InputStream? = context.contentResolver.openInputStream(sourceUri)
            val outputStream = FileOutputStream(destFile)
            
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            
            optimizeSavedImage(destFile)
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Optimizes an image file (checks EXIF orientation and resizes if larger than 1200px).
     */
    fun optimizeSavedImage(file: File) {
        try {
            if (!file.exists() || file.length() == 0L) return

            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, options)

            var sampleSize = 1
            val maxDimension = 1200
            while (options.outWidth / sampleSize > maxDimension || options.outHeight / sampleSize > maxDimension) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
            val bitmap = BitmapFactory.decodeFile(file.absolutePath, decodeOptions) ?: return

            // Handle EXIF Rotation
            val exif = try { ExifInterface(file.absolutePath) } catch (_: Exception) { null }
            val orientation = exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                ?: ExifInterface.ORIENTATION_NORMAL

            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            }

            val rotatedBitmap = if (!matrix.isIdentity) {
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } else {
                bitmap
            }

            FileOutputStream(file).use { out ->
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                out.flush()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Loads a Bitmap from either an absolute file path or a content:// URI string.
     */
    fun loadBitmap(context: Context, pathOrUriStr: String?): Bitmap? {
        if (pathOrUriStr.isNullOrBlank()) return null
        return try {
            if (pathOrUriStr.startsWith("/") || pathOrUriStr.startsWith("file://")) {
                val cleanPath = pathOrUriStr.removePrefix("file://")
                val file = File(cleanPath)
                if (file.exists()) {
                    BitmapFactory.decodeFile(file.absolutePath)
                } else null
            } else {
                val uri = Uri.parse(pathOrUriStr)
                // First try file direct if it's a file provider uri in our app files
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
