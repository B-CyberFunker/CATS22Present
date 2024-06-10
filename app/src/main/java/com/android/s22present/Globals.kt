package com.android.s22present

import android.annotation.SuppressLint
import android.os.IBinder
import android.view.Display
import android.view.SurfaceControl
import android.widget.ProgressBar
import android.widget.TextView

// This class stores global variables.
class Globals
{
    @SuppressLint("StaticFieldLeak")
    companion object
    {
        var CurrentNotifs : String? = null
        val sfids = SurfaceControl::class.java.getMethod("getPhysicalDisplayIds").invoke(null) as LongArray
        val token = SurfaceControl::class.java.getMethod("getPhysicalDisplayToken", Long::class.java).invoke(null, sfids[1])
        val displayPowerMethod = SurfaceControl::class.java.getMethod("setDisplayPowerMode", IBinder::class.java, Int::class.java)
        // DisplayManager Display ID storage.
        var display : Display? = null
        var maindisplay : Display? = null
        // Make Presentation content variable (currently allows for multiple presets, full customization will require more work than this).
        var content : Int = R.layout.presentation
        // UI Element storage. Technically doing this can potentially create a memory leak but this doesn't.
        lateinit var datefield : TextView
        lateinit var titlefield : TextView
        lateinit var loading : ProgressBar
        lateinit var loadingtext : TextView
        lateinit var timefield : TextView
        lateinit var contentfield : TextView
    }
}