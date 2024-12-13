package com.wantee.formedia

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.wantee.camera.CameraContext
import com.wantee.common.log.Log
import com.wantee.common.log.LogEnum

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private lateinit var mSelectorView: LinearLayout
    val REQUEST_CODE_CHOOSE: Int = 23

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CameraContext.Instance.context = this
        Log.setLogger { level, tag, message ->
            message?.let {
                if (level == LogEnum.Error) {
                    android.util.Log.e(tag, it)
                } else if (level == LogEnum.Waring) {
                    android.util.Log.w(tag, message)
                } else {
                    android.util.Log.i(tag, message)
                }
            }
        }
        setContentView(R.layout.activity_main)
        mSelectorView = findViewById(R.id.ll_selector)
        mSelectorView.setOnClickListener({

        })
    }

    override fun onDestroy() {
        super.onDestroy()
        CameraContext.Instance.context = null
    }
}