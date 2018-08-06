package com.example.ozturkse.x.util

import android.content.Context
import android.graphics.*
import android.net.ConnectivityManager
import android.support.media.ExifInterface
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


object Util {

    fun compressImage(bitmap: Bitmap): Bitmap {
        val aspectRatio = bitmap.width / bitmap.height.toFloat()
        val width = 480
        val height = Math.round(width / aspectRatio)
        val resized = Bitmap.createScaledBitmap(bitmap, width, height, true)
        return resized
    }

    fun bitmapToFile(resized: Bitmap, filesDir: File): File {
        val imageFile = File(filesDir, "rectangleface.jpg")
        imageFile.createNewFile()

        val bos = ByteArrayOutputStream()
        resized.compress(Bitmap.CompressFormat.JPEG, 70 /*ignored for PNG*/, bos)
        val bitmapdata = bos.toByteArray()

        val fos = FileOutputStream(imageFile)
        fos.write(bitmapdata)
        fos.flush()
        fos.close()

        return imageFile
    }


    fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }


    fun getCameraPhotoOrientation(imageFilePath: String): Int {
        var rotate = 0
        try {

            val exif: ExifInterface

            exif = ExifInterface(imageFilePath)
            val exifOrientation = exif
                    .getAttribute(ExifInterface.TAG_ORIENTATION)
            Log.d("exifOrientation", exifOrientation)
            val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL)

            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_270 -> rotate = 270
                ExifInterface.ORIENTATION_ROTATE_180 -> rotate = 180
                ExifInterface.ORIENTATION_ROTATE_90 -> rotate = 90
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return rotate
    }

    fun isInternetAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null
    }

    fun toGrayscale(bmpOriginal: Bitmap): Bitmap {
        val width: Int
        val height: Int
        height = bmpOriginal.height
        width = bmpOriginal.width

        val bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val c = Canvas(bmpGrayscale)
        val paint = Paint()
        val cm = ColorMatrix()
        cm.setSaturation(0f)
        val f = ColorMatrixColorFilter(cm)
        paint.colorFilter = f
        c.drawBitmap(bmpOriginal, 0f, 0f, paint)
        return bmpGrayscale
    }

}