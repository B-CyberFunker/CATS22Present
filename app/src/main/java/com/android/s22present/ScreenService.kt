package com.android.s22present

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.UEventObserver
import android.util.Log
import android.view.SurfaceControl
import android.view.WindowManagerGlobal
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
        // Grab Display 2 Token
        override fun handleMessage(msg: Message) {
            // If the message
            when (msg.what) {
                3 ->{Runtime.getRuntime().exec("input keyevent KEYCODE_SLEEP"); Log.v("S22PresScreenServ", "Sleep!");
                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            SurfaceControl.setDisplayPowerMode(Globals.token as IBinder?, SurfaceControl.POWER_MODE_OFF)
                        },500)}
                2 ->{Runtime.getRuntime().exec("input keyevent KEYCODE_WAKEUP"); Log.v("S22PresScreenServ", "Wakeup!")}
                1 ->{SurfaceControl.setDisplayPowerMode(Globals.token as IBinder?, SurfaceControl.POWER_MODE_OFF); Log.v("S22PresScreenServ", "Turning off!")}
                // If the message isn't recognised.
                else ->
                    // Log it.
                    {
                    Log.e("S22PresScreenServ", "I wasn't told anything meaningful... ${msg.what}")
                    }
            }
        }
    }
    override fun onBind(intent: Intent): IBinder
    {
        // When service is bound.
        // Log the change and send the messenger back to the ListenerService.
        Log.i("S22PresScreenServInit", "Hello! I've been bound :3")
        mMessenger = Messenger(IncomingHandler(this))
        return mMessenger.binder

    }
}




