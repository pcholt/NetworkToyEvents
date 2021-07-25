package com.example.toynavigator.ui.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch


class HomeViewModel : ViewModel() {

    val wifiStateReceiver by lazy {
        object: BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                viewModelScope.launch {
                    data.send(UIEvents.WifiStrengthChange(p1?.extras))
                }
            }
        }
    }
    val data = Channel<UIEvents>()
    val uiEvents = data.receiveAsFlow().asLiveData()

    private lateinit var telephonyManager: TelephonyManager
    private lateinit var wifiManager: WifiManager

    fun start(telephonyManager: TelephonyManager, wifiManager: WifiManager) {

        this.telephonyManager = telephonyManager
        this.wifiManager = wifiManager

        telephonyManager.listen(object : PhoneStateListener() {
            override fun onSignalStrengthChanged(asu: Int) {
                super.onSignalStrengthChanged(asu)
                viewModelScope.launch {
                    data.send(UIEvents.DisplayText("++ $asu"))
                }
            }
            override fun onSignalStrengthsChanged(signalStrength: SignalStrength?) {
                super.onSignalStrengthsChanged(signalStrength)
                viewModelScope.launch {
                    data.send(UIEvents.DisplayText("+ $signalStrength"))
                }
            }
        }, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS or
                PhoneStateListener.LISTEN_SIGNAL_STRENGTH)
    }

}

sealed class UIEvents {
    data class DisplayText(val displayString: String) : UIEvents()
    data class WifiStrengthChange(val extras: Bundle?) : UIEvents()
}
