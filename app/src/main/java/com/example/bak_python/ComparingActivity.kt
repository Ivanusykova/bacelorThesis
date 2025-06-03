package com.example.bak_python

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import okhttp3.*
import org.json.JSONArray
import java.time.Year


class ComparingActivity : ComponentActivity() {
    private lateinit var adapter: ImageAdapter
    private lateinit var adapterYear: ImageAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewScreenshots: RecyclerView
    private lateinit var coinsDir: File
    private lateinit var circulDir: File
    private lateinit var screenDir: File
    private lateinit var selectedScreen: String
    private lateinit var selectedYear: String
    private lateinit var yearSpinner: Spinner
    private lateinit var numberSpinner: Spinner
//    private lateinit var eraseScreenshotsButton: Button
    private lateinit var recompareButton: Button
    private lateinit var compareButton: Button
    private lateinit var databaseOpenerButton: Button
    private var selectedTop: Int = 10
    private lateinit var waitingProgressBar: ProgressBar
    private lateinit var explainingTextView: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        setContentView(R.layout.activity_comparing)
        recyclerView = findViewById(R.id.recycler_view_comparing)
        recyclerViewScreenshots = findViewById(R.id.recycler_view_screenshots)
        coinsDir = File(filesDir, "coins_images")
        circulDir = File(filesDir, "circulation_coins")
        screenDir = File(filesDir, "screenshots")
        yearSpinner = findViewById(R.id.yearSpinner)
        numberSpinner = findViewById(R.id.topSpinner)
        // eraseScreenshotsButton = findViewById(R.id.btn_erase_screenshots)
        recompareButton = findViewById(R.id.btn_recomparing)
        compareButton = findViewById(R.id.btn_comparing)
        databaseOpenerButton = findViewById(R.id.btn_databaseOpener)
        waitingProgressBar = findViewById(R.id.waitingBar)
        explainingTextView = findViewById(R.id.text_explaining)

        makeSpinner()
        lifecycleScope.launch {
            selectScreenshot()
            topSpinner()
        }

        findViewById<Button>(R.id.btn_scanCo).setOnClickListener {
            startActivity(Intent(this, ScanActivity::class.java))
        }
        findViewById<Button>(R.id.btn_loginCo).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        findViewById<Button>(R.id.btn_databaseCo).setOnClickListener {
            startActivity(Intent(this, DatabaseActivity::class.java))
        }
        findViewById<Button>(R.id.btn_comparing).setOnClickListener {
            recyclerViewScreenshots.visibility = RecyclerView.VISIBLE
            waitingProgressBar.visibility = ProgressBar.VISIBLE

            if (!::selectedScreen.isInitialized) {
                Toast.makeText(this, "Please select a screenshot first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!::selectedYear.isInitialized) {
                Toast.makeText(this, "Please select a year first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                compare()
            }
        }
//        findViewById<Button>(R.id.btn_erase_screenshots).setOnClickListener{
//            if (screenDir.exists() && screenDir.isDirectory) {
//                screenDir.listFiles()?.forEach { it.delete() }
//                Toast.makeText(this, "Screenshots cleared!", Toast.LENGTH_SHORT).show()
//            } else {
//                Toast.makeText(this, "No screenshots to delete.", Toast.LENGTH_SHORT).show()
//            }
//        }

        recompareButton.setOnClickListener {
            recyclerViewScreenshots.visibility = RecyclerView.VISIBLE
//            eraseScreenshotsButton.visibility = Button.VISIBLE
            numberSpinner.visibility = Spinner.VISIBLE
            yearSpinner.visibility = Spinner.VISIBLE
            compareButton.visibility = Button.VISIBLE
            recompareButton.visibility = Button.GONE
            recyclerView.visibility = RecyclerView.GONE
        }

//        if (!screenDir.exists() || !screenDir.isDirectory){
//            eraseScreenshotsButton.visibility = Button.GONE
//        }

        databaseOpenerButton.setOnClickListener {
            val intent = Intent(this, DatabaseActivity::class.java)
            intent.putExtra("selected_year", selectedYear)
            startActivity(intent)
        }
    }

    private suspend fun compare() {
        recyclerViewScreenshots.visibility = RecyclerView.GONE
       //  eraseScreenshotsButton.visibility = Button.GONE
        numberSpinner.visibility = Spinner.GONE
        yearSpinner.visibility = Spinner.GONE
        databaseOpenerButton.visibility = Button.VISIBLE
        explainingTextView.visibility = TextView.GONE
        recyclerView.visibility = RecyclerView.VISIBLE

        download()
    }

    private suspend fun download() {
        val outputText = if (selectedYear == "circulation_coins") {
            "your coin is not commemorative and sadly its worth equals its nominal value"
        } else {
            "your coin might be worth more than its nominal value"
        }

        withContext(Dispatchers.IO) {
            try {
                val py = Python.getInstance()
                val module = py.getModule("SHIFT")
                val screenshot = File(screenDir, selectedScreen)

                if (!screenshot.exists()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ComparingActivity, "Screenshot not found.", Toast.LENGTH_SHORT).show()
                    }
                    return@withContext
                }

                val resultJson = module.callAttr(
                    "find_top_similar_images",
                    filesDir.absolutePath,
                    screenshot.absolutePath,
                    selectedYear,
                    selectedTop
                ).toString()

                val jsonArray = JSONArray(resultJson)
                val imageFiles = mutableListOf<File>()

                for (i in 0 until jsonArray.length()) {
                    val path = jsonArray.getJSONObject(i).getString("path")
                    val file = File(path)
                    if (file.exists()) imageFiles.add(file)
                }

                withContext(Dispatchers.Main) {
                    if (!isFinishing && !isDestroyed) {
                        recyclerView.layoutManager = GridLayoutManager(this@ComparingActivity, 4)
                        adapter = ImageAdapter(imageFiles) { clickedFile ->
                            Toast.makeText(
                                this@ComparingActivity,
                                "${clickedFile.name.replace("_", " ").replace(".jpg", "")} was selected, $outputText",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        recyclerView.adapter = adapter
                        recompareButton.visibility = View.VISIBLE
                        compareButton.visibility = View.GONE
                        waitingProgressBar.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                Log.e("ComparingActivity", "Python error", e)
                withContext(Dispatchers.Main) {
                    if (!isFinishing && !isDestroyed) {
                        Toast.makeText(this@ComparingActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        waitingProgressBar.visibility = View.GONE
                    }
                }
            }
        }
    }


    private suspend fun selectScreenshot() {
        val imageFiles = mutableListOf<File>()
        val files = screenDir.listFiles()
        if (files != null) {
            for (file in files) {
                if (file.exists() && file.isFile) {
                    imageFiles.add(file)
                }
            }
        }

        withContext(Dispatchers.Main) {
            recyclerViewScreenshots.layoutManager = GridLayoutManager(this@ComparingActivity, 4)
            adapterYear = ImageAdapter(imageFiles) { clickedFile ->
                selectedScreen = clickedFile.name
                Toast.makeText(this@ComparingActivity, "Selected: ${clickedFile.name}", Toast.LENGTH_SHORT).show()
            }
            recyclerViewScreenshots.adapter = adapterYear
        }
    }

    fun makeSpinner() {
        val spinner: Spinner = findViewById(R.id.yearSpinner)
        val yearFolders = coinsDir
            .listFiles()
            ?.filter { it.isDirectory && it.name.matches(Regex("^\\d{4}$")) }
            ?.map { it.name }
            ?.sorted()
            ?.toMutableList()
            ?: mutableListOf()

        yearFolders.add(circulDir.name)

        if (yearFolders.isEmpty()) {
            Toast.makeText(this, "No year folders found.", Toast.LENGTH_SHORT).show()
            spinner.isEnabled = false
            return
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, yearFolders)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(0)

        selectedYear = yearFolders[0]

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selected = yearFolders[position]
                selectedYear = selected
            }

            override fun onNothingSelected(parent: AdapterView<*>) { }
        }
    }

    fun topSpinner() {
        val spinner: Spinner = findViewById(R.id.topSpinner)
        val numberList = (0..10).map { it.toString() }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, numberList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(0)

        selectedTop = numberList[0].toInt()

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedTop = numberList[position].toInt()
            }

            override fun onNothingSelected(parent: AdapterView<*>) { }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleScope.coroutineContext.cancelChildren()
    }

}