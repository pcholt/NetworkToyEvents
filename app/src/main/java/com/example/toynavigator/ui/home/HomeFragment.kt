package com.example.toynavigator.ui.home

import android.content.Context
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Bundle
import android.telephony.TelephonyManager
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.toynavigator.R
import com.example.toynavigator.databinding.FragmentHomeBinding
import com.example.toynavigator.ui.home.UIEvents.*

class HomeFragment : Fragment() {

    private val homeViewModel by lazy {
        ViewModelProvider(this).get(HomeViewModel::class.java)
    }

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val wifiManager by lazy {
        requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    private val telephonyManager by lazy {
        requireContext().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        requireContext().registerReceiver(
            homeViewModel.wifiStateReceiver,
            IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION)
        )

        homeViewModel.start(
            telephonyManager = telephonyManager,
            wifiManager = wifiManager
        )

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        homeViewModel.uiEvents.observe(viewLifecycleOwner, Observer {
            binding.textHome.apply {
                append(Html.fromHtml(
                    when (it) {
                        is WifiStrengthChange ->
                            "<font color=#ff0000>strength change <b>${it.rssi}</b></font>"
                        is LocationChange ->
                            "<font color=#00ff00>location change <b>${it.toString}</b></font>"
                        is CellularStrengthChange ->
                            "cellular bars <b>${it.bars}</b>"
                    }
                ))
                append("\n")
            }
        })
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}