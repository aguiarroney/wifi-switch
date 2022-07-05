package com.example.wifi_poc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.net.wifi.WifiNetworkSuggestion
import android.os.Bundle
import android.provider.Settings.ACTION_WIFI_ADD_NETWORKS
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    private val list = ArrayList<WifiNetworkSuggestion>()
    private lateinit var wifiManager: WifiManager

    private lateinit var connectivityManager:ConnectivityManager

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onUnavailable() {
            super.onUnavailable()
        }

        override fun onLosing(network: Network, maxMsToLive: Int) {
            super.onLosing(network, maxMsToLive)
            wifiManager.reconnect()
        }

        override fun onAvailable(network: Network) {
            super.onAvailable(network)
//            connectivityManager.bindProcessToNetwork(network)
        }

        override fun onLost(network: Network) {
            super.onLost(network)

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        wifiManager = this.getSystemService(Context.WIFI_SERVICE) as WifiManager
        connectivityManager = (getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?)!!

        val button: Button = findViewById(R.id.btn_remove_suggestions)
        val button2: Button = findViewById(R.id.btn_disconnect)

        button.setOnClickListener {
            wifiManager.removeNetworkSuggestions(list)
        }

        button2.setOnClickListener {
            wifiManager.disconnect()
        }

        suggestion(this, "Rede", "my-strong-password")

    }

    private fun suggestion(context: Context, networkName: String, networkPassword: String) {

        val suggestion = WifiNetworkSuggestion.Builder()
            .setSsid(networkName)
            .setWpa2Passphrase(networkPassword)
            .setIsAppInteractionRequired(true)
            .build()


        list.add(suggestion)
        val result = wifiManager.addNetworkSuggestions(list)

        if (result == WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS)
            Log.d("AppLog", "success")
        else
            Log.d("AppLog", "failed")


        val intentFilter = IntentFilter(WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION);

        val broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                println("!!!")
                if (!intent.action.equals(WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION)) {
                    return
                }
                // do post connect processing here
                println("!!!")
            }
        }
        context.registerReceiver(broadcastReceiver, intentFilter);

    }

    override fun onDestroy() {
        super.onDestroy()
        connectivityManager?.unregisterNetworkCallback(networkCallback)
    }

    private fun specifier() {
        val specifier = WifiNetworkSpecifier.Builder()
            .setSsid("Rede")
            .setWpa2Passphrase("my-strong-password")
            .build()

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .setNetworkSpecifier(specifier)
            .build()

        connectivityManager.requestNetwork(request, networkCallback);
    }
}