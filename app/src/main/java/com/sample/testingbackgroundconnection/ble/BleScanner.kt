package com.sample.testingbackgroundconnection.ble

import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.Context

class BleScanner(context: Context) {

    private var btAdapter =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    private var scanCallback: ScanCallback? = null

    fun startScan(scanCallback: ScanCallback) {
        //Stoppiamo eventuali scan fatti partire ma mai stoppati
        scanCallback?.let {
            btAdapter.bluetoothLeScanner.stopScan(this.scanCallback)
        }

        this.scanCallback = scanCallback
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .build()

        val scanFilterFilo = ScanFilter.Builder()
            .setDeviceName("FILO").build()

        val scanFilterFiloTag = ScanFilter.Builder()
            .setDeviceName("FILO-TAG").build()

        btAdapter.bluetoothLeScanner.startScan(
            mutableListOf(scanFilterFilo, scanFilterFiloTag), scanSettings, this.scanCallback
        )
    }

    fun stopScan() {
        btAdapter.bluetoothLeScanner.stopScan(this.scanCallback)
    }

    fun Context.isBluetoothEnabled(): Boolean {
        val bluetoothManager = this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        return bluetoothManager.adapter.isEnabled
    }
}