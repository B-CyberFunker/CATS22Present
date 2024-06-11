package com.android.s22present

import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.topjohnwu.superuser.Shell

// Clue's in the name.
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?)
    {
        // On start
        Log.d("S22Present-Main", "Hello!")
        super.onCreate(savedInstanceState)
        // Create UI
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        // Grab the loading bar and reset the progress
        Globals.loading = findViewById(R.id.progressBar)
        Globals.loading?.progress = 0
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main))
        {
            v, insets -> val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Get the secondary display ID from Display Manager.
        Log.d("S22Present-Main", "Looking for second display...")
        try {
            val displaymanager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
            Globals.display = displaymanager.displays[1]
            Globals.loading?.progress = 1
            // If no second display is found
            if(Globals.display==null)
            {
                // Prompt user to run commands and reset progress
                Log.d("S22Present-Main", "No second display found. Run the commands!")
                Toast.makeText(this, "No second display found. Run the commands!", 2500).show()
                Globals.loading?.progress = 0
            }
            // If second display is found
            else
            {
                // Identify the service
                val serviceintent = Intent(this, ListenerService::class.java)
                Log.d("S22Present-Main", "I'm killing any existing services to prevent duplicates")
                // Kill existing service
                stopService(serviceintent)
                // Run service and update progress
                Log.d("S22Present-Main", "Asking Service to Run.")
                startService(serviceintent)
                Globals.loading?.progress = 2
            }
            // If a crash occurs when trying to get the second display
        } catch(e: ExceptionInInitializerError)
        {
            // Prompt user to run commands and reset progress
            Log.d("S22Present-Main", "An Exception occurred trying to find the second screen. It is likely because the second screen isn't activated.")
            Toast.makeText(this, "No second display found. Run the commands!", 2500).show()
            Globals.loading?.progress = 0
        }
        // When the user pushes the commands button
        findViewById<Button>(R.id.button).setOnClickListener {
            Log.d("S22Present-Main", "Secondary screen activation requested. Asking for root.")
            // Obtain root access by creating a SU shell.
            Shell.getShell()
            // If root access is granted
            if (Shell.isAppGrantedRoot() == true)
            {
                // Execute shell commands to activate second screen.
                Log.d("S22Present-Main", "Root achieved. Running commands... System will soft reboot!")
                Shell.cmd("settings put global hidden_api_policy 1","setprop ro.vendor.gsi.image_running false", "setprop ctl.restart vendor.hwcomposer-2-1").exec()
                // If the system hasn't rebooted after 3 seconds.
                Handler(Looper.getMainLooper()).postDelayed(
                {
                    // Notify user that commands failed.
                    Log.d("S22Present-Main", "App is still running. The commands failed! Check device is rooted.")
                    Toast.makeText(this, "Commands failed. Check device is rooted.", 2500).show()
                }, 3000)
            }
            // If no root access is obtained.
            else
            {
                // Notify user that root access wasn't achieved.
                Log.d("S22Present-Main", "Check device is rooted.")
                Toast.makeText(this, "Have you got root access?", 2500).show()
            }
        }
    }
    override fun onResume()
    {
        super.onResume()
    }

    override fun onPause()
    {
        super.onPause()
    }

    override fun onStop()
    {
        super.onStop()
    }
}
