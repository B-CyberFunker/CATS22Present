package com.android.s22present

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.util.Log
import com.topjohnwu.superuser.ipc.RootService

// A service that performs screen changes based on the requests of ListenerService. This service has root access and limited context abilities.
class ScreenService : RootService()
{
    // Create a way for ListenerService to communicate.
    private lateinit var mMessenger: Messenger
    // Manage incoming messages.
    internal class IncomingHandler(
        context: Context,
    ) : Handler() {
        override fun handleMessage(msg: Message) {
            // If the message
            when (msg.what) {
                // If the message is asking for the screen to be turned on.
                2 ->{
                    // This is the biggest hack in the history of hacks
                    // After 500ms, wake up the device.
                        Handler(Looper.getMainLooper()).postDelayed(
                            {
                             Runtime.getRuntime().exec("input keyevent KEYCODE_WAKEUP")
                                Log.d("S22Present-Screen", "Turning on!")
                            }, 500
                        )
                    // After 3000ms, put the device back to sleep (In most cases the second display remains on a little longer)
                        Handler(Looper.getMainLooper()).postDelayed(
                            {
                                Runtime.getRuntime().exec("input keyevent KEYCODE_SOFT_SLEEP")
                                Log.d("S22Present-Screen", "and off.")
                            }, 3000
                        )
                    }
                // If the message is asking for the screen to be turned off
                1 ->{
                    // Wait one second to make sure the second screen is actually on, and then turn it off.
                      Handler(Looper.getMainLooper()).postDelayed(
                          {
                          Globals.displayPowerMethod.invoke(null,Globals.token,0)
                          Log.d("S22Present-Screen", "Turning off!")
                          },1000)
                }
                // If the message isn't recognised.
                else ->
                    // Log it.
                    {
                    Log.d("S22Present-Screen", "I wasn't told anything meaningful... ${msg.what}")
                    }
            }
        }
    }
    override fun onBind(intent: Intent): IBinder
    {
        // When service is bound.
        // Log the change and send the messenger back to the ListenerService.
        Log.d("S22Present-Screen", "Hello! I've been bound :3")
        mMessenger = Messenger(IncomingHandler(this))
        return mMessenger.binder

    }
}




