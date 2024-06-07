package com.android.s22present

import android.app.Presentation
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Display
import android.widget.TextView
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

// Manages the Presentation and it's contents.
class PresentationHandler(context: Context, display: Display?): Presentation(context,display)
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        // When started
        Log.d("S22Presentathandler", "Presentation start triggered")
        super.onCreate(savedInstanceState)
        // Grab the content variable and display whatever it says should be displayed.
        setContentView(Globals.content)
        // Get todays date and the "local" format (although im in the UK and this displays the month first!)
        val Datefield : TextView = findViewById(R.id.textView2)
        val today = LocalDateTime.now()
        val format = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
        val localtoday = today.format(format)
        // Push the date to the presentation.
        Datefield.text = localtoday
        Log.d("S22Presenthandler", "Presentation displayed")
    }
}
