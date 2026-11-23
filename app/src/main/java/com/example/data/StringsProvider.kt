package com.example.data

import com.example.model.AppLanguage
import com.example.model.ControllerLayoutMode
import com.example.model.HapticStrength
import com.example.model.OrientationPreference

data class AppCopy(
    val appTitle: String,
    val offlineBadge: String,
    val connectedPrefix: String,
    val connectingLabel: String,
    val notConnectedLabel: String,
    val tapToConnectTv: String,
    val modePro: String,
    val modeRetro: String,
    val modeTvRemote: String,
    val tvRemoteLabel: String,
    val btSheetTitle: String,
    val btSheetSubtitle: String,
    val permissionTitle: String,
    val permissionBody: String,
    val grantPermissionBtn: String,
    val btOffTitle: String,
    val btOffBody: String,
    val turnOnBtBtn: String,
    val hidUnsupportedOs: String,
    val hidUnsupportedHw: String,
    val hidReadyLabel: String,
    val hidRegisteringLabel: String,
    val makeDiscoverableBtn: String,
    val openBtSettingsBtn: String,
    val pairedTvsHeader: String,
    val noPairedTvsEmpty: String,
    val scanNearbyBtn: String,
    val scanningLabel: String,
    val nearbyDevicesHeader: String,
    val noNearbyEmpty: String,
    val connectBtn: String,
    val disconnectBtn: String,
    val pairBtn: String,
    val pairingInProgress: String,
    val connectedChip: String,
    val guideTitle: String,
    val guideStep1: String,
    val guideStep2: String,
    val guideStep3: String,
    val settingsTitle: String,
    val hapticLabel: String,
    val deadzoneLabel: String,
    val keepScreenOnLabel: String,
    val keepScreenOnSub: String,
    val orientationLabel: String,
    val languageLabel: String,
    val liveTesterTitle: String,
    val noActiveInput: String,
    val packetsSentLabel: String,
    val closeBtn: String,
    val tvHomeBtn: String,
    val tvBackBtn: String,
    val tvMuteBtn: String,
    val tvPlayPauseBtn: String,
    val tvVolDownBtn: String,
    val tvVolUpBtn: String
) {
    fun layoutModeName(mode: ControllerLayoutMode, isBn: Boolean): String = when (mode) {
        ControllerLayoutMode.PRO_DUAL_STICK -> if (isBn) "প্রো স্টিক" else "Pro Stick"
        ControllerLayoutMode.RETRO_ARCADE -> if (isBn) "রেট্রো" else "Retro"
        ControllerLayoutMode.TV_REMOTE -> if (isBn) "টিভি রিমোট" else "TV Remote"
    }

    fun hapticName(strength: HapticStrength, isBn: Boolean): String = when (strength) {
        HapticStrength.OFF -> if (isBn) "বন্ধ" else "Off"
        HapticStrength.LIGHT -> if (isBn) "হালকা" else "Light"
        HapticStrength.MEDIUM -> if (isBn) "মাঝারি" else "Medium"
        HapticStrength.STRONG -> if (isBn) "শক্তিশালী" else "Strong"
    }

    fun orientationName(pref: OrientationPreference, isBn: Boolean): String = when (pref) {
        OrientationPreference.AUTO -> if (isBn) "অটো" else "Auto"
        OrientationPreference.LANDSCAPE -> if (isBn) "ল্যান্ডস্কেপ" else "Landscape"
        OrientationPreference.PORTRAIT -> if (isBn) "পোর্ট্রেট" else "Portrait"
    }
}

object StringsProvider {
    private val bengali = AppCopy(
        appTitle = "TV Pad",
        offlineBadge = "অফলাইন • ইন্টারনেট ছাড়াই",
        connectedPrefix = "সংযুক্ত:",
        connectingLabel = "সংযুক্ত হচ্ছে...",
        notConnectedLabel = "টিভি সংযুক্ত নেই",
        tapToConnectTv = "টিভির সাথে ব্লুটুথ সংযোগ করতে ট্যাপ করুন",
        modePro = "প্রো স্টিক",
        modeRetro = "রেট্রো",
        modeTvRemote = "টিভি রিমোট",
        tvRemoteLabel = "টিভি নেভিগেশন ও রিমোট কন্ট্রোল",
        btSheetTitle = "টিভিতে ব্লুটুথ সংযোগ",
        btSheetSubtitle = "কোনো ইন্টারনেট বা ওয়াই-ফাই ছাড়াই সরাসরি ব্লুটুথের মাধ্যমে টিভিতে গেম খেলুন",
        permissionTitle = "ব্লুটুথ অনুমতি প্রয়োজন",
        permissionBody = "টিভির সাথে গেমপ্যাড সংযুক্ত করতে কাছাকাছি ব্লুটুথ ডিভাইস ব্যবহারের অনুমতি দিন।",
        grantPermissionBtn = "অনুমতি দিন",
        btOffTitle = "ব্লুটুথ বন্ধ আছে",
        btOffBody = "টিভিতে গেমপ্যাড চালাতে আপনার ফোনের ব্লুটুথ চালু করুন।",
        turnOnBtBtn = "ব্লুটুথ চালু করুন",
        hidUnsupportedOs = "ব্লুটুথ গেমপ্যাড প্রোফাইল চালানোর জন্য Android 9 বা নতুন ভার্সন প্রয়োজন।",
        hidUnsupportedHw = "এই ডিভাইসে ব্লুটুথ হার্ডওয়্যার পাওয়া যায়নি। নিচে বাটন টেস্ট করতে পারেন।",
        hidReadyLabel = "গেমপ্যাড প্রোফাইল সক্রিয় (HID প্রস্তুত)",
        hidRegisteringLabel = "ব্লুটুথ গেমপ্যাড প্রোফাইল চালু হচ্ছে...",
        makeDiscoverableBtn = "টিভির জন্য দৃশ্যমান করুন",
        openBtSettingsBtn = "ব্লুটুথ সেটিংস",
        pairedTvsHeader = "পেয়ার করা টিভি ও ডিভাইস",
        noPairedTvsEmpty = "এখনো কোনো টিভি পেয়ার করা নেই। নিচে স্ক্যান করুন অথবা ফোন দৃশ্যমান করে টিভি থেকে পেয়ার করুন।",
        scanNearbyBtn = "কাছাকাছি টিভি খুঁজুন",
        scanningLabel = "ডিভাইস খোঁজা হচ্ছে...",
        nearbyDevicesHeader = "পাওয়া গেছে এমন ডিভাইস",
        noNearbyEmpty = "নতুন ডিভাইস পেতে 'খুঁজুন' বাটনে চাপ দিন।",
        connectBtn = "সংযুক্ত করুন",
        disconnectBtn = "বিচ্ছিন্ন করুন",
        pairBtn = "পেয়ার করুন",
        pairingInProgress = "পেয়ার হচ্ছে...",
        connectedChip = "সংযুক্ত",
        guideTitle = "টিভির সাথে যুক্ত করার সহজ নিয়ম (ইন্টারনেট ছাড়া)",
        guideStep1 = "১. টিভির Settings → Remotes & Accessories বা Bluetooth অপশনে যান।",
        guideStep2 = "২. উপরে 'টিভির জন্য দৃশ্যমান করুন' চাপুন, তারপর টিভিতে 'TV Pad' বা ফোনের নাম সিলেক্ট করে Pair করুন।",
        guideStep3 = "৩. পেয়ার হলে উপরের তালিকা থেকে টিভির নামের পাশে 'সংযুক্ত করুন' চাপুন এবং গেম খেলা শুরু করুন!",
        settingsTitle = "কন্ট্রোলার সেটিংস",
        hapticLabel = "ভাইব্রেশন ফিডব্যাক",
        deadzoneLabel = "অ্যানালগ স্টিক ডেডজোন",
        keepScreenOnLabel = "খেলার সময় স্ক্রিন চালু রাখুন",
        keepScreenOnSub = "গেমপ্যাড ব্যবহারের সময় ডিসপ্লে বন্ধ হবে না",
        orientationLabel = "স্ক্রিন ওরিয়েন্টেশন",
        languageLabel = "ভাষা / Language",
        liveTesterTitle = "লাইভ বাটন ও সিগন্যাল টেস্টার",
        noActiveInput = "যেকোনো বাটন বা জয়স্টিক চাপুন...",
        packetsSentLabel = "সিগন্যাল পাঠানো হয়েছে:",
        closeBtn = "বন্ধ করুন",
        tvHomeBtn = "হোম",
        tvBackBtn = "ব্যাক",
        tvMuteBtn = "মিউট",
        tvPlayPauseBtn = "প্লে/পজ",
        tvVolDownBtn = "ভলিউম -",
        tvVolUpBtn = "ভলিউম +"
    )

    private val english = AppCopy(
        appTitle = "TV Pad",
        offlineBadge = "Offline • No Internet",
        connectedPrefix = "Connected:",
        connectingLabel = "Connecting...",
        notConnectedLabel = "No TV Connected",
        tapToConnectTv = "Tap to pair & connect TV via Bluetooth",
        modePro = "Pro Stick",
        modeRetro = "Retro",
        modeTvRemote = "TV Remote",
        tvRemoteLabel = "TV Navigation & Media Control",
        btSheetTitle = "Connect TV via Bluetooth",
        btSheetSubtitle = "Play TV games directly over Bluetooth with zero internet or Wi-Fi required",
        permissionTitle = "Bluetooth Permission Needed",
        permissionBody = "Allow Nearby Devices access so your phone can act as a Bluetooth gamepad for your TV.",
        grantPermissionBtn = "Grant Permission",
        btOffTitle = "Bluetooth Is Off",
        btOffBody = "Turn on Bluetooth on your phone to connect with your TV.",
        turnOnBtBtn = "Turn On Bluetooth",
        hidUnsupportedOs = "Bluetooth HID Gamepad requires Android 9 (API 28) or higher.",
        hidUnsupportedHw = "Bluetooth hardware unavailable on this device. You can still test controls below.",
        hidReadyLabel = "Gamepad HID Profile Active",
        hidRegisteringLabel = "Initializing Bluetooth Gamepad profile...",
        makeDiscoverableBtn = "Make Discoverable for TV",
        openBtSettingsBtn = "Bluetooth Settings",
        pairedTvsHeader = "Paired TVs & Devices",
        noPairedTvsEmpty = "No paired TVs found yet. Scan below or make this phone discoverable and pair from your TV.",
        scanNearbyBtn = "Scan Nearby TVs",
        scanningLabel = "Scanning nearby devices...",
        nearbyDevicesHeader = "Discovered Devices",
        noNearbyEmpty = "Tap Scan to discover nearby Bluetooth TVs.",
        connectBtn = "Connect",
        disconnectBtn = "Disconnect",
        pairBtn = "Pair",
        pairingInProgress = "Pairing...",
        connectedChip = "Connected",
        guideTitle = "Quick Offline TV Setup Guide",
        guideStep1 = "1. Open your TV Settings → Remotes & Accessories or Bluetooth.",
        guideStep2 = "2. Tap 'Make Discoverable for TV' above, then select this phone on your TV to pair.",
        guideStep3 = "3. Once paired, tap 'Connect' next to your TV above and start playing!",
        settingsTitle = "Controller Settings",
        hapticLabel = "Haptic Vibration",
        deadzoneLabel = "Analog Stick Deadzone",
        keepScreenOnLabel = "Keep Screen On",
        keepScreenOnSub = "Prevents screen timeout during gameplay",
        orientationLabel = "Screen Orientation",
        languageLabel = "Language / ভাষা",
        liveTesterTitle = "Live Input & Signal Tester",
        noActiveInput = "Press any button or move stick...",
        packetsSentLabel = "HID Packets Sent:",
        closeBtn = "Close",
        tvHomeBtn = "Home",
        tvBackBtn = "Back",
        tvMuteBtn = "Mute",
        tvPlayPauseBtn = "Play/Pause",
        tvVolDownBtn = "Vol -",
        tvVolUpBtn = "Vol +"
    )

    fun forLanguage(language: AppLanguage): AppCopy = when (language) {
        AppLanguage.BENGALI -> bengali
        AppLanguage.ENGLISH -> english
    }
}
