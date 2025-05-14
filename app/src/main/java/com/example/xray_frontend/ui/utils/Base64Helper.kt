package com.example.xray_frontend.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream

object Base64Helper {
    fun convertToBitmap(encodedImage: String?): Bitmap {
        val decodedString = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    }

    fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    fun drawableToBase64(context: Context, drawableId: Int): String {
        val bitmap = BitmapFactory.decodeResource(context.resources, drawableId)
        return bitmapToBase64(bitmap)
    }
}
