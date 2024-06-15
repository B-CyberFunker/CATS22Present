package com.android.s22present

import android.animation.ObjectAnimator
import android.app.ActivityTaskManager.RootTaskInfo
import android.app.Presentation
import android.content.Context
import android.graphics.Color
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.ShellCommand
import android.text.Layout
import android.util.Log
import android.view.Display
import android.view.DisplayInfo
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.ShellRootLayer
import android.view.WindowManagerGlobal
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import com.chibde.visualizer.BarVisualizer
import com.chibde.visualizer.LineVisualizer
import com.chibde.visualizer.SquareBarVisualizer
import com.topjohnwu.superuser.Shell
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

// Manages the Presentation and it's contents.
class PresentationHandler(context: Context, display: Display?): Presentation(context,display)
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        // When started
        Log.i("S22PresHandlerInit", "Presentation start triggered")
        Display.FLAG_PRESENTATION
        Display.FLAG_SECURE
        WindowManager.LayoutParams.FLAG_SECURE
        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
        super.onCreate(savedInstanceState)
        // Grab the content variable and display whatever it says should be displayed.
        setContentView(R.layout.presentation)
        // Get todays date and the "local" format (although im in the UK and this displays the month first!)
        val today = LocalDateTime.now()
        val format = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
        val localtoday = today.format(format)
        // Push the date to the presentation.
        Globals.datefield = findViewById(R.id.textView2)
        Globals.datefield.text = localtoday
        Globals.titlefield = findViewById(R.id.textViewTitle)
        Globals.timefield = findViewById(R.id.textClock)
        Globals.contentfield = findViewById(R.id.textViewContent)
        ObjectAnimator.ofFloat(Globals.titlefield, "translationY", 20f).apply { duration = 500; start() }
        ObjectAnimator.ofFloat(Globals.contentfield, "translationY", 20f).apply { duration = 500; start() }
        ObjectAnimator.ofFloat(Globals.datefield, "translationY", 0f).apply { duration = 5; start() }
        ObjectAnimator.ofFloat(Globals.timefield, "translationY", 0f).apply { duration = 5; start() }
        val visual: BarVisualizer = findViewById(R.id.visualizerBar)
        val visualSquare: SquareBarVisualizer = findViewById(R.id.visualizerSquare)
        val digifont = resources.getFont(R.font.digital7)
        val pixelfont = resources.getFont(R.font.dogica)
        Globals.visualbar = findViewById(R.id.visualizerBar)
        Globals.visualsquare = findViewById(R.id.visualizerSquare)

        fun digifontset()
        {
            findViewById<TextView>(R.id.textClock).typeface=digifont
            findViewById<TextView>(R.id.textView2).typeface=digifont
            findViewById<TextView>(R.id.textViewTitle).typeface=digifont
            findViewById<TextView>(R.id.textViewContent).typeface=digifont
            findViewById<TextView>(R.id.textClock).textSize=17f
            findViewById<TextView>(R.id.textView2).textSize=17f
            findViewById<TextView>(R.id.textViewTitle).textSize=14f
            findViewById<TextView>(R.id.textViewContent).textSize=10f
        }
        fun pixelfontset()
        {
            findViewById<TextView>(R.id.textClock).typeface=pixelfont
            findViewById<TextView>(R.id.textView2).typeface=pixelfont
            findViewById<TextView>(R.id.textViewTitle).typeface=pixelfont
            findViewById<TextView>(R.id.textViewContent).typeface=pixelfont
            findViewById<TextView>(R.id.textViewContent).letterSpacing=-0.05f
            findViewById<TextView>(R.id.textViewContent).setLineSpacing(3f, 1f)
            findViewById<TextView>(R.id.textClock).letterSpacing=-0.05f
            findViewById<TextView>(R.id.textView2).letterSpacing=-0.05f
            findViewById<TextView>(R.id.textViewTitle).letterSpacing=-0.05f
            findViewById<TextView>(R.id.textClock).textSize=12f
            findViewById<TextView>(R.id.textView2).textSize=12f
            findViewById<TextView>(R.id.textViewTitle).textSize=10f
            findViewById<TextView>(R.id.textViewContent).textSize=9f
        }
        fun squarevis()
        {
            visualSquare.isEnabled = true
            visualSquare.isInvisible = true
            visualSquare.setPlayer(0)
            visualSquare.setDensity(12F)
            Globals.visual = 2
        }
        fun barvis()
        {
            visual.isEnabled = true
            visual.isInvisible = true
            visual.setPlayer(0)
            visual.setDensity(20F)
            Globals.visual = 1
        }
        if(Globals.style=="1")
        {
            pixelfontset()
            squarevis()
            findViewById<TextView>(R.id.textClock).setTextColor(Color.parseColor("#052745"))
            findViewById<TextView>(R.id.textView2).setTextColor(Color.parseColor("#052745"))
            findViewById<TextView>(R.id.textViewTitle).setTextColor(Color.parseColor("#052745"))
            findViewById<TextView>(R.id.textViewContent).setTextColor(Color.parseColor("#052745"))
            findViewById<View>(R.id.view).setBackgroundColor(Color.parseColor("#093c6c"))
            visualSquare.setColor(Color.parseColor("#10508c"))
        }

        if(Globals.style=="2")
        {
            digifontset()
            squarevis()
            findViewById<TextView>(R.id.textClock).setTextColor(Color.parseColor("#cc0000"))
            findViewById<TextView>(R.id.textView2).setTextColor(Color.parseColor("#cc0000"))
            findViewById<TextView>(R.id.textViewTitle).setTextColor(Color.parseColor("#cc0000"))
            findViewById<TextView>(R.id.textViewContent).setTextColor(Color.parseColor("#cc0000"))
            visualSquare.setColor(Color.parseColor("#790000"))
        }
        if(Globals.style=="0")
        {
            barvis()
            visual.setColor(255255255)
        }
        if(Globals.font=="1")
        {
            digifontset()
        }
        if(Globals.font=="2")
        {
            pixelfontset()
        }
        Log.i("S22PresHandlerInit", "Presentation displayed")
    }
}

