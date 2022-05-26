package com.sample.testingbackgroundconnection

import android.annotation.SuppressLint
import android.app.*
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.filo.testingbackgroundconnection.R
import com.sample.testingbackgroundconnection.ble.BleScanner

private const val TAG = "MyForegroundService"

class MyForegroundService : Service() {

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate: ")
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.i(TAG, "onTaskRemoved: ")
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.i(TAG, "onBind: ")
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand: ")

        // If the notification supports a direct reply action, use
        // PendingIntent.FLAG_MUTABLE instead.
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(
                    this, 0, notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            }

        val channelId = createNotificationChannel("my_service", "My Background Service")
        val notification: Notification = Notification.Builder(this, channelId)
            .setContentTitle("Notification Title")
            .setContentText("Notification Message")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setTicker("Notification Ticker")
            .build()

        // Notification ID cannot be 0.
        startForeground(2308, notification)

        startScan()
        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: BluetoothDevice?) {
        device?.connectGatt(applicationContext, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)
                Log.i(TAG, "onConnectionStateChange: status $status newState $newState")

                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.i(TAG, "STATE_CONNECTED")
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.i(TAG, "STATE_DISCONNECTED")
                    gatt?.close()
                    startScan()
                }
            }
        })
    }

    private fun startScan() {
        val bleScanner = BleScanner(applicationContext)

        Log.i(TAG, "> START SCAN")
        bleScanner.startScan(object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                super.onScanResult(callbackType, result)
                Log.i(TAG, "onScanResult: " + result?.device?.address)

                if (result?.device?.address == "A0:BB:3E:AE:CD:73") {
                    Toast.makeText(
                        applicationContext,
                        "DEVICE FOUND STOP SCAN!",
                        Toast.LENGTH_SHORT
                    ).show()
                    bleScanner.stopScan()
                    connectToDevice(result.device)
                }
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
                Log.i(TAG, "onScanFailed: WAIT 30 SEC")
                Handler().postDelayed(
                    {
                        Log.i(TAG, "scan again!")
                        startScan()
                    }, 30_000
                )
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

}