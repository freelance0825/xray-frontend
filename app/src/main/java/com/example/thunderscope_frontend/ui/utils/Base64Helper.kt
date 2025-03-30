package com.example.thunderscope_frontend.ui.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log

object Base64Helper {
    fun convertToBitmap(encodedImage: String?): Bitmap {
        Log.e("FTEST", "  -> cvB 1: ${encodedImage}", )

        val decodedString = Base64.decode(encodedImage, Base64.DEFAULT)

        Log.e("FTEST", "  -> cvB 2: ${decodedString}", )

        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    }
}
