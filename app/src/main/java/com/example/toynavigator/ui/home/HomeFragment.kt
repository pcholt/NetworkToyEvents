package com.example.toynavigator.ui.home

import android.content.Context
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Bundle
import android.telephony.TelephonyManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
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
            telephonyManager = requireContext().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager,
            wifiManager = requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        )

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        homeViewModel.uiEvents.observe(viewLifecycleOwner, Observer {
            binding.textHome.append(when (it) {
                is DisplayText ->
                    ": ${it.displayString}\n"
                is WifiStrengthChange ->
                    "event\n ${it.extras?.keySet()?.joinToString(":")}\n"
            })
        })
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}