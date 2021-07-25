package com.example.toynavigator.ui.home

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.telephony.CellLocation
import android.telephony.PhoneStateListener
import android.telephony.PhoneStateListener.*
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch


/**
 * TODO: Convert signal strength to "bars" for cellular data
 */
private val SignalStrength?.bars: Int
    get() = 1

class HomeViewModel : ViewModel() {

    val data = Channel<UIEvents>()
    val uiEvents = data.receiveAsFlow().asLiveData()

    val wifiStateReceiver by lazy {
        object: BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                viewModelScope.launch {
                    data.send(UIEvents.WifiStrengthChange(wifiManager.connectionInfo.rssi))
                }
            }
        }
    }

    private lateinit var telephonyManager: TelephonyManager
    private lateinit var wifiManager: WifiManager

    fun start(telephonyManager: TelephonyManager, wifiManager: WifiManager) {

        this.telephonyManager = telephonyManager
        this.wifiManager = wifiManager

        telephonyManager.listen(object : PhoneStateListener() {

            override fun onSignalStrengthsChanged(signalStrength: SignalStrength?) {
                super.onSignalStrengthsChanged(signalStrength)
                viewModelScope.launch {
                    data.send(UIEvents.CellularStrengthChange(signalStrength.bars))
                }
            }

            @SuppressLint("MissingPermission")
            override fun onCellLocationChanged(location: CellLocation?) {
                super.onCellLocationChanged(location)
                viewModelScope.launch {
                    data.send(UIEvents.LocationChange(location.toString()))
                }
                location.toString()
            }

        }, LISTEN_SIGNAL_STRENGTHS or
                LISTEN_SIGNAL_STRENGTH or LISTEN_CELL_LOCATION)
    }

}

sealed class UIEvents {
    data class WifiStrengthChange(val rssi: Int) : UIEvents()
    data class LocationChange(val toString: String) : UIEvents()
    data class CellularStrengthChange(val bars: Int) : UIEvents()
}
