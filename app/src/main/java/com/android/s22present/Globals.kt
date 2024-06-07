package com.android.s22present

import android.annotation.SuppressLint
import android.os.IBinder
import android.view.Display
import android.view.SurfaceControl
import android.widget.ProgressBar

// This class stores global variables.
class Globals
{
    companion object
    {
        // Get Surface Flinger IDs
        val sfids = SurfaceControl::class.java.getMethod("getPhysicalDisplayIds").invoke(null) as LongArray
        // Get a token for both displays (Main display unused)
        val token = SurfaceControl::class.java.getMethod("getPhysicalDisplayToken", Long::class.java).invoke(null, Globals.sfids[1])
        val tokenmain = SurfaceControl::class.java.getMethod("getPhysicalDisplayToken", Long::class.java).invoke(null, Globals.sfids[0])
        // Get a method for toggling display power.
        val displayPowerMethod = SurfaceControl::class.java.getMethod("setDisplayPowerMode", IBinder::class.java, Int::class.java)

        // DisplayManager Display ID storage.
        var display : Display? = null
        // Make Presentation content variable (currently allows for multiple presets, full customization will require more work than this).
        var content : Int = R.layout.presentation

        // Progress bar storage. Storing a progress bar here isn't best practice but I'm not smart enough to come up with another way of updating it from the Service.
        @SuppressLint("StaticFieldLeak")
        var loading : ProgressBar? = null
    }
}