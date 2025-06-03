package com.example.bak_python

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

class ScanActivity : ComponentActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var imageCapture: ImageCapture
    private lateinit var imageView: ImageView
    private lateinit var compareButton: Button
    private lateinit var screenDir: File
    private var cameraProvider: ProcessCameraProvider? = null

    private var photoCount = 0

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        checkLoginStatus()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_scan)

        previewView = findViewById(R.id.previewView)
        imageView = findViewById(R.id.click_image)
        compareButton = findViewById(R.id.compare_button)
        screenDir = File(filesDir, "screenshots")

        imageView.visibility = ImageView.GONE
        compareButton.visibility = Button.GONE

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        findViewById<Button>(R.id.camera_button).setOnClickListener {
            takePhoto()
        }

        findViewById<Button>(R.id.btn_scanS).setOnClickListener {
            startActivity(Intent(this, ScanActivity::class.java))
        }
        findViewById<Button>(R.id.btn_loginS).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        findViewById<Button>(R.id.btn_databaseS).setOnClickListener {
            startActivity(Intent(this, DatabaseActivity::class.java))
        }

        compareButton.setOnClickListener {
            startActivity(Intent(this, ComparingActivity::class.java))
            finish()
        }
        findViewById<Button>(R.id.btn_erase_screenshotsC).setOnClickListener {
            if (screenDir.exists() && screenDir.isDirectory) {
                screenDir.listFiles()?.forEach { it.delete() }
                Toast.makeText(this, "Screenshots cleared!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No screenshots to delete.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    this as LifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (exc: Exception) {
                Log.e("CameraX", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    fun rotateImageIfRequired(imagePath: String, bitmap: Bitmap): Bitmap {
        val exif = ExifInterface(imagePath)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )

        val matrix = Matrix()

        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            else -> return bitmap
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }


    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val imageFolder = File(filesDir, "screenshots")
        if (!imageFolder.exists()) {
            imageFolder.mkdirs()
        }

        val photoFile = File(
            imageFolder,
            "screenshot_${photoCount}.jpg"
        )
        photoCount += 1

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("CameraX", "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val msg = "Photo saved: ${photoFile.absolutePath}"
                    Log.d("CameraX", msg)

                    Thread {
                        try {
                            val originalBitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                            val rotatedBitmap = rotateImageIfRequired(photoFile.absolutePath, originalBitmap)
                            FileOutputStream(photoFile).use { out ->
                                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                            }

                            runOnUiThread {
                                Toast.makeText(baseContext, "Image was saved and can be used after clicking on 'COMPARE' button", Toast.LENGTH_LONG).show()
                                imageView.setImageBitmap(rotatedBitmap)
                                imageView.visibility = ImageView.VISIBLE
                                compareButton.visibility = Button.VISIBLE
                            }

                        } catch (e: Exception) {
                            Log.e("CameraX", "Error fixing orientation: ${e.message}", e)
                        }
                    }.start()
                }

            }
        )
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Permissions not granted.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun checkLoginStatus() {
        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        if (!prefs.getBoolean("isLoggedIn", false)) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraProvider?.unbindAll()
    }

    override fun onStop() {
        super.onStop()
        cameraProvider?.unbindAll()
    }

}
