package com.android.s22present

import android.annotation.SuppressLint
import android.media.audiofx.Visualizer
import android.os.IBinder
import android.view.SurfaceControl
import android.view.SurfaceView
import android.view.View
import android.view.WindowManagerGlobal
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.chibde.visualizer.BarVisualizer
import com.chibde.visualizer.LineBarVisualizer
import com.chibde.visualizer.SquareBarVisualizer

// This class stores global variables.
class Globals
{
    @SuppressLint("StaticFieldLeak")
    companion object
    {
        val sfids = SurfaceControl::class.java.getMethod("getPhysicalDisplayIds").invoke(null) as LongArray
        val token = SurfaceControl::class.java.getMethod("getPhysicalDisplayToken", Long::class.java).invoke(null, sfids[1])
        // Make Presentation content variable (currently allows for multiple presets, full customization will require more work than this).
        // UI Element storage. Technically doing this can potentially create a memory leak but this doesn't.
        lateinit var datefield : TextView
        lateinit var titlefield : TextView
        lateinit var loading : ProgressBar
        lateinit var loadingtext : TextView
        lateinit var timefield : TextView
        lateinit var contentfield : TextView
        var visual : Int = 0
        lateinit var visualbar: BarVisualizer
        lateinit var visualsquare: SquareBarVisualizer
        var style = "0"
        var font = "0"
    }
}