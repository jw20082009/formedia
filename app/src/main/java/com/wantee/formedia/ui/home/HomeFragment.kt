package com.wantee.formedia.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.wantee.camera.CameraContext
import com.wantee.camera.EquipmentType
import com.wantee.camera.PreviewListener
import com.wantee.camera.PreviewType
import com.wantee.camera.Previewer
import com.wantee.common.log.Log
import com.wantee.formedia.databinding.FragmentHomeBinding
import com.wantee.render.view.SurfacePlayer

class HomeFragment : Fragment() {
    private val tag: String = "HomeFragment"
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val player: SurfacePlayer = SurfacePlayer()
    private val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        CameraContext.Instance.setSurfaceListener(object :PreviewListener<Surface>{
            override fun onOpened(requestCode: Int, displayRotate: Int) {
            }

            override fun onClosed(requestCode: Int) {
            }

            override fun onError(requestCode: Int, errorMessage: String?) {
            }

            override fun onStartPreview(
                requestCode: Int,
                equipmentType: EquipmentType?
            ): Previewer<Surface> {
                return object : Previewer<Surface>() {
                    override fun type(): PreviewType {
                        return PreviewType.SurfaceTexture
                    }

                    override fun createDestination(previewWidth: Int, previewHeight: Int): Surface {
                        return player.tryObtainSurface(previewWidth, previewHeight)
                    }
                }
            }
        })
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        if (checkPermission()) {
            openCamera()
        }
        player.setView(binding.glsurfaceview)
        player.onResume()
    }

    override fun onPause() {
        super.onPause()
        CameraContext.Instance.close()
        player.onPause()
    }

    private fun openCamera() {
        CameraContext.Instance.open(EquipmentType.Front_Camera2, 720, 1280)
    }

    private fun checkPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= 23) {
            var permissionAllGranted = true
            val permissionResults = IntArray(permissions.size)
            for (i in permissions.indices) {
                val p = permissions[i]
                if (activity?.checkSelfPermission(p) != PackageManager.PERMISSION_GRANTED) {
                    permissionAllGranted = false
                }
                permissionResults[i] = PackageManager.PERMISSION_GRANTED
            }
            if (!permissionAllGranted) {
                activity?.requestPermissions(permissions, 0)
            } else {
                handlePermission(0, permissions, permissionResults)
            }
            return permissionAllGranted
        }
        return true
    }

    protected fun handlePermission(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == 0) {
            val deniedPermission = StringBuilder("denied:")
            var granted = true
            for (i in grantResults.indices) {
                val result = grantResults[i]
                if (result != PackageManager.PERMISSION_GRANTED) {
                    granted = false
                    deniedPermission.append(permissions[i]).append(";")
                }
            }
            if (granted) {
                openCamera()
            } else {
                Log.e(tag, deniedPermission.toString())
                Toast.makeText(activity, deniedPermission.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }
}