package com.wantee.formedia

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.wantee.camera.CameraContext
import com.wantee.common.log.Log
import com.wantee.common.log.LogEnum
import com.wantee.formedia.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

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
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main2)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        navView.setupWithNavController(navController)
    }

    override fun onDestroy() {
        super.onDestroy()
        CameraContext.Instance.context = null
    }
}