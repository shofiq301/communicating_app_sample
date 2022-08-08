package com.cibl.communicate.app.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.wear.ambient.AmbientModeSupport
import com.cibl.communicate.app.R
import com.cibl.communicate.app.databinding.ActivityWatchHomeBinding
import com.google.android.gms.wearable.*
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.BuildConfig
import com.orhanobut.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import java.nio.charset.StandardCharsets


class WatchHomeActivity : AppCompatActivity(), CoroutineScope by MainScope(),
    AmbientModeSupport.AmbientCallbackProvider,
    DataClient.OnDataChangedListener,
    MessageClient.OnMessageReceivedListener,
    CapabilityClient.OnCapabilityChangedListener {


    private lateinit var binding: ActivityWatchHomeBinding

    private var activityContext: Context? = null

    private val TAG_MESSAGE_RECEIVED = "receive1"
    private val APP_OPEN_WEARABLE_PAYLOAD_PATH = "/APP_OPEN_WEARABLE_PAYLOAD"

    private var mobileDeviceConnected: Boolean = false


    // Payload string items
    private val wearableAppCheckPayloadReturnACK = "AppOpenWearableACK"

    private val MESSAGE_ITEM_RECEIVED_PATH: String = "/message-item-received"


    private var messageEvent: MessageEvent? = null
    private var mobileNodeUri: String? = null
    private var event1 = "accounts"
    private var event2 = "cards"
    private var currentEvent = ""
    private var eventId = 1
    companion object {
        const val ACCOUNT_LIST_REQUEST_CODE = 1001
    }
    private lateinit var ambientController: AmbientModeSupport.AmbientController

    private var activityLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback {
            val data = it.data
            when (it.resultCode) {
                ACCOUNT_LIST_REQUEST_CODE -> {
                    val accId = it.data!!.getIntExtra("event3", -1)
                    eventId = 2
                    currentEvent = accId.toString()
                    requestEvent(currentEvent)
                }
            }
        }
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.addLogAdapter(object : AndroidLogAdapter() {
            override fun isLoggable(priority: Int, tag: String?): Boolean {
                return BuildConfig.DEBUG
            }
        })
        binding = DataBindingUtil.setContentView(this, R.layout.activity_watch_home)

        activityContext = this

        // Enables Always-on
        ambientController = AmbientModeSupport.attach(this)

        if (!mobileDeviceConnected) {
            binding.txtConnect.visibility = View.VISIBLE
            binding.homeContent.visibility = View.GONE
        }else {
            requestEvent("")
        }


        binding.btnAcList.setOnClickListener {
            eventId = 1
            currentEvent = event1
            requestEvent(currentEvent)
        }
        binding.btnCardList.setOnClickListener {
            eventId = 2
            currentEvent = event2
            requestEvent(currentEvent)
        }
    }

    private fun requestEvent(event: String) {
        val nodeId: String = messageEvent?.sourceNodeId!!
        // Set the data of the message to be the bytes of the Uri.
        val payload: ByteArray =
            event.toByteArray()

        // Send the rpc
        // Instantiates clients without member variables, as clients are inexpensive to
        // create. (They are cached and shared between GoogleApi instances.)
        val sendMessageTask =
            Wearable.getMessageClient(activityContext!!)
                .sendMessage(nodeId, MESSAGE_ITEM_RECEIVED_PATH, payload)
        sendMessageTask.addOnCompleteListener {
            if (it.isSuccessful) {
                binding.txtConnect.visibility = View.GONE
                binding.homeContent.visibility = View.VISIBLE
            } else {
                Logger.d("Message failed.")
            }
        }
    }

    override fun onDataChanged(p0: DataEventBuffer) {
    }

    override fun onMessageReceived(p0: MessageEvent) {
        try {
            val s1 = String(p0.data, StandardCharsets.UTF_8)
            val messageEventPath: String = p0.path

            //Send back a message back to the source node
            //This acknowledges that the receiver activity is open
            if (messageEventPath.isNotEmpty() && messageEventPath == APP_OPEN_WEARABLE_PAYLOAD_PATH) {
                try {
                    // Get the node id of the node that created the data item from the host portion of
                    // the uri.
                    val nodeId: String = p0.sourceNodeId.toString()
                    // Set the data of the message to be the bytes of the Uri.
                    val returnPayloadAck = wearableAppCheckPayloadReturnACK
                    val payload: ByteArray = returnPayloadAck.toByteArray()

                    // Send the rpc
                    // Instantiates clients without member variables, as clients are inexpensive to
                    // create. (They are cached and shared between GoogleApi instances.)
                    val sendMessageTask =
                        Wearable.getMessageClient(activityContext!!)
                            .sendMessage(nodeId, APP_OPEN_WEARABLE_PAYLOAD_PATH, payload)



                    messageEvent = p0
                    mobileNodeUri = p0.sourceNodeId

                    sendMessageTask.addOnCompleteListener {
                        if (it.isSuccessful) {
                            requestEvent(event1)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Logger.d(
                        "Handled in sending message back to the sending node"
                    )
                }
            }//emd of if
            else if (messageEventPath.isNotEmpty() && messageEventPath == MESSAGE_ITEM_RECEIVED_PATH) {
                val intent: Intent
                if (currentEvent == "accounts") {
                   intent =  Intent(this, WatchMainActivity::class.java)
                        .putExtra("event", s1)
                        .putExtra("eventTitle","Account list")
                        .putExtra("eventId", eventId)
                    currentEvent = ""
                   activityLauncher.launch(intent)
                }else {
                    Toast.makeText(this, s1, Toast.LENGTH_SHORT).show()
                    intent =  Intent(this, AccountDetailsActivity::class.java)
                        .putExtra("details", s1)
                    currentEvent = ""
                    activityLauncher.launch(intent)
                }
            }
        } catch (e: Exception) {
            Logger.d("Handled in onMessageReceived")
            e.printStackTrace()
        }
    }



    override fun onCapabilityChanged(p0: CapabilityInfo) {
    }

    override fun onPause() {
        super.onPause()
        try {
            Wearable.getDataClient(activityContext!!).removeListener(this)
            Wearable.getMessageClient(activityContext!!).removeListener(this)
            Wearable.getCapabilityClient(activityContext!!).removeListener(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onResume() {
        super.onResume()
        try {
            Wearable.getDataClient(activityContext!!).addListener(this)
            Wearable.getMessageClient(activityContext!!).addListener(this)
            Wearable.getCapabilityClient(activityContext!!)
                .addListener(this, Uri.parse("wear://"), CapabilityClient.FILTER_REACHABLE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getAmbientCallback(): AmbientModeSupport.AmbientCallback = MyAmbientCallback()

    private inner class MyAmbientCallback : AmbientModeSupport.AmbientCallback() {
        override fun onEnterAmbient(ambientDetails: Bundle) {
            super.onEnterAmbient(ambientDetails)
        }

        override fun onUpdateAmbient() {
            super.onUpdateAmbient()
        }

        override fun onExitAmbient() {
            super.onExitAmbient()
        }
    }
}