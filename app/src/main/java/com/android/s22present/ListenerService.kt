package com.android.s22present

import android.app.Service
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.provider.Telephony
import android.telephony.TelephonyManager
import android.util.Log
import com.topjohnwu.superuser.ipc.RootService

// This service listens for various things and tells the ScreenService what it should do.
// These tasks have to be separated into two services since RootServices cannot do things that require context like register Broadcast receivers, but normal services can't do root things like turn the screen off.
class ListenerService : Service()
{
    // Create a connection between the two services.
    var rootservice: Messenger? = null
    val RootConnect = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?)
        {
            // When connected
            Log.d("S22Present-Screen", "Connecting...")
            rootservice = Messenger(service)
            // Check that a messenger was actually received (in some of my testing it'd report connected, but the ScreenService wasn't actually running)
            if(rootservice!=null)
            {
                // Max out the progress bar.
                Globals.loading?.progress = 4
                Log.d("S22Present-Screen", "Done!")

            }
            else
            {
                // If there's no messenger, empty the progress bar and log it.
                Globals.loading?.progress = 0
                Log.d("S22Present-Screen", "Failed!")
            }
        }
        override fun onServiceDisconnected(name: ComponentName?)
        {
            // When the service disconnects, log it.
            Log.d("S22Present-Screen", "Disconnected.")
        }
    }
    // This function starts the ScreenService before it is actually required and binds to it.
    fun startscreenservice(): Unit?
    {
        Log.d("S22Present-Listener", "Let's get the service for the screen running...")
        // Identify ScreenService
        val intent = Intent(this, ScreenService::class.java)
        // Bind to it
        RootService.bind(intent, RootConnect)
        return null
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
    {
        // When this service starts.
        // Update Progress bar and log.
        Globals.loading?.progress = 3
        Log.d("S22Present-Listener", "Hello!")
        // Bind to ScreenService.
        startscreenservice()
        // Identify and show presentation.
        val present = PresentationHandler(this, Globals.display)
        present.show()

        // Create a variable to store requests for ScreenService.
        var request: Message
        // Create a BroadcastReceiver.
        val screenStateReciever = object : BroadcastReceiver()
        {
            // Create a variable that tracks if the screen is currently being turned on. Since I can't use SetPowerMode to turn the screen on for some reason,
            // this allows me to wake the device whilst avoiding an infinite loop. Without this, once the device goes to sleep again, this message would be sent again.
            var processing = 0
            override fun onReceive(context: Context, intent: Intent)
            {
                // When a Broadcast is Received.
                // If the System reports that the main screen is off, a charger is connected, a charger is disconnected, ir a SMS is received.
                if (intent.action == Intent.ACTION_SCREEN_OFF || intent.action == Intent.ACTION_POWER_CONNECTED || intent.action == Intent.ACTION_POWER_DISCONNECTED || intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION)
                {
                    // Log change
                    Log.d("S22Present-Listener", "Action requiring screen on detected")
                    // If another request to turn the screen on doesn't currently exist
                    if(processing==0)
                    {
                        // Update the variable stating one does exist
                        processing = 1
                        // Create a message to turn the screen on and send it to ScreenService.
                        request = Message.obtain(null, 2, 0, 0)
                        rootservice?.send(request)
                        Log.d("S22Present-Listener", "I've asked for the screen to turn on")
                        // After 4 seconds, reset the variable tracking this request.
                        Handler(Looper.getMainLooper()).postDelayed(
                            {
                                processing = 0
                            }, 4000
                        )
                    }
                    // If another request to turn the screen on does currently exist.
                    else
                    {
                        // Do nothing and log it.
                        Log.d("S22Present-Listener", "To avoid creating a loop, I'm not sending any requests.")
                    }
                }
                // If the System reports that the main screen is on.
                if (intent.action == Intent.ACTION_SCREEN_ON)
                {
                    // Log change
                    Log.d("S22Present-Listener", "Activity detected.")
                    // If a request to turn the screen on doesn't currently exist
                    if(processing==0) {
                        // Create a message to turn the screen on and send it to ScreenService.
                        request = Message.obtain(null, 1, 0, 0)
                        rootservice?.send(request)
                        Log.d("S22Present-Listener", "I've asked for the screen to turn off")
                    }
                    // If another request to turn the screen on does currently exist.
                    else
                    {
                        // Do nothing and log it.
                        Log.d("S22Present-Listener", "To avoid creating a loop, I'm not sending any requests.")
                    }
                }
                // If the System reports that the current call status has changed.
                if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED)
                {
                    // Grab the current call status.
                    val state = TelephonyManager.EXTRA_STATE
                    // If the phone is ringing
                    if(state==TelephonyManager.EXTRA_STATE_RINGING)
                    {
                        // Log change
                        Log.d("S22Present-Listener", "Action requiring screen on detected")
                        // If a request to turn the screen on doesn't currently exist
                        if(processing==0)
                        {
                            // Update the variable stating one does exist
                            processing = 1
                            // Create a message to turn the screen on and send it to ScreenService.
                            request = Message.obtain(null, 2, 0, 0)
                            rootservice?.send(request)
                            Log.d("S22Present-Listener", "I've asked for the screen to turn on")
                            // After 4 seconds, reset the variable tracking this request.
                            Handler(Looper.getMainLooper()).postDelayed(
                                {
                                    processing = 0
                                }, 4000
                            )
                        }
                        // If another request to turn the screen on does currently exist.
                        else
                        {
                            // Do nothing and log it.
                            Log.d("S22Present-Listener", "To avoid creating a loop, I'm not sending any requests.")
                        }
                    }
                }
            }
        }
        // Create a filter for the Broadcast Receiver
        val screenStateFilter = IntentFilter()
        // Add various actions to it.
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF)
        screenStateFilter.addAction(Intent.ACTION_SCREEN_ON)
        screenStateFilter.addAction(Intent.ACTION_POWER_CONNECTED)
        screenStateFilter.addAction(Intent.ACTION_POWER_DISCONNECTED)
        screenStateFilter.addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)
        screenStateFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
        // Start the Receiver with the filter we just created.
        registerReceiver(screenStateReciever, screenStateFilter)
        Log.d("S22Present-Listener", "Listening...")

        return START_STICKY
    }
    // Unused.
    override fun onBind(intent: Intent?): IBinder? {
      return null
    }
}





