package com.homektv.tv.player

import android.media.AudioDeviceInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MicrophoneInputSelectorTest {
    @Test
    fun `prefers usb then wired then bluetooth inputs`() {
        assertTrue(
            MicrophoneInputSelector.priority(AudioDeviceInfo.TYPE_USB_HEADSET) <
                MicrophoneInputSelector.priority(AudioDeviceInfo.TYPE_WIRED_HEADSET),
        )
        assertTrue(
            MicrophoneInputSelector.priority(AudioDeviceInfo.TYPE_WIRED_HEADSET) <
                MicrophoneInputSelector.priority(AudioDeviceInfo.TYPE_BLE_HEADSET),
        )
        assertTrue(
            MicrophoneInputSelector.priority(AudioDeviceInfo.TYPE_BLE_HEADSET) <
                MicrophoneInputSelector.priority(AudioDeviceInfo.TYPE_BLUETOOTH_SCO),
        )
    }

    @Test
    fun `labels supported microphone types`() {
        assertEquals("USB 麦克风", MicrophoneInputSelector.label(AudioDeviceInfo.TYPE_USB_DEVICE))
        assertEquals("有线麦克风", MicrophoneInputSelector.label(AudioDeviceInfo.TYPE_WIRED_HEADSET))
        assertEquals("蓝牙麦克风", MicrophoneInputSelector.label(AudioDeviceInfo.TYPE_BLUETOOTH_SCO))
    }

    @Test
    fun `android 8 supports classic inputs but excludes ble audio`() {
        assertTrue(MicrophoneInputSelector.isSupportedType(AudioDeviceInfo.TYPE_USB_HEADSET, 26))
        assertTrue(MicrophoneInputSelector.isBluetoothType(AudioDeviceInfo.TYPE_BLUETOOTH_SCO, 26))
        assertFalse(MicrophoneInputSelector.isSupportedType(AudioDeviceInfo.TYPE_BLE_HEADSET, 26))
        assertFalse(MicrophoneInputSelector.isBluetoothType(AudioDeviceInfo.TYPE_BLE_HEADSET, 26))
    }

    @Test
    fun `android 12 and newer retain ble microphone support`() {
        assertTrue(MicrophoneInputSelector.isSupportedType(AudioDeviceInfo.TYPE_BLE_HEADSET, 31))
        assertTrue(MicrophoneInputSelector.isBluetoothType(AudioDeviceInfo.TYPE_BLE_HEADSET, 31))
    }

    @Test
    fun `allows vendor default routed input only for verified tv model`() {
        assertTrue(MicrophoneInputSelector.allowsDefaultRoutedInput("Q601F"))
        assertTrue(MicrophoneInputSelector.allowsDefaultRoutedInput("q601f"))
        assertEquals(false, MicrophoneInputSelector.allowsDefaultRoutedInput("generic-tv"))
    }
}
