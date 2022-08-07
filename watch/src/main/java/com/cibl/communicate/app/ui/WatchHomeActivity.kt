package com.cibl.communicate.app.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.wear.ambient.AmbientModeSupport
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
    private var eventId = 1
    companion object {
        const val ACCOUNT_LIST_REQUEST_CODE = 1001
    }
    private lateinit var ambientController: AmbientModeSupport.AmbientController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.addLogAdapter(object : AndroidLogAdapter() {
            override fun isLoggable(priority: Int, tag: String?): Boolean {
                return BuildConfig.DEBUG
            }
        })
        binding = ActivityWatchHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            requestEvent(event1)
        }
        binding.btnCardList.setOnClickListener {
            eventId = 2
            requestEvent(event2)
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
                Log.d("send1", "Message sent successfully")
                val sbTemp = StringBuilder()
                sbTemp.append("\n")
                sbTemp.append(event)
                sbTemp.append(" (Sent to mobile)")
                Log.d("receive1", " $sbTemp")
                binding.txtConnect.visibility = View.GONE
                binding.homeContent.visibility = View.VISIBLE
            } else {
                Log.d("send1", "Message failed.")
            }
        }
    }

    override fun onDataChanged(p0: DataEventBuffer) {
    }

    override fun onMessageReceived(p0: MessageEvent) {
        try {
            Log.d(TAG_MESSAGE_RECEIVED, "onMessageReceived event received")
            val s1 = String(p0.data, StandardCharsets.UTF_8)
            val messageEventPath: String = p0.path

            Log.d(
                TAG_MESSAGE_RECEIVED,
                "onMessageReceived() A message from watch was received:"
                        + p0.requestId
                        + " "
                        + messageEventPath
                        + " "
                        + s1
            )

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

                    Log.d(
                        TAG_MESSAGE_RECEIVED,
                        "Acknowledgement message successfully with payload : $returnPayloadAck"
                    )

                    messageEvent = p0
                    mobileNodeUri = p0.sourceNodeId

                    sendMessageTask.addOnCompleteListener {
                        if (it.isSuccessful) {
                            requestEvent(event1)
                        } else {
                            Log.d(TAG_MESSAGE_RECEIVED, "Message failed.")
                        }
                    }
                } catch (e: Exception) {
                    Log.d(
                        TAG_MESSAGE_RECEIVED,
                        "Handled in sending message back to the sending node"
                    )
                    e.printStackTrace()
                }
            }//emd of if
            else if (messageEventPath.isNotEmpty() && messageEventPath == MESSAGE_ITEM_RECEIVED_PATH) {
                if (event1 == "accounts") {
                    startActivityForResult(
                        Intent(this, WatchMainActivity::class.java)
                            .putExtra("event", s1)
                            .putExtra("eventTitle","Account list")
                            .putExtra("eventId", eventId)
                        , ACCOUNT_LIST_REQUEST_CODE)
                }else {
                    Logger.d(s1)
                }
            }
        } catch (e: Exception) {
            Log.d(TAG_MESSAGE_RECEIVED, "Handled in onMessageReceived")
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

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ACCOUNT_LIST_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val accId = data!!.getIntExtra("event3", -1)
               requestEvent(accId.toString())
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}