package com.android.s22present

import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.UEventObserver
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.topjohnwu.superuser.Shell
import org.lsposed.hiddenapibypass.HiddenApiBypass

// Clue's in the name.
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?)
    {
        // On start
        Log.i("S22PresMain", "Hello!")
        Log.i("S22PresMainInit", "Tasks associated with initial setup will be appended with Init")
        HiddenApiBypass.addHiddenApiExemptions("L")
        super.onCreate(savedInstanceState)
        // Create UI
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main))
        {
            v, insets -> val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        findViewById<Button>(R.id.buttonSecondScreen).isEnabled = true
        // Grab the loading bar and reset the progress
        val progress : ProgressBar = findViewById(R.id.progressBar)
        val progresstext : TextView = findViewById(R.id.textViewProgress)
        progress.progress = 0
        progresstext.text = "Starting..."
        // Try to find display [1]
        val displaymanager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        try {
            Log.i("S22PresMainInit", "Looking for second display...")
            val display1 = displaymanager.displays[1]
            // If the display isn't found.
            if(display1 == null)
            {
                // Notify the user.
                progresstext.text = "No second display found!"
                Log.w("S22PresMainInit", "No second display found. Run the commands!")
                Toast.makeText(this, "No second display found. Run the commands!", 2500).show()
            }
            // If the display is found.
            else
            {
                // Push displays and UI elements to globals (referencing globals without a second display causes a crash which is why it's done here)
                Globals.loading = progress
                Globals.loadingtext = progresstext
                Globals.loading.progress = 1
                Globals.loadingtext.text = "Got displays"
                // Disable command button.
                findViewById<Button>(R.id.buttonSecondScreen).isEnabled = false
                // Identify the service
                val serviceintent = Intent(this, ListenerService::class.java)
                // Kill existing service
                Log.i("S22PresMainInit", "I'm killing any existing services to prevent duplicates")
                stopService(serviceintent)
                // Run service and update progress
                Log.i("S22PresMainInit", "Asking Services to Run.")
                startService(serviceintent)
                Globals.loading.progress = 2
                Globals.loadingtext.text = "Starting ListenerService"
                val white =  ContextCompat.getColor(this, R.color.grey)
                Log.i("S22PresMainInit", "White = $white")
            }
        }
        // If a crash occurs whilst looking for a second display.
        catch (e: Exception)
        {
            // Notify user.
            Log.e("S22PresMainInit", "An Exception occurred trying to find the second screen. It is likely because the second screen isn't activated.")
            Toast.makeText(this, "No second display found. Run the commands!", 2500).show()
            progress.progress = 0
            progresstext.text = "No second display found!"
        }
        // When the user pushes the commands button
        findViewById<Button>(R.id.buttonSecondScreen).setOnClickListener{
            Log.i("S22PresMain", "Secondary screen activation requested. Asking for root.")
            // Obtain root access by creating a SU shell.
            Shell.getShell()
            // If root access is granted
            if (Shell.isAppGrantedRoot() == true)
            {
                // Execute shell commands to activate second screen.
                Log.w("S22PresMain", "Root achieved. Running commands... System will soft reboot!")
                Shell.cmd("settings put global hidden_api_policy 1","setprop ro.vendor.gsi.image_running false", "setprop ctl.restart vendor.hwcomposer-2-1").exec()
                // If the system hasn't rebooted after 3 seconds.
                Handler(Looper.getMainLooper()).postDelayed(
                {
                    // Notify user that commands failed.
                    Log.e("S22PresMain", "App is still running. The commands failed! Check device is rooted.")
                    Toast.makeText(this, "Commands failed. Check device is rooted.", 2500).show()
                }, 3000)
            }
            // If no root access is obtained.
            else
            {
                // Notify user that root access wasn't achieved.
                Log.w("S22PressMain", "Check device is rooted.")
                Toast.makeText(this, "Have you got root access?", 2500).show()
            }
        }
        // When user pushes the notification settings button.
        findViewById<Button>(R.id.buttonNotify).setOnClickListener{
            // Navigate the user to the settings menu for notifications permissions.
            startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
        }
        findViewById<Button>(R.id.buttonSetting).setOnClickListener{
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
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

