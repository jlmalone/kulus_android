package org.kulus.android.service

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.ParcelUuid
import android.util.Log
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bluetooth glucose service for Contour Next One meter.
 * Implements official Bluetooth Glucose Service (UUID 0x1808) with SFLOAT16 parsing.
 * Matches iOS BluetoothService.swift feature set.
 */

enum class BluetoothConnectionState {
    DISCONNECTED,
    SCANNING,
    CONNECTING,
    CONNECTED,
    FAILED
}

data class GlucoseMeasurement(
    val value: Double,        // in mg/dL from device
    val valueMmol: Double,    // converted to mmol/L
    val sequenceNumber: Int,
    val timestamp: Date?,
    val deviceName: String?
)

@Singleton
class BluetoothGlucoseService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "BluetoothGlucose"

        // Standard Bluetooth Glucose Service UUIDs
        val GLUCOSE_SERVICE_UUID: UUID = UUID.fromString("00001808-0000-1000-8000-00805F9B34FB")
        val GLUCOSE_MEASUREMENT_UUID: UUID = UUID.fromString("00002A18-0000-1000-8000-00805F9B34FB")
        val GLUCOSE_MEASUREMENT_CONTEXT_UUID: UUID = UUID.fromString("00002A34-0000-1000-8000-00805F9B34FB")
        val GLUCOSE_FEATURE_UUID: UUID = UUID.fromString("00002A51-0000-1000-8000-00805F9B34FB")
        val RECORD_ACCESS_CONTROL_UUID: UUID = UUID.fromString("00002A52-0000-1000-8000-00805F9B34FB")

        val DEVICE_INFO_SERVICE_UUID: UUID = UUID.fromString("0000180A-0000-1000-8000-00805F9B34FB")

        // Client Characteristic Configuration Descriptor
        val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")

        // Unit conversion factor
        private const val MMOL_FACTOR = 18.0182
    }

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private var bluetoothGatt: BluetoothGatt? = null

    private val _connectionState = MutableStateFlow(BluetoothConnectionState.DISCONNECTED)
    val connectionState: StateFlow<BluetoothConnectionState> = _connectionState.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<BluetoothDevice>> = _discoveredDevices.asStateFlow()

    private val _measurements = MutableStateFlow<List<GlucoseMeasurement>>(emptyList())
    val measurements: StateFlow<List<GlucoseMeasurement>> = _measurements.asStateFlow()

    private val _connectedDeviceName = MutableStateFlow<String?>(null)
    val connectedDeviceName: StateFlow<String?> = _connectedDeviceName.asStateFlow()

    val isBluetoothEnabled: Boolean
        get() = bluetoothAdapter?.isEnabled == true

    val isBluetoothSupported: Boolean
        get() = bluetoothAdapter != null

    fun hasPermissions(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }

    @SuppressLint("MissingPermission")
    fun startScanning() {
        if (!hasPermissions()) {
            Log.w(TAG, "Missing Bluetooth permissions")
            _connectionState.value = BluetoothConnectionState.FAILED
            return
        }

        if (!isBluetoothEnabled) {
            Log.w(TAG, "Bluetooth is not enabled")
            _connectionState.value = BluetoothConnectionState.FAILED
            return
        }

        _connectionState.value = BluetoothConnectionState.SCANNING
        _discoveredDevices.value = emptyList()

        bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner

        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(GLUCOSE_SERVICE_UUID))
            .build()

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        bluetoothLeScanner?.startScan(listOf(filter), settings, scanCallback)
        Log.d(TAG, "Started BLE scan for glucose devices")
    }

    @SuppressLint("MissingPermission")
    fun stopScanning() {
        bluetoothLeScanner?.stopScan(scanCallback)
        if (_connectionState.value == BluetoothConnectionState.SCANNING) {
            _connectionState.value = BluetoothConnectionState.DISCONNECTED
        }
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(device: BluetoothDevice) {
        stopScanning()
        _connectionState.value = BluetoothConnectionState.CONNECTING
        _connectedDeviceName.value = device.name

        bluetoothGatt = device.connectGatt(context, false, gattCallback)
        Log.d(TAG, "Connecting to ${device.name ?: device.address}")
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        _connectionState.value = BluetoothConnectionState.DISCONNECTED
        _connectedDeviceName.value = null
    }

    /**
     * Parse IEEE-11073 SFLOAT16 format.
     * 4-bit signed exponent + 12-bit signed mantissa.
     * Matches iOS BluetoothService SFLOAT16 parsing.
     */
    fun parseSFLOAT16(data: ByteArray, offset: Int): Double {
        if (offset + 1 >= data.size) return 0.0

        val raw = (data[offset].toInt() and 0xFF) or ((data[offset + 1].toInt() and 0xFF) shl 8)

        // Extract exponent (4 MSBs, signed)
        var exponent = (raw shr 12) and 0x0F
        if (exponent >= 8) exponent -= 16

        // Extract mantissa (12 LSBs, signed)
        var mantissa = raw and 0x0FFF
        if (mantissa >= 2048) mantissa -= 4096

        // Special values
        if (mantissa == 0x07FF) return Double.POSITIVE_INFINITY  // NaN
        if (mantissa == 0x0800) return Double.NaN                // NRes
        if (mantissa == 0x07FE) return Double.POSITIVE_INFINITY  // +INF
        if (mantissa == -2046) return Double.NEGATIVE_INFINITY   // -INF

        return mantissa * Math.pow(10.0, exponent.toDouble())
    }

    // BLE Scan callback
    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val currentDevices = _discoveredDevices.value.toMutableList()
            if (currentDevices.none { it.address == device.address }) {
                currentDevices.add(device)
                _discoveredDevices.value = currentDevices
                Log.d(TAG, "Discovered: ${device.name ?: "Unknown"} (${device.address})")
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "Scan failed with error code: $errorCode")
            _connectionState.value = BluetoothConnectionState.FAILED
        }
    }

    // GATT callback for connection and data
    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d(TAG, "Connected to GATT server")
                    _connectionState.value = BluetoothConnectionState.CONNECTED
                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d(TAG, "Disconnected from GATT server")
                    _connectionState.value = BluetoothConnectionState.DISCONNECTED
                    _connectedDeviceName.value = null
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "Service discovery failed: $status")
                return
            }

            val glucoseService = gatt.getService(GLUCOSE_SERVICE_UUID)
            if (glucoseService == null) {
                Log.e(TAG, "Glucose service not found on device")
                return
            }

            // Enable notifications for glucose measurement
            val measurementChar = glucoseService.getCharacteristic(GLUCOSE_MEASUREMENT_UUID)
            if (measurementChar != null) {
                gatt.setCharacteristicNotification(measurementChar, true)
                val descriptor = measurementChar.getDescriptor(CCCD_UUID)
                descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                gatt.writeDescriptor(descriptor)
                Log.d(TAG, "Enabled glucose measurement notifications")
            }
        }

        @Deprecated("Deprecated in API 33")
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            if (characteristic.uuid == GLUCOSE_MEASUREMENT_UUID) {
                parseGlucoseMeasurement(characteristic.value, gatt)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun parseGlucoseMeasurement(data: ByteArray, gatt: BluetoothGatt) {
        if (data.isEmpty()) return

        var offset = 0
        val flags = data[offset].toInt() and 0xFF
        offset++

        // Sequence number (uint16)
        val sequenceNumber = (data[offset].toInt() and 0xFF) or ((data[offset + 1].toInt() and 0xFF) shl 8)
        offset += 2

        // Base time (7 bytes: year, month, day, hour, min, sec)
        var timestamp: Date? = null
        val hasBaseTime = (flags and 0x01) != 0
        if (hasBaseTime && offset + 6 < data.size) {
            val year = (data[offset].toInt() and 0xFF) or ((data[offset + 1].toInt() and 0xFF) shl 8)
            val month = data[offset + 2].toInt() and 0xFF
            val day = data[offset + 3].toInt() and 0xFF
            val hour = data[offset + 4].toInt() and 0xFF
            val minute = data[offset + 5].toInt() and 0xFF
            val second = data[offset + 6].toInt() and 0xFF
            offset += 7

            val calendar = Calendar.getInstance()
            calendar.set(year, month - 1, day, hour, minute, second)
            timestamp = calendar.time
        }

        // Time offset (optional)
        if ((flags and 0x02) != 0) {
            offset += 2 // int16 time offset in minutes
        }

        // Concentration (SFLOAT16)
        val hasConcentration = (flags and 0x04) != 0
        if (hasConcentration && offset + 1 < data.size) {
            val glucoseValueMgdl = parseSFLOAT16(data, offset)

            // Validate range (20-600 mg/dL)
            if (glucoseValueMgdl in 20.0..600.0) {
                val glucoseValueMmol = glucoseValueMgdl / MMOL_FACTOR

                val measurement = GlucoseMeasurement(
                    value = glucoseValueMgdl,
                    valueMmol = glucoseValueMmol,
                    sequenceNumber = sequenceNumber,
                    timestamp = timestamp,
                    deviceName = gatt.device?.name
                )

                val currentMeasurements = _measurements.value.toMutableList()
                currentMeasurements.add(0, measurement)
                _measurements.value = currentMeasurements

                Log.d(TAG, "Glucose: ${String.format("%.1f", glucoseValueMmol)} mmol/L (${glucoseValueMgdl.toInt()} mg/dL) seq=$sequenceNumber")
            } else {
                Log.w(TAG, "Invalid glucose value: $glucoseValueMgdl mg/dL")
            }
        }
    }
}
