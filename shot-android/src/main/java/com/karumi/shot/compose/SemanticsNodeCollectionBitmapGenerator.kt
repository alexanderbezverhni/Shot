package com.karumi.shot.compose

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage

class SemanticsNodeCollectionBitmapGenerator {

    @RequiresApi(Build.VERSION_CODES.O)
    fun generateBitmap(screenshot: ScreenshotSource.NodesCollection): android.graphics.Bitmap {
        val bitmap1 = screenshot.nodes[0].captureToImage().asAndroidBitmap()
        val bitmap2 = screenshot.nodes[1].captureToImage().asAndroidBitmap()
        return merge(bitmap1, bitmap2)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun generateBitmap(screenshot: ScreenshotSource.Nodes): android.graphics.Bitmap {
        val bitmap1 = screenshot.nodes[0].captureToImage().asAndroidBitmap()
        val bitmap2 = screenshot.nodes[1].captureToImage().asAndroidBitmap()
        return merge(bitmap1, bitmap2)
    }

    private fun merge(bitmap1: android.graphics.Bitmap, bitmap2: android.graphics.Bitmap): android.graphics.Bitmap {
        val output = android.graphics.Bitmap.createBitmap(bitmap1.width, bitmap1.height, android.graphics.Bitmap.Config.ARGB_8888)

        val comboImage = Canvas(output)

        comboImage.drawBitmap(bitmap1, 0f, 0f, null)
        comboImage.drawBitmap(bitmap2, 0f, 0f, null)

        return output
    }
}
