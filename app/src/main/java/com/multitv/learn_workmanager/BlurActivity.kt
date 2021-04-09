package com.multitv.learn_workmanager

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.work.WorkInfo
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_blur.*
import timber.log.Timber

class BlurActivity : AppCompatActivity() {

    private lateinit var viewModel: BlurViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blur)

        // Get the ViewModel
        viewModel = ViewModelProviders.of(this).get(BlurViewModel::class.java)

        // Image uri should be stored in the ViewModel; put it there then display
        val imageUriExtra = intent.getStringExtra(KEY_IMAGE_URI)
        viewModel.setImageUri(imageUriExtra)
        viewModel.imageUri?.let { imageUri ->
            Glide.with(this).load(imageUri).into(image_view)
        }

        go_button.setOnClickListener { viewModel.applyBlur(blurLevel) }
        viewModel.outPutWorkInfos.observe(this, workInfosObserver())

        see_file_button.setOnClickListener {
            viewModel.outputUri?.let { currentUri ->
                val actionView = Intent(Intent.ACTION_VIEW, currentUri)
                actionView.resolveActivity(packageManager)?.run {
                    startActivity(actionView)
                }
            }
        }

        cancel_button.setOnClickListener { viewModel.cancelWork() }
    }

    private fun workInfosObserver(): Observer<List<WorkInfo>> {
        return Observer { listOfWorkInfo ->
            Timber.e(listOfWorkInfo.toString())
            if (listOfWorkInfo.isNullOrEmpty())
                return@Observer

            val workInfo = listOfWorkInfo[0]
            if (workInfo.state.isFinished) {
                showWorkFinished()
                val outputImageUri = workInfo.outputData.getString(KEY_IMAGE_URI)
                if (!outputImageUri.isNullOrEmpty()) {
                    viewModel.setOutputUri(outputImageUri)
                    see_file_button.visibility = View.VISIBLE
                }
            } else {
                showWorkInProgress()
            }

        }
    }

    /**
     * Shows and hides views for when the Activity is processing an image
     */
    private fun showWorkInProgress() {
        run {
            progress_bar.visibility = View.VISIBLE
            cancel_button.visibility = View.VISIBLE
            go_button.visibility = View.GONE
            see_file_button.visibility = View.GONE
        }
    }

    /**
     * Shows and hides views for when the Activity is done processing an image
     */
    private fun showWorkFinished() {
        run {
            progress_bar.visibility = View.GONE
            cancel_button.visibility = View.GONE
            go_button.visibility = View.VISIBLE
        }
    }

    private val blurLevel: Int
        get() =
            when (radio_blur_group.checkedRadioButtonId) {
                R.id.radio_blur_lv_1 -> 1
                R.id.radio_blur_lv_2 -> 2
                R.id.radio_blur_lv_3 -> 3
                else -> 1
            }
}
