package com.android.s22present

import android.app.Presentation
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import kotlinx.coroutines.delay
import kotlin.concurrent.timer


class Presentservice : Service()
{

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int
    {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show()

        var presentation: Presentation?  = null
        val displaymanager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val display = displaymanager.displays[1]

        presentation = DefaultPresentation(this, display)
        presentation.show()

        return START_STICKY
    }
    override fun onDestroy()
    {
        var presentation: Presentation?  = null
        val displaymanager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val display = displaymanager.displays[1]
        presentation = DefaultPresentation(this, display)
        presentation.hide()
        super.onDestroy()
    }
        override fun onBind(intent: Intent?): IBinder?
    {

        return null
    }
}