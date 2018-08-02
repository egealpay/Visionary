package com.example.ozturkse.x.util

import android.graphics.Bitmap
import android.graphics.Matrix
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

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
        resized.compress(Bitmap.CompressFormat.JPEG, 50 /*ignored for PNG*/, bos)
        val bitmapdata = bos.toByteArray()

        val fos = FileOutputStream(imageFile)
        fos.write(bitmapdata)
        fos.flush()
        fos.close()

        return imageFile
    }

    fun rotateImage(bitmap: Bitmap): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(90f)

        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width, bitmap.height, true)
        val rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.width, scaledBitmap.height, matrix, true)

        return rotatedBitmap
    }

}