package com.example.bak_python

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.TextView
import java.io.File
import android.content.res.AssetManager
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream


// https://www.ecb.europa.eu/euro/coins/comm/html/index.en.html
// https://www.coin-database.com/series/eurozone-commemorative-2-euro-coins-2-euro.html

class DownloadActivity : ComponentActivity() {
    private lateinit var coinsDir: File
    private lateinit var infoDir: File
    private lateinit var ecbButton: Button
    private lateinit var textButton: Button
    private lateinit var backButton: Button
    private lateinit var redownloadButton: Button
    private lateinit var textDownloading: TextView
    private lateinit var textDownloaded: TextView
    private lateinit var textDelay: TextView
    private lateinit var textInfoSites: TextView
    private lateinit var downloadProcess: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        copyCirculationCoinsFolder()
        setContentView(R.layout.activity_download)

        ecbButton = findViewById(R.id.ecb_button)
        textButton = findViewById(R.id.text_button)
        backButton = findViewById(R.id.back_button)
        textDownloading = findViewById(R.id.text_downloading)
        textDownloaded = findViewById(R.id.text_downloaded)
        redownloadButton = findViewById(R.id.redownload_button)
        coinsDir = File(filesDir, "coins_images")
        infoDir = File(filesDir, "coins_info")
        downloadProcess = findViewById(R.id.downloadProgress)
        textDelay = findViewById(R.id.text_delay)
        textInfoSites = findViewById(R.id.text_info_sites)


        ecbButton.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://www.ecb.europa.eu/euro/coins/comm/html/index.en.html")
            })
        }

        textButton.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://www.coin-database.com/series/eurozone-commemorative-2-euro-coins-2-euro.html")
            })
        }

        backButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        redownloadButton.setOnClickListener {
            lifecycleScope.launch {
                redownload()
            }
        }

        if (coinsDir.exists() && coinsDir.listFiles()?.isNotEmpty() == true &&
            infoDir.exists() && infoDir.listFiles()?.isNotEmpty() == true) {
            textDownloading.visibility = TextView.GONE
            downloadProcess.visibility = TextView.GONE
            textDelay.visibility = TextView.GONE
            textDownloaded.visibility = TextView.VISIBLE
            backButton.visibility = Button.VISIBLE
        } else {
            lifecycleScope.launch {
                textDownloading.visibility = TextView.VISIBLE
                downloadProcess.visibility = TextView.VISIBLE
                textDelay.visibility = TextView.VISIBLE
                textDownloaded.visibility = TextView.GONE
                backButton.visibility = Button.GONE

                try {
                    download()
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@DownloadActivity, "Download failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        textDownloading.visibility = TextView.GONE
                        downloadProcess.visibility = TextView.GONE
                        textDownloaded.visibility = TextView.VISIBLE
                        backButton.visibility = Button.VISIBLE
                    }
                }
            }
        }
    }

    private suspend fun download() {
        withContext(Dispatchers.IO) {
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(this@DownloadActivity))
            }
            val py = Python.getInstance()
            val moduleImage = py.getModule("coins_images")
            val moduleInfo = py.getModule("coins_info")
            val scriptDir = filesDir.absolutePath
            moduleImage.callAttr("download_images", scriptDir)
            moduleInfo.callAttr("download_info", scriptDir)
        }
    }

    private suspend fun redownload() {
        withContext(Dispatchers.IO) {
            if (coinsDir.exists()) deleteRecursive(coinsDir)
            if (infoDir.exists()) deleteRecursive(infoDir)
        }
        textDownloading.visibility = TextView.VISIBLE
        downloadProcess.visibility = TextView.VISIBLE
        textDownloaded.visibility = TextView.GONE
        backButton.visibility = Button.GONE

        download()

        textDownloading.visibility = TextView.GONE
        downloadProcess.visibility = TextView.GONE
        textDownloaded.visibility = TextView.VISIBLE
        backButton.visibility = Button.VISIBLE
    }

    private fun deleteRecursive(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory) {
            fileOrDirectory.listFiles()?.forEach { child ->
                deleteRecursive(child)
            }
        }
        fileOrDirectory.delete()
    }

    private fun copyCirculationCoinsFolder() {
        val assetManager: AssetManager = assets
        val assetFolder = "circulation_coins"
        val outDir = File(filesDir, assetFolder)

        if (!outDir.exists()) {
            outDir.mkdirs()
        }

        try {
            val files = assetManager.list(assetFolder)
            files?.forEach { filename ->
                if (filename.endsWith(".jpg")) {
                    val inStream: InputStream = assetManager.open("$assetFolder/$filename")
                    val outFile = File(outDir, filename)
                    val outStream: OutputStream = FileOutputStream(outFile)

                    val buffer = ByteArray(1024)
                    var read: Int
                    while (inStream.read(buffer).also { read = it } != -1) {
                        outStream.write(buffer, 0, read)
                    }

                    inStream.close()
                    outStream.flush()
                    outStream.close()
                }
            }
            println("images copied successfully")
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error: coping image -> ${e.localizedMessage}")
        }
    }
}

