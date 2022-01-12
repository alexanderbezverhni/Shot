package com.karumi.shot.compose

import android.graphics.Bitmap
import android.os.Build
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsNodeInteractionCollection
import com.google.gson.Gson
import com.karumi.shot.AndroidStorageInfo
import java.io.File
import java.io.FileOutputStream
import java.lang.IllegalArgumentException

class ScreenshotSaver(
        private val packageName: String,
        private val singleNodeBitmapGenerator: SemanticsNodeBitmapGenerator,
        private val multipleNodesBitmapGenerator: SemanticsNodeCollectionBitmapGenerator
        ) {

    private val screenshotsFolder: String = "${AndroidStorageInfo.storageBaseUrl}/screenshots/$packageName/screenshots-compose-default/"
    private val metadataFile: String = "$screenshotsFolder/metadata.json"
    private val gson: Gson = Gson()

    fun saveScreenshot(screenshot: ScreenshotToSave) {
        val bitmap = getBitmapFromScreenshotToSave(screenshot)
        createScreenshotsFolderIfDoesNotExist()
        saveScreenshotBitmap(bitmap, screenshot.data)
    }

    private fun getBitmapFromScreenshotToSave(screenshot: ScreenshotToSave): android.graphics.Bitmap = when (screenshot.source) {
        is ScreenshotSource.Bitmap -> screenshot.source.bitmap
        is ScreenshotSource.Nodes ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                multipleNodesBitmapGenerator.generateBitmap(screenshot.source)
            } else {
                throw IllegalArgumentException("Can't extract bitmap from nodes in a SDK version lower than Build.VERSION_CODES.O")
            }
        is ScreenshotSource.NodesCollection ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                multipleNodesBitmapGenerator.generateBitmap(screenshot.source)
            } else {
                throw IllegalArgumentException("Can't extract bitmap from nodes in a SDK version lower than Build.VERSION_CODES.O")
            }
        is ScreenshotSource.Node ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                singleNodeBitmapGenerator.generateBitmap(screenshot.source)
            } else {
                throw IllegalArgumentException("Can't extract bitmap from node in a SDK version lower than Build.VERSION_CODES.O")
            }
    }

    fun saveMetadata(session: ScreenshotTestSession) {
        createScreenshotsFolderIfDoesNotExist()
        val metadata = session.getScreenshotSessionMetadata()
        val serializedMetadata = gson.toJson(metadata)
        saveSerializedMetadata(serializedMetadata)
    }

    private fun createScreenshotsFolderIfDoesNotExist() {
        val folder = File(screenshotsFolder)
        if (!folder.exists()) {
            folder.mkdirs()
        }
    }

    private fun saveSerializedMetadata(serializedMetadata: String) {
        deleteFileIfExists(metadataFile)
        val file = createFileIfNotExists(metadataFile)
        val printWriter = file.printWriter()
        printWriter.print(serializedMetadata)
        printWriter.close()
    }

    private fun saveScreenshotBitmap(bitmap: android.graphics.Bitmap, data: ScreenshotMetadata) {
        val screenshotPath = getScreenshotSdCardPath(data)
        deleteFileIfExists(screenshotPath)
        createFileIfNotExists(screenshotPath)
        val fileOutputStream = FileOutputStream(screenshotPath)
        fileOutputStream.use { out ->
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
        }
        fileOutputStream.close()
    }

    private fun deleteFileIfExists(path: String) {
        val file = File(path)
        if (file.exists()) {
            file.delete()
        }
    }

    private fun createFileIfNotExists(path: String): File {
        val file = File(path)
        if (!file.exists()) {
            file.createNewFile()
        }
        return file
    }

    private fun getScreenshotSdCardPath(data: ScreenshotMetadata): String = "$screenshotsFolder${data.name}.png"
}

data class ScreenshotToSave(val source: ScreenshotSource, val data: ScreenshotMetadata)

sealed class ScreenshotSource {
    data class Node(val node: SemanticsNodeInteraction) : ScreenshotSource()
    data class NodesCollection(val nodes: SemanticsNodeInteractionCollection) : ScreenshotSource()
    data class Nodes(val nodes: List<SemanticsNodeInteraction>) : ScreenshotSource()
    data class Bitmap(val bitmap: android.graphics.Bitmap) : ScreenshotSource()
}
