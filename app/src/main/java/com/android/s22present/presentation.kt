package com.android.s22present

import android.app.Presentation
import android.content.Context
import android.os.Bundle
import android.view.Display
import android.view.Window

class DefaultPresentation(context: Context, display: Display): Presentation(context,display)
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.presentation)
    }
}