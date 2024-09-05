package com.yong.aquamonitor.activity

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.yong.aquamonitor.R

class ConnectActivity : AppCompatActivity() {
    private var btnStartScan: Button? = null
    private var btnStopScan: Button? = null
    private var progressBar: ProgressBar? = null
    private var recyclerView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_connect)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnStartScan = findViewById(R.id.connect_btn_start)
        btnStopScan = findViewById(R.id.connect_btn_stop)
        progressBar = findViewById(R.id.connect_progress_bar)
        recyclerView = findViewById(R.id.connect_recycler_ble)

        btnStartScan!!.setOnClickListener(btnListener)
        btnStopScan!!.setOnClickListener(btnListener)

        progressBar!!.visibility = View.INVISIBLE
    }

    private fun startScan() {
        progressBar!!.visibility = View.VISIBLE
    }

    private fun stopScan() {
        progressBar!!.visibility = View.INVISIBLE
    }

    private val btnListener = View.OnClickListener { view ->
        when(view.id) {
            R.id.connect_btn_start -> startScan()
            R.id.connect_btn_stop -> stopScan()
        }
    }
}