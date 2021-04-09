package com.multitv.learn_workmanager

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.work.*
import com.multitv.learn_workmanager.workers.BlurWorker
import com.multitv.learn_workmanager.workers.CleanupWorker
import com.multitv.learn_workmanager.workers.SaveImageToFileWorker
import timber.log.Timber


class BlurViewModel(application: Application) : AndroidViewModel(application) {

    internal var imageUri: Uri? = null
    internal var outputUri: Uri? = null
    private var workManager = WorkManager.getInstance(application)
    internal val outPutWorkInfos: LiveData<List<WorkInfo>>

    init {
        outPutWorkInfos = workManager.getWorkInfosByTagLiveData(TAG_OUTPUT)
        Timber.e("BlurViewModel init: $outPutWorkInfos")
    }

    /**
     * Create the WorkRequest to apply the blur and save the resulting image
     * @param blurLevel The amount to blur the image
     */
    internal fun applyBlur(blurLevel: Int) {

//        var continuation = workManager.beginWith(OneTimeWorkRequest.from(CleanupWorker::class.java))

        var continuation = workManager.beginUniqueWork(
            IMAGE_MANIPULATION_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequest.Builder(CleanupWorker::class.java).build()
        )

        for (i in 0 until blurLevel) {
            val blurBuilder = OneTimeWorkRequest.Builder(BlurWorker::class.java)
            if (i == 0)
                blurBuilder.setInputData(createInputDataForUri())
            continuation = continuation.then(blurBuilder.build())
        }

//        val blurRequest = OneTimeWorkRequest.Builder(BlurWorker::class.java)
//            .setInputData(createInputDataForUri()).build()
//        continuation = continuation.then(blurRequest)

        val save = OneTimeWorkRequest.Builder(SaveImageToFileWorker::class.java)
            .addTag(TAG_OUTPUT)
            .build()
        continuation = continuation.then(save)

        continuation.enqueue()
    }

    private fun createInputDataForUri(): Data {
        val builder = Data.Builder()
        imageUri?.let {
            builder.putString(KEY_IMAGE_URI, it.toString())
        }
        return builder.build()
    }

    private fun uriOrNull(uriString: String?): Uri? {
        return if (!uriString.isNullOrEmpty()) {
            Uri.parse(uriString)
        } else {
            null
        }
    }

    /**
     * Setters
     */
    internal fun setImageUri(uri: String?) {
        imageUri = uriOrNull(uri)
    }

    internal fun setOutputUri(outputImageUri: String?) {
        outputUri = uriOrNull(outputImageUri)
    }

    internal fun cancelWork(){
        workManager.cancelUniqueWork(IMAGE_MANIPULATION_WORK_NAME)
    }
}
