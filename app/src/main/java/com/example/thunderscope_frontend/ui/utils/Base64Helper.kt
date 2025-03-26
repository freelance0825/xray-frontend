package com.example.thunderscope_frontend.ui.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64

object Base64Helper {
    fun convertToBitmap(encodedImage: String?): Bitmap {
        val decodedString = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    }
}
