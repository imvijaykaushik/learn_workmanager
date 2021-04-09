package com.multitv.learn_workmanager.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.multitv.learn_workmanager.KEY_IMAGE_URI
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class SaveImageToFileWorker(ctx: Context, param: WorkerParameters) : Worker(ctx, param) {
    private val title = "blurred image"
    private val dateFormatter = SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z", Locale.getDefault())

    override fun doWork(): Result {
        makeStatusNotification("Saving image", applicationContext)
        sleep()
        val resolver = applicationContext.contentResolver
        return try {
            val resourceUri = inputData.getString(KEY_IMAGE_URI)
            val bitmap =
                BitmapFactory.decodeStream(resolver.openInputStream(Uri.parse(resourceUri)))
            val imageUrl = MediaStore.Images.Media.insertImage(
                resolver,
                bitmap,
                title,
                dateFormatter.format(Date())
            )
            if (!imageUrl.isNullOrEmpty()) {
                val output = workDataOf(KEY_IMAGE_URI to imageUrl)
                return Result.success(output)
            } else {
                Timber.e("Writing to MediaStore failed!!")
                Result.failure()
            }
        } catch (exp: Exception) {
            Timber.e(exp)
            Result.failure()
        }
    }
}