package com.unimal.uas_240170129_muhammad_raihann_azhari.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.max

/** Menyalin foto (kamera/galeri) ke penyimpanan internal aplikasi dalam ukuran yang diperkecil. */
object ImageUtils {

    private const val MAX_SIZE = 1280
    private const val QUALITY = 85

    fun newPhotoFile(context: Context, prefix: String = "bekal"): File {
        val dir = File(context.filesDir, "photos").apply { mkdirs() }
        return File(dir, "${prefix}_${System.currentTimeMillis()}.jpg")
    }

    /** Foto dari galeri (Photo Picker). */
    fun importFromUri(context: Context, uri: Uri): File? {
        val dest = newPhotoFile(context)
        return compress({ context.contentResolver.openInputStream(uri) }, dest)
    }

    /** Foto dari kamera: [source] adalah file sementara dan akan dihapus setelah diproses. */
    fun importFromFile(context: Context, source: File): File? {
        val dest = newPhotoFile(context)
        val result = compress({ FileInputStream(source) }, dest)
        source.delete()
        return result
    }

    private fun compress(open: () -> InputStream?, dest: File): File? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        open()?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_SIZE) sample *= 2

        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        var bitmap = open()?.use { BitmapFactory.decodeStream(it, null, options) } ?: return null

        val rotation = try {
            open()?.use { readRotation(it) } ?: 0
        } catch (e: Exception) {
            0
        }

        val matrix = Matrix()
        val scale = MAX_SIZE.toFloat() / max(bitmap.width, bitmap.height)
        if (scale < 1f) matrix.postScale(scale, scale)
        if (rotation != 0) matrix.postRotate(rotation.toFloat())
        if (!matrix.isIdentity) {
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }

        FileOutputStream(dest).use { bitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, it) }
        return dest
    }

    private fun readRotation(input: InputStream): Int {
        val exif = ExifInterface(input)
        return when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }
}
