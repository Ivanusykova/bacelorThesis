package com.example.bak_python

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class SavedActivity : ComponentActivity() {
    private lateinit var savedDir: File
    private lateinit var infoDir: File
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_saved)

        savedDir = File(filesDir, "saved_coins")

        recyclerView = findViewById(R.id.savedRecyclerView)
        restartView()

        findViewById<Button>(R.id.btn_scanF).setOnClickListener {
            startActivity(Intent(this, ScanActivity::class.java))
        }
        findViewById<Button>(R.id.btn_loginF).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        findViewById<Button>(R.id.btn_databaseF).setOnClickListener {
            startActivity(Intent(this, DatabaseActivity::class.java))
        }
        findViewById<Button>(R.id.btn_eraseF).setOnClickListener {
            eraseSaved()
        }
    }

    private fun restartView() {
        recyclerView.layoutManager = GridLayoutManager(this, 4)
        adapter = ImageAdapter(listOf()) { file ->
            val name = makeTitleHuman(file.nameWithoutExtension)
            val infoFile = File(savedDir, "${file.nameWithoutExtension}.txt")
            val description = if (infoFile.exists()) infoFile.readText() else "The description can be find in the Database"
            showInfoDialog(name, description)
        }
        recyclerView.adapter = adapter

        loadSavedImages()
    }

    private fun loadSavedImages() {
        val imageFiles = savedDir.listFiles()?.filter {
            it.isFile && it.extension.lowercase() == "jpg"
        } ?: emptyList()

        if (imageFiles.isEmpty()) {
            Toast.makeText(this, "No saved coin images found.", Toast.LENGTH_SHORT).show()
        }

        adapter.updateData(imageFiles)
    }

    private fun showInfoDialog(title: String, description: String) {
        val croppedDescription = Regex("""Description:\s*(.*?)\s*Issuing volume:""", RegexOption.DOT_MATCHES_ALL)
            .find(description)
            ?.groupValues?.get(1)
            ?.trim()
            ?: "description can be found in the database"

        val dialogView = layoutInflater.inflate(R.layout.dialog_view, null)
        val titleView = layoutInflater.inflate(R.layout.dialog_title, null)
        val titleShow = titleView.findViewById<TextView>(R.id.dialogTitle)
        val descView = dialogView.findViewById<TextView>(R.id.dialogDescription)
        val volumeShow = dialogView.findViewById<TextView>(R.id.dialogVolume)
        val savedButton = dialogView.findViewById<Button>(R.id.btn_saved)
        savedButton.visibility = Button.GONE
        volumeShow.visibility = View.GONE
        titleShow.text = title
        descView.text = croppedDescription

        runOnUiThread {
            com.google.android.material.dialog.MaterialAlertDialogBuilder(this, R.style.MyAlertDialogTheme)
                .setCustomTitle(titleView)
                .setView(dialogView)
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun makeTitleHuman(title: String): String {
        return title.replace("_", " ")
    }

    private fun eraseSaved() {
        if (savedDir.exists() && savedDir.isDirectory) {
            savedDir.listFiles()?.forEach { it.delete() }
            Toast.makeText(this, "Saved images are cleared!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "No images to delete.", Toast.LENGTH_SHORT).show()
        }
        restartView()
    }
}
