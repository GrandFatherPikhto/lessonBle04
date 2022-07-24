package com.grandfatherpikhto.blin

import android.app.PendingIntent
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class BcScanReceiver constructor(private val bleScanManager: BleScanManager, dispatcher: CoroutineDispatcher = Dispatchers.IO): BroadcastReceiver() {
    companion object {
        const val ACTION_BLE_SCAN = "com.pikhto.lessonble04.ACTION_BLE_SCAN"
        const val REQUEST_CODE_BLE_SCANNER_PENDING_INTENT = 1000
    }
    private val logTag = this.javaClass.simpleName
    private val bcPendingIntent: PendingIntent by lazy {
        PendingIntent.getBroadcast(
            bleScanManager.applicationContext,
            REQUEST_CODE_BLE_SCANNER_PENDING_INTENT,
            Intent(ACTION_BLE_SCAN),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    val pendingIntent get() = bcPendingIntent

    private fun extractScanError(intent: Intent) : Int {
        if (intent.hasExtra(BluetoothLeScanner.EXTRA_CALLBACK_TYPE)) {
            val callbackType =
                intent.getIntExtra(BluetoothLeScanner.EXTRA_CALLBACK_TYPE, -1)
            if (callbackType >= 0) {
                if (intent.hasExtra(BluetoothLeScanner.EXTRA_ERROR_CODE)) {
                    val errorCode
                            = intent.getIntExtra(BluetoothLeScanner.EXTRA_ERROR_CODE, -1 )
                    Log.e(logTag, "Scan error: $errorCode")
                    if (errorCode != -1) {
                        bleScanManager.onReceiveError(errorCode)
                    }
                    return errorCode
                }
            }
        }
        return -1
    }

    private fun extractScanResult(intent: Intent) : ScanResult? {
        val errorCode = extractScanError(intent)

        if (errorCode == -1 && intent.hasExtra(BluetoothLeScanner.EXTRA_LIST_SCAN_RESULT)) {
            intent.getParcelableArrayListExtra<ScanResult>(BluetoothLeScanner.EXTRA_LIST_SCAN_RESULT)
                ?.let { results ->
                    results.forEach { result ->
                        result.device?.let { device ->
                            Log.d(logTag, "Device: $device")
                            bleScanManager.onReceiveScanResult(result)
                            return result
                        }
                    }
                }
        }

        return null
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if ( context != null && intent != null ) {
            when (intent.action) {
                ACTION_BLE_SCAN -> {
                    extractScanResult(intent)
                }
                else -> {
                    Log.d(logTag, "Action: ${intent.action}")
                }
            }
        }
    }

}