package com.multitv.learn_workmanager.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.multitv.learn_workmanager.OUTPUT_PATH
import timber.log.Timber
import java.io.File

class CleanupWorker(ctx: Context, param: WorkerParameters) : Worker(ctx, param) {

    override fun doWork(): Result {
        makeStatusNotification("Clean old temp files", applicationContext)
        sleep()
        return try {
            val outputDirectory = File(applicationContext.filesDir, OUTPUT_PATH)
            if (outputDirectory.exists()) {
                val entries = outputDirectory.listFiles()
                if (entries != null) {
                    for (entry in entries) {
                        val name = entry.name
                        if (name.isNotEmpty() && name.endsWith(".png")) {
                            val deleted = entry.delete()
                            Timber.i("Delete file - $name -- $deleted")
                        }
                    }
                }
            }
            Result.success()

        } catch (e: Exception) {
            Timber.e(e)
            return Result.success()
        }
    }
}