package com.example.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.model.BluetoothTvDevice
import com.example.model.BondStatus
import com.example.model.DeviceConnectionState
import com.example.model.GamepadInputState
import com.example.model.HidRegistrationState
import com.example.model.TvRemoteKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class BluetoothHidController(private val context: Context) {

    companion object {
        private const val TAG = "BluetoothHidController"
    }

    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private val btExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    private var hidDeviceProxy: BluetoothHidDevice? = null
    private var activeHostDevice: BluetoothDevice? = null
    private var latestGamepadReport: ByteArray = GamepadHidDescriptor.buildGamepadReport(GamepadInputState())
    private var isReceiverRegistered = false
    private var isAppRegistered = false

    private val _hidState = MutableStateFlow(HidRegistrationState.REGISTERING)
    val hidState: StateFlow<HidRegistrationState> = _hidState.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BluetoothTvDevice>>(emptyList())
    val pairedDevices: StateFlow<List<BluetoothTvDevice>> = _pairedDevices.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<BluetoothTvDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<BluetoothTvDevice>> = _discoveredDevices.asStateFlow()

    private val _connectedDevice = MutableStateFlow<BluetoothTvDevice?>(null)
    val connectedDevice: StateFlow<BluetoothTvDevice?> = _connectedDevice.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _packetsSent = MutableStateFlow(0L)
    val packetsSent: StateFlow<Long> = _packetsSent.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val rawDiscoveredMap = LinkedHashMap<String, BluetoothDevice>()

    fun requiredRuntimePermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE
            )
        } else {
            emptyArray()
        }
    }

    fun hasConnectPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun hasScanPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun initialize() {
        if (bluetoothAdapter == null) {
            _hidState.value = HidRegistrationState.UNSUPPORTED_HARDWARE
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            _hidState.value = HidRegistrationState.UNSUPPORTED_OS
            return
        }
        registerBluetoothReceiver()
        refreshStateAndRegisterProfile()
    }

    fun refreshStateAndRegisterProfile() {
        val adapter = bluetoothAdapter ?: run {
            _hidState.value = HidRegistrationState.UNSUPPORTED_HARDWARE
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            _hidState.value = HidRegistrationState.UNSUPPORTED_OS
            return
        }
        if (!hasConnectPermission()) {
            _hidState.value = HidRegistrationState.PERMISSION_REQUIRED
            return
        }
        if (!adapter.isEnabled) {
            _hidState.value = HidRegistrationState.BLUETOOTH_DISABLED
            _pairedDevices.value = emptyList()
            _connectedDevice.value = null
            activeHostDevice = null
            return
        }

        refreshPairedDevices()

        if (hidDeviceProxy == null) {
            _hidState.value = HidRegistrationState.REGISTERING
            try {
                adapter.getProfileProxy(context, profileListener, BluetoothProfile.HID_DEVICE)
            } catch (se: SecurityException) {
                Log.w(TAG, "Missing permission for HID profile proxy", se)
                _hidState.value = HidRegistrationState.PERMISSION_REQUIRED
            }
        } else if (!isAppRegistered) {
            registerHidApp(hidDeviceProxy!!)
        } else {
            _hidState.value = HidRegistrationState.REGISTERED
        }
    }

    @SuppressLint("MissingPermission")
    fun refreshPairedDevices() {
        val adapter = bluetoothAdapter ?: return
        if (!hasConnectPermission() || !adapter.isEnabled) {
            _pairedDevices.value = emptyList()
            return
        }
        try {
            val bonded = adapter.bondedDevices.orEmpty()
            val hid = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) hidDeviceProxy else null
            val list = bonded.map { device ->
                val connState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && hid != null) {
                    mapConnectionState(hid.getConnectionState(device))
                } else if (activeHostDevice?.address == device.address) {
                    DeviceConnectionState.CONNECTED
                } else {
                    DeviceConnectionState.DISCONNECTED
                }
                toTvDevice(device, connState)
            }.sortedWith(
                compareByDescending<BluetoothTvDevice> { it.connectionState == DeviceConnectionState.CONNECTED }
                    .thenByDescending { it.isTvOrMedia }
                    .thenBy { it.name.lowercase() }
            )
            _pairedDevices.value = list
            _connectedDevice.value = list.firstOrNull { it.connectionState == DeviceConnectionState.CONNECTED }
        } catch (se: SecurityException) {
            Log.w(TAG, "SecurityException reading bondedDevices", se)
        }
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(address: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return
        val adapter = bluetoothAdapter ?: return
        val hid = hidDeviceProxy
        if (!hasConnectPermission()) {
            _hidState.value = HidRegistrationState.PERMISSION_REQUIRED
            return
        }
        if (hid == null || !isAppRegistered) {
            _errorMessage.value = "HID profile not registered yet"
            refreshStateAndRegisterProfile()
            return
        }

        val target = try {
            adapter.getRemoteDevice(address)
        } catch (_: IllegalArgumentException) {
            null
        } ?: return

        try {
            if (adapter.isDiscovering && hasScanPermission()) {
                adapter.cancelDiscovery()
            }
            // Disconnect current device if different
            activeHostDevice?.let { current ->
                if (current.address != target.address) {
                    hid.disconnect(current)
                }
            }
            updateDeviceConnectionState(address, DeviceConnectionState.CONNECTING)
            val initiated = hid.connect(target)
            if (!initiated) {
                updateDeviceConnectionState(address, DeviceConnectionState.DISCONNECTED)
                _errorMessage.value = "Could not initiate HID connection"
            }
        } catch (se: SecurityException) {
            Log.w(TAG, "SecurityException connecting to device", se)
            _hidState.value = HidRegistrationState.PERMISSION_REQUIRED
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnectCurrent() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return
        val hid = hidDeviceProxy ?: return
        if (!hasConnectPermission()) return
        val device = activeHostDevice ?: _connectedDevice.value?.let {
            runCatching { bluetoothAdapter?.getRemoteDevice(it.address) }.getOrNull()
        } ?: return

        try {
            hid.disconnect(device)
            activeHostDevice = null
            _connectedDevice.value = null
            refreshPairedDevices()
        } catch (se: SecurityException) {
            Log.w(TAG, "SecurityException disconnecting", se)
        }
    }

    @SuppressLint("MissingPermission")
    fun startDiscovery() {
        val adapter = bluetoothAdapter ?: return
        if (!hasConnectPermission() || !hasScanPermission()) {
            _hidState.value = HidRegistrationState.PERMISSION_REQUIRED
            return
        }
        if (!adapter.isEnabled) {
            _hidState.value = HidRegistrationState.BLUETOOTH_DISABLED
            return
        }
        try {
            if (adapter.isDiscovering) {
                adapter.cancelDiscovery()
            }
            rawDiscoveredMap.clear()
            _discoveredDevices.value = emptyList()
            val started = adapter.startDiscovery()
            _isScanning.value = started
        } catch (se: SecurityException) {
            Log.w(TAG, "SecurityException starting discovery", se)
        }
    }

    @SuppressLint("MissingPermission")
    fun stopDiscovery() {
        val adapter = bluetoothAdapter ?: return
        if (!hasScanPermission()) return
        try {
            if (adapter.isDiscovering) {
                adapter.cancelDiscovery()
            }
            _isScanning.value = false
        } catch (se: SecurityException) {
            Log.w(TAG, "SecurityException stopping discovery", se)
        }
    }

    @SuppressLint("MissingPermission")
    fun pairWithDevice(address: String) {
        val adapter = bluetoothAdapter ?: return
        if (!hasConnectPermission()) {
            _hidState.value = HidRegistrationState.PERMISSION_REQUIRED
            return
        }
        val device = rawDiscoveredMap[address] ?: runCatching {
            adapter.getRemoteDevice(address)
        }.getOrNull() ?: return

        try {
            if (hasScanPermission() && adapter.isDiscovering) {
                adapter.cancelDiscovery()
            }
            device.createBond()
        } catch (se: SecurityException) {
            Log.w(TAG, "SecurityException pairing device", se)
        }
    }

    fun sendGamepadState(state: GamepadInputState) {
        val report = GamepadHidDescriptor.buildGamepadReport(state)
        latestGamepadReport = report
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return
        val hid = hidDeviceProxy ?: return
        val host = activeHostDevice ?: return
        if (!isAppRegistered || !hasConnectPermission()) return

        btExecutor.execute {
            try {
                @SuppressLint("MissingPermission")
                val sent = hid.sendReport(host, GamepadHidDescriptor.REPORT_ID_GAMEPAD, report)
                if (sent) {
                    _packetsSent.value = _packetsSent.value + 1
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to send gamepad HID report", e)
            }
        }
    }

    fun sendConsumerKeyTap(key: TvRemoteKey) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return
        val hid = hidDeviceProxy ?: return
        val host = activeHostDevice ?: return
        if (!isAppRegistered || !hasConnectPermission()) return

        val pressReport = GamepadHidDescriptor.buildConsumerReport(key.consumerUsage)
        val releaseReport = GamepadHidDescriptor.buildConsumerReport(0)

        btExecutor.execute {
            try {
                @SuppressLint("MissingPermission")
                val pressed = hid.sendReport(host, GamepadHidDescriptor.REPORT_ID_CONSUMER, pressReport)
                Thread.sleep(35)
                @SuppressLint("MissingPermission")
                val released = hid.sendReport(host, GamepadHidDescriptor.REPORT_ID_CONSUMER, releaseReport)
                if (pressed || released) {
                    _packetsSent.value = _packetsSent.value + 1
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to send consumer HID report", e)
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun release() {
        stopDiscovery()
        if (isReceiverRegistered) {
            runCatching { context.unregisterReceiver(btReceiver) }
            isReceiverRegistered = false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            hidDeviceProxy?.let { hid ->
                try {
                    if (hasConnectPermission()) {
                        activeHostDevice?.let { hid.disconnect(it) }
                        if (isAppRegistered) {
                            hid.unregisterApp()
                        }
                    }
                    bluetoothAdapter?.closeProfileProxy(BluetoothProfile.HID_DEVICE, hid)
                } catch (e: Exception) {
                    Log.w(TAG, "Error releasing HID proxy", e)
                }
            }
        }
        hidDeviceProxy = null
        activeHostDevice = null
        isAppRegistered = false
        btExecutor.shutdownNow()
    }

    private val profileListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && profile == BluetoothProfile.HID_DEVICE) {
                val hid = proxy as? BluetoothHidDevice ?: return
                hidDeviceProxy = hid
                registerHidApp(hid)
                refreshPairedDevices()
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                hidDeviceProxy = null
                isAppRegistered = false
                activeHostDevice = null
                _connectedDevice.value = null
                refreshPairedDevices()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("MissingPermission")
    private fun registerHidApp(hid: BluetoothHidDevice) {
        if (!hasConnectPermission()) {
            _hidState.value = HidRegistrationState.PERMISSION_REQUIRED
            return
        }
        val sdp = BluetoothHidDeviceAppSdpSettings(
            "TV Pad",
            "Offline Bluetooth TV Gamepad",
            "TV Pad",
            BluetoothHidDevice.SUBCLASS2_GAMEPAD,
            GamepadHidDescriptor.DESCRIPTOR
        )
        try {
            val ok = hid.registerApp(sdp, null, null, btExecutor, hidCallback)
            if (!ok) {
                _hidState.value = HidRegistrationState.ERROR
            }
        } catch (se: SecurityException) {
            Log.w(TAG, "SecurityException registering HID app", se)
            _hidState.value = HidRegistrationState.PERMISSION_REQUIRED
        }
    }

    @get:RequiresApi(Build.VERSION_CODES.P)
    private val hidCallback by lazy {
        object : BluetoothHidDevice.Callback() {
            override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
                isAppRegistered = registered
                _hidState.value = if (registered) {
                    HidRegistrationState.REGISTERED
                } else {
                    HidRegistrationState.ERROR
                }
                if (registered && pluggedDevice != null) {
                    activeHostDevice = pluggedDevice
                    refreshPairedDevices()
                }
            }

            @SuppressLint("MissingPermission")
            override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
                when (state) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        activeHostDevice = device
                        _errorMessage.value = null
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        if (activeHostDevice?.address == device?.address) {
                            activeHostDevice = null
                        }
                    }
                }
                refreshPairedDevices()
            }

            @SuppressLint("MissingPermission")
            override fun onGetReport(
                device: BluetoothDevice?,
                type: Byte,
                id: Byte,
                bufferSize: Int
            ) {
                val hid = hidDeviceProxy ?: return
                if (device == null || !hasConnectPermission()) return
                if (type == BluetoothHidDevice.REPORT_TYPE_INPUT) {
                    val payload = if (id.toInt() == GamepadHidDescriptor.REPORT_ID_CONSUMER) {
                        GamepadHidDescriptor.buildConsumerReport(0)
                    } else {
                        latestGamepadReport
                    }
                    hid.replyReport(device, type, id, payload)
                }
            }

            override fun onSetProtocol(device: BluetoothDevice?, protocol: Byte) {
                // Report mode accepted by default
            }
        }
    }

    private fun registerBluetoothReceiver() {
        if (isReceiverRegistered) return
        val filter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                addAction(BluetoothHidDevice.ACTION_CONNECTION_STATE_CHANGED)
            }
        }
        ContextCompat.registerReceiver(
            context,
            btReceiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )
        isReceiverRegistered = true
    }

    private val btReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(ctx: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    refreshStateAndRegisterProfile()
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    _isScanning.value = true
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _isScanning.value = false
                }
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    if (device != null && hasConnectPermission()) {
                        try {
                            if (device.bondState != BluetoothDevice.BOND_BONDED) {
                                rawDiscoveredMap[device.address] = device
                                _discoveredDevices.value = rawDiscoveredMap.values.map {
                                    toTvDevice(it, DeviceConnectionState.DISCONNECTED)
                                }.sortedByDescending { it.isTvOrMedia }
                            }
                        } catch (_: SecurityException) {
                        }
                    }
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    refreshPairedDevices()
                    val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    if (device != null && hasConnectPermission()) {
                        try {
                            if (device.bondState == BluetoothDevice.BOND_BONDED) {
                                rawDiscoveredMap.remove(device.address)
                                _discoveredDevices.value = rawDiscoveredMap.values.map {
                                    toTvDevice(it, DeviceConnectionState.DISCONNECTED)
                                }
                            }
                        } catch (_: SecurityException) {
                        }
                    }
                }
                BluetoothHidDevice.ACTION_CONNECTION_STATE_CHANGED -> {
                    refreshPairedDevices()
                }
            }
        }
    }

    private fun updateDeviceConnectionState(address: String, state: DeviceConnectionState) {
        _pairedDevices.value = _pairedDevices.value.map {
            if (it.address == address) it.copy(connectionState = state) else it
        }
    }

    @SuppressLint("MissingPermission")
    private fun toTvDevice(device: BluetoothDevice, connectionState: DeviceConnectionState): BluetoothTvDevice {
        val devName = runCatching { device.name }.getOrNull()?.takeIf { it.isNotBlank() } ?: "Bluetooth Device"
        val majorClass = runCatching { device.bluetoothClass?.majorDeviceClass }.getOrNull()
        val isTvOrMedia = majorClass == BluetoothClass.Device.Major.AUDIO_VIDEO ||
            majorClass == BluetoothClass.Device.Major.COMPUTER ||
            devName.contains("TV", ignoreCase = true) ||
            devName.contains("Bravia", ignoreCase = true) ||
            devName.contains("Fire", ignoreCase = true) ||
            devName.contains("Shield", ignoreCase = true) ||
            devName.contains("Box", ignoreCase = true)

        val bond = when (runCatching { device.bondState }.getOrNull()) {
            BluetoothDevice.BOND_BONDED -> BondStatus.BONDED
            BluetoothDevice.BOND_BONDING -> BondStatus.BONDING
            else -> BondStatus.NONE
        }
        return BluetoothTvDevice(
            name = devName,
            address = device.address,
            isTvOrMedia = isTvOrMedia,
            bondStatus = bond,
            connectionState = connectionState
        )
    }

    private fun mapConnectionState(state: Int): DeviceConnectionState = when (state) {
        BluetoothProfile.STATE_CONNECTED -> DeviceConnectionState.CONNECTED
        BluetoothProfile.STATE_CONNECTING -> DeviceConnectionState.CONNECTING
        else -> DeviceConnectionState.DISCONNECTED
    }
}
