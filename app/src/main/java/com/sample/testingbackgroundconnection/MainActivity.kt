package com.sample.testingbackgroundconnection

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.filo.testingbackgroundconnection.R
import com.sample.testingbackgroundconnection.ble.BleScanner

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bleScanner = BleScanner(applicationContext)

        findViewById<Button>(R.id.btnStartScan).setOnClickListener {
            Log.i(TAG, "> START SCAN")
            bleScanner.startScan(object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult?) {
                    super.onScanResult(callbackType, result)
                    Log.i(TAG, "onScanResult: " + result?.device?.address)
                }

                override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                    super.onBatchScanResults(results)
                    Log.i(TAG, "onBatchScanResults: $results")
                }

                override fun onScanFailed(errorCode: Int) {
                    super.onScanFailed(errorCode)
                    Toast.makeText(
                        applicationContext,
                        "onScanFailed: $errorCode",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(TAG, "onScanFailed: $errorCode")
                }
            })
        }

        findViewById<Button>(R.id.btnStopScan).setOnClickListener {
            Log.i(TAG, "[] STOP SCAN: ")
            bleScanner.stopScan()
        }

        //FOREGROUND
        findViewById<Button>(R.id.btnStartForeground).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val startForegroundService =
                    Intent(applicationContext, MyForegroundService::class.java)
                applicationContext.startForegroundService(startForegroundService)
            }
        }

        findViewById<Button>(R.id.btnStopForeground).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val startForegroundService =
                    Intent(applicationContext, MyForegroundService::class.java)
                applicationContext.stopService(startForegroundService)
            }
        }
    }
}