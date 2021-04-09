package com.multitv.learn_workmanager.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.TextUtils
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.multitv.learn_workmanager.KEY_IMAGE_URI
import timber.log.Timber

class BlurWorker(ctx: Context, param: WorkerParameters) : Worker(ctx, param) {
    override fun doWork(): Result {
        val appContext = applicationContext
        val resourceUri = inputData.getString(KEY_IMAGE_URI)
        makeStatusNotification("Blurring image", appContext)
        sleep()
        return try {
//            val picture = BitmapFactory.decodeResource(appContext.resources, R.drawable.test)
            if (TextUtils.isEmpty(resourceUri)) {
                Timber.e("Invalid input uri")
                throw IllegalArgumentException("Invalid input uri")
            }
            val resolver = appContext.contentResolver
            val picture =
                BitmapFactory.decodeStream(resolver.openInputStream(Uri.parse(resourceUri)))
            val output = blurBitmap(picture, appContext)
            val outputUri = writeBitmapToFile(appContext, output)
            val outputData = workDataOf(KEY_IMAGE_URI to outputUri.toString())
            Result.success(outputData)
        } catch (throwable: Throwable) {
            Timber.e(throwable, "Error appling blur")
            Result.failure()
        }
    }
}