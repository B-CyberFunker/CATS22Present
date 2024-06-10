package com.android.s22present

import android.animation.ObjectAnimator
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.telephony.TelephonyManager
import android.util.Log
import android.view.Display
import com.topjohnwu.superuser.ipc.RootService


// This service listens for various things and tells the ScreenService what it should do.
// These tasks have to be separated into two services since RootServices cannot do things that require context like register Broadcast receivers, but normal services can't do root things like turn the screen off.
class ListenerService : Service()
{
    // Create a connection between the two services.
    var rootservice: Messenger? = null
    val RootConnect = object : ServiceConnection
    {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?)
        {
            Globals.loading.progress = 5
            Globals.loadingtext.text = "ListenerService Bound"
            // When connected
            Log.d("S22PresScreenServInit", "Connecting...")
            rootservice = Messenger(service)
            // Check that a messenger was actually received (in some of my testing it'd report connected, but the ScreenService wasn't actually running)
            if(rootservice!=null)
            {
          //      Handler(Looper.getMainLooper()).postDelayed(
           //         {
                        Globals.loading.progress = 6
                        Globals.loadingtext.text = "Done!"
                        Log.d("S22PresScreenServInit", "Done!")
              //      },100)
            } else {
                // If there's no messenger, empty the progress bar and log it.
                Globals.loading.progress = 0
                Globals.loadingtext.text = "Couldn't connect to ScreenService!"
                Log.d("S22PresScreenServInit", "Failed!")
            }
        }
        override fun onServiceDisconnected(name: ComponentName?)
        {
            // When the service disconnects, log it.
            Log.d("S22PresScreenServInit", "Disconnected.")
        }
    }
    // This function starts the ScreenService before it is actually required and binds to it.
    fun startscreenservice(): Unit?
    {
        Globals.loading.progress = 4
        Globals.loadingtext.text = "Starting ScreenService..."
        Log.d("S22PresListServInit", "Let's get the service for the screen running...")
        // Identify ScreenService
        val intent = Intent(this, ScreenService::class.java)
        // Bind to it
        RootService.bind(intent, RootConnect)
        return null
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
    {
        // Identify and show presentation.
        val present = PresentationHandler(this, Globals.display)
        present.show()
        // When this service starts.
        // Update Progress bar and log.
        Globals.loading.progress = 3
        Globals.loadingtext.text = "ListenerService Started!"
        Log.d("S22PresListServInit", "Hello!")
        // Bind to ScreenService.
        startscreenservice()
        // Create a variable to store requests for ScreenService.
        var request: Message
        // Create a BroadcastReceiver.
        val screenStateReciever = object : BroadcastReceiver()
        {
            var processing = 0
            var notif = 0
            // Create a variable that tracks if the screen is currently being turned on. Since I can't use SetPowerMode to turn the screen on for some reason,
            // this allows me to wake the device whilst avoiding an infinite loop. Without this, once the device goes to sleep again, this message would be sent again.
            override fun onReceive(context: Context, intent: Intent)
            {
                // When a Broadcast is Received.
                // If the System reports that the main screen is off, a charger is connected, a charger is disconnected, ir a SMS is received.
                if (intent.action == Intent.ACTION_SCREEN_OFF || intent.action == Intent.ACTION_POWER_CONNECTED || intent.action == Intent.ACTION_POWER_DISCONNECTED || intent.action == "com.android.s22present.NOTIFICATION_RECEIVED")
                {
                    // Log change
                    if (Globals.maindisplay!!.state == Display.STATE_OFF && processing == 0)
                    {
                        processing = 1
                        Runtime.getRuntime().exec("settings put system screen_off_timeout 500")
                        Log.d("S22PresListServ", "Action requiring screen on detected")
                        // Create a message to turn the screen on and send it to ScreenService.
                        request = Message.obtain(null, 2, 0, 0)
                        rootservice?.send(request)
                        Log.d("S22PresListServ", "I've asked for a wakeup")
                        Handler(Looper.getMainLooper()).postDelayed(
                            {
                                processing = 0
                            }, 5000
                        )
                    } else {
                        // Do nothing and log it.
                        Log.d("S22PresListServ", "I'm not doing anything.")
                    }
                }
                // If the System reports that the main screen is on.
                if (intent.action == Intent.ACTION_USER_PRESENT)
                {
                    Log.d("S22PresListServ", "Activity detected.")
                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            if(processing==0)
                            {
                                request = Message.obtain(null, 1, 0, 0)
                                rootservice?.send(request)
                                Log.d("S22PresListServ", "I've asked the second screen to be turned off")
                            }
                            else
                            {
                                Log.d("S22PresListServ", "I'm not doing anything.")
                            }
                        }, 3000)
                }
                // If the System reports that the current call status has changed.
                if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED)
                {
                    // Grab the current call status.
                    val state = TelephonyManager.EXTRA_STATE
                    // If the phone is ringing
                    if (state == TelephonyManager.EXTRA_STATE_RINGING)
                    {
                        // Log change
                        Log.d("S22PresListServ", "The Phone is doing something.")
                        // Create a message to turn the screen on and send it to ScreenService.
                        request = Message.obtain(null, 2, 0, 0)
                        rootservice?.send(request)
                        Log.d("S22PresListServ", "I've asked for a wakeup")
                    } else {
                        // If another request to turn the screen on does currently exist.
                        // Do nothing and log it.
                        Log.d("S22PresListServ", "The Phone isn't ringing.")
                    }
                }
            }
        }
        // Create a filter for the Broadcast Receiver
        val screenStateFilter = IntentFilter()
        // Add various actions to it.
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF)
        screenStateFilter.addAction(Intent.ACTION_SCREEN_ON)
        screenStateFilter.addAction(Intent.ACTION_POWER_CONNECTED)
        screenStateFilter.addAction(Intent.ACTION_POWER_DISCONNECTED)
        screenStateFilter.addAction("com.android.s22present.NOTIFICATION_RECEIVED")
        screenStateFilter.addAction("com.android.s22present.NOTIFICATION_REMOVED")
        screenStateFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
        screenStateFilter.addAction(Intent.ACTION_USER_PRESENT)
        // Start the Receiver with the filter we just created.
        registerReceiver(screenStateReciever, screenStateFilter)
        Log.d("S22PresListServInit", "Listening...")
        return START_STICKY
    }

    // Unused.
    override fun onBind(intent: Intent?): IBinder? {
      return null
    }
}
// Notification listener. Whilst technically a service is it ran so long as it's declared in the manifest.
class NotificationService : NotificationListenerService() {
    val currentnotifs : String? = null
    // When listener created.
    override fun onCreate() {
        // Log.
        Log.d("S22PresNotifServInit", "Listening...")
        super.onCreate()
    }

    // When Notification created.
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // Store information in variables
        currentnotifs.plus(sbn.id.toString())
        val packageName = sbn.packageName
        val extras = sbn.notification.extras
        val title = extras.getString("android.title")
        val text = extras.getString("android.text")
        if(Globals.titlefield.text =="")
        {
            ObjectAnimator.ofFloat(Globals.datefield, "translationY", -4f).apply { duration = 500; start()}
            ObjectAnimator.ofFloat(Globals.timefield, "translationY", -4f).apply { duration = 500; start()}
        }
        if(title != Globals.titlefield.text) {
            Globals.titlefield.text = title
            Log.d("S22PresNotifServ", "Ping!")
            Intent().also { broadcast ->
                broadcast.setAction("com.android.s22present.NOTIFICATION_RECEIVED")
                sendBroadcast(broadcast)
            }
        }
        if(text != Globals.contentfield.text)
        {
            Globals.contentfield.text = text
        }
    }
    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        Log.d("S22PresNotifServ", "Something got removed.")
            val title = sbn?.notification?.extras?.getString("android.title")
            val text = sbn?.notification?.extras?.getString("android.text")
            if(Globals.titlefield.text == title && Globals.contentfield.text == text)
            {
                Log.d("S22PresNotifServ", "Clearing display.")
                Globals.titlefield.text = ""
                Globals.contentfield.text = ""
                ObjectAnimator.ofFloat(Globals.datefield, "translationY", 4f).apply { duration = 500; start() }
                ObjectAnimator.ofFloat(Globals.timefield, "translationY", 4f).apply { duration = 500; start() }
            }
            super.onNotificationRemoved(sbn)
        }
    }








