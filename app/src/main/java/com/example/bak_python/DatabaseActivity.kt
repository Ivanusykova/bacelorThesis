package com.example.bak_python

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class DatabaseActivity : ComponentActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ImageAdapter
    private lateinit var coinsDir: File
    private lateinit var circulDir: File
    private lateinit var infoDir: File
    private lateinit var savedDir: File
    private lateinit var selectedYear: String
    private lateinit var textSpinner: Spinner
    private lateinit var allTexts: Map<String, String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_database)
        checkLoginStatus()
        coinsDir = File(filesDir, "coins_images")
        infoDir = File(filesDir, "coins_info")
        circulDir = File(filesDir, "circulation_coins")
        savedDir = File(filesDir, "saved_coins")
        selectedYear = intent.getStringExtra("selected_year") ?: "-1"

        if (!savedDir.exists()) { savedDir.mkdirs() }

        if (coinsDir.exists() && coinsDir.listFiles()?.isNotEmpty() == true &&
            infoDir.exists() && infoDir.listFiles()?.isNotEmpty() == true &&
            circulDir.exists() && circulDir.listFiles()?.isNotEmpty() == true) {
            Toast.makeText(this, "all we need is downloaded:)", Toast.LENGTH_LONG).show()
        } else {
            val databaseButton: Button = findViewById(R.id.btn_downloadD)
            databaseButton.visibility = View.VISIBLE
            databaseButton.setOnClickListener{ startActivity(Intent(this, DownloadActivity::class.java)) }
        }

        findViewById<Button>(R.id.btn_scan).setOnClickListener {
            startActivity(Intent(this, ScanActivity::class.java))
        }
        findViewById<Button>(R.id.btn_login).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        findViewById<Button>(R.id.btn_favorites).setOnClickListener {
            startActivity(Intent(this, SavedActivity::class.java))
        }
        findViewById<Button>(R.id.btn_databaseD).setOnClickListener {
            startActivity(Intent(this, DatabaseActivity::class.java))
        }

        findViewById<Button>(R.id.btn_text).setOnClickListener {
            returnAllTextFromYear(selectedYear)
        }

        recyclerView = findViewById(R.id.coinsRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 4)
        adapter = ImageAdapter(listOf()) { file ->
            val croppedDescription = Regex("""Description:\s*(.+)\s*Issuing volume:""", RegexOption.DOT_MATCHES_ALL)
                .find(returnTextFromImage(file))
                ?.groupValues?.get(1)
                ?.trim()
                ?: "No description available."
            val name = makeTitleHuman(file.name.replace(".jpg", ""))
            showInfoDialog(name, croppedDescription, file)
        }
        recyclerView.adapter = adapter

        setupSpinner()

        textSpinner = findViewById(R.id.textSpinner)
        textSpinner.visibility = View.GONE

    }

    fun loadImages(folderName: String? = null) {
        val coinImages = mutableListOf<File>()
        val baseDir: File = when {
            folderName == null -> coinsDir
            folderName == circulDir.name -> circulDir
            else -> File(coinsDir, folderName)
        }

        if (baseDir.exists()) {
            baseDir.walkTopDown().forEach { file ->
                if (file.isFile && file.extension.lowercase() == "jpg") {
                    coinImages.add(file)
                }
            }

            if (coinImages.isEmpty()) {
                Toast.makeText(this, "No coin images found${if (folderName != null) " in $folderName" else ""}!", Toast.LENGTH_SHORT).show()
            }

            adapter.updateData(coinImages)
        } else {
            Toast.makeText(this, "Folder not found: ${baseDir.name}", Toast.LENGTH_SHORT).show()
        }
    }

    fun setupSpinner() {
        val spinner: Spinner = findViewById(R.id.folderSpinner)

        val yearFolders = coinsDir
            .listFiles()
            ?.filter { it.isDirectory && it.name.matches(Regex("^\\d{4}$")) }
            ?.map { it.name }
            ?.sorted()
            ?.toMutableList()
            ?: mutableListOf()

        yearFolders.add(circulDir.name)

        if (yearFolders.isEmpty()) {
            Toast.makeText(this, "No year folders found. Showing all images.", Toast.LENGTH_SHORT).show()
            spinner.isEnabled = false
            loadImages()
            return
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, yearFolders)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(0)

        loadImages(yearFolders[0])
        selectedYear = yearFolders[0]

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selected = yearFolders[position]
                selectedYear = selected

                loadImages(selected)

                textSpinner.visibility = View.GONE
                textSpinner.adapter = null
                allTexts = emptyMap()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                loadImages()
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

    fun returnTextFromImage(imageFile: File): String {
        val imageBaseName = imageFile.nameWithoutExtension
        val imageYear = imageFile.parentFile?.name ?: return "Unknown year"
        val textFile = File(infoDir, "$imageYear/$imageBaseName.txt")

        return if (textFile.exists()) {
            textFile.readText()
        } else {
            "Information about this coin: ${makeTitleHuman(imageBaseName)} can be found at the bottom of the page, with information button, some images have different names, thank you for understanding:)"
        }
    }

    fun showInfoDialog(title: String, description: String, file: File) {
        val croppedDescription = Regex("""Description:\s*(.*?)\s*Issuing volume:""", RegexOption.DOT_MATCHES_ALL)
            .find(description)
            ?.groupValues?.get(1)
            ?.trim()
            ?: "No description available."

        val croppedIssueValue = Regex("""Issuing volume:\s*(.*?)\s*Issuing date:""", RegexOption.DOT_MATCHES_ALL)
            .find(description)
            ?.groupValues?.get(1)
            ?.trim()
            ?: "No description available."

        val dialogView = layoutInflater.inflate(R.layout.dialog_view, null)
        val titleView = layoutInflater.inflate(R.layout.dialog_title, null)
        val titleShow = titleView.findViewById<TextView>(R.id.dialogTitle)
        val descView = dialogView.findViewById<TextView>(R.id.dialogDescription)
        val volumeShow = dialogView.findViewById<TextView>(R.id.dialogVolume)
        val savedButton = dialogView.findViewById<Button>(R.id.btn_saved)
        titleShow.text = title
        descView.text = croppedDescription
        if (croppedDescription == "No description available.") {
            volumeShow.text = ""
            volumeShow.visibility = View.GONE
        } else {
            volumeShow.text = buildString {
                append("Issuing volume: ")
                append(croppedIssueValue)
            }
            volumeShow.visibility = View.VISIBLE
        }

        runOnUiThread {
            com.google.android.material.dialog.MaterialAlertDialogBuilder(this, R.style.MyAlertDialogTheme)
                .setCustomTitle(titleView)
                .setView(dialogView)
                .setPositiveButton("OK", null)
                .show()
        }

        savedButton.setOnClickListener{
            savedToFavorites(file)
        }
    }

    private fun returnAllTextFromYear(year: String) {
        textSpinner.visibility = View.GONE
        textSpinner.adapter = null
        val textFiles = mutableListOf<File>()

        if (year == "-1") {
            infoDir.walkTopDown().forEach { file ->
                if (file.isFile && file.extension.lowercase() == "txt") {
                    if (!textFiles.contains(file)) {
                        textFiles.add(file)
                    }
                }
            }
        } else {
            val yearFolder = File(infoDir, year)
            if (yearFolder.exists()) {
                yearFolder.walkTopDown().forEach { file ->
                    if (file.isFile && file.extension.lowercase() == "txt") {
                        if (!textFiles.contains(file)) {
                            textFiles.add(file)
                        }
                    }
                }
            }
        }

        if (textFiles.isEmpty()) {
            if (selectedYear == circulDir.name) {
                Toast.makeText(this, "This coin is circulation coin. No information about this coin is available. The value of this coin matches the value of its head side.", Toast.LENGTH_SHORT).show()
            }
            Toast.makeText(this, "No text files found!", Toast.LENGTH_SHORT).show()
            return
        }

        var namePairs = textFiles.map { it.nameWithoutExtension to makeTitleHuman(it.nameWithoutExtension) }
        namePairs = namePairs.distinctBy { it.second }

        allTexts = textFiles.associate { it.nameWithoutExtension to it.readText() }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, namePairs.map { it.second })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        textSpinner.adapter = adapter
        textSpinner.visibility = View.VISIBLE

        textSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedRawName = namePairs[position].first
                val displayName = namePairs[position].second

                val textContent = allTexts[selectedRawName] ?: "No content found."
                showInfoDialog(displayName, textContent, File(selectedRawName))
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }


    fun makeTitleHuman(title: String): String {
        return title.replace("_", " ")
    }

    fun savedToFavorites(file: File) {
        if (!savedDir.exists()) {
            savedDir.mkdirs()
        }

        if (!file.exists()) {
            Toast.makeText(this, "The source image doesn't exist.", Toast.LENGTH_LONG).show()
            return
        }

        val destImageFile = File(savedDir, file.name)
        if (destImageFile.exists()) {
            Toast.makeText(this, "This coin is already saved.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            file.copyTo(destImageFile, overwrite = false)
            Toast.makeText(this, "Coin saved to favorites!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to save coin: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
