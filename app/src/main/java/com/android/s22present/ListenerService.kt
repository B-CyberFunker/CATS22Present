package com.android.s22present

import android.animation.ObjectAnimator
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.display.DisplayManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.UEventObserver
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.telephony.TelephonyManager
import android.util.Log
import android.view.Display
import android.view.WindowManager
import android.view.WindowManagerGlobal
import androidx.core.view.isInvisible
import com.topjohnwu.superuser.ipc.RootService
import java.io.File


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
            // When connected
            Log.i("S22PresScreenServInit", "Connecting...")
            rootservice = Messenger(service)
            // Check that a messenger was actually received (in some of my testing it'd report connected, but the ScreenService wasn't actually running)
            if(rootservice!=null)
            {
                        Globals.loading.progress = 5
                        Globals.loadingtext.text = "Done!"
                        Log.i("S22PresScreenServInit", "Done!")
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
            Log.i("S22PresScreenServInit", "Disconnected.")
        }
    }
    // This function starts the ScreenService before it is actually required and binds to it.
    fun startscreenservice(): Unit?
    {
        Globals.loading.progress = 4
        Globals.loadingtext.text = "Starting ScreenService..."
        Log.i("S22PresListServInit", "Let's get the service for the screen running...")
        // Identify ScreenService
        val intent = Intent(this, ScreenService::class.java)
        RootService.stop(intent)
        // Bind to it
        RootService.bind(intent, RootConnect)
        return null
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
    {
        Log.i("S22PresListServInit", "Hello!")
        val displaymanager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val display0 = displaymanager.displays[0]
        val display1 = displaymanager.displays[1]
        val file = "settings"
        val filedir = File(filesDir, file)
        try
        {
            val settings = filedir.readText().split("|").toTypedArray()
            Globals.style = settings[0].toString()
            Globals.font = settings[1].toString()
        }
        catch (e: Exception)
        {
            Log.w("S22PresListServInit", "Failed to load settings. Continuing with defaults.")
        }
        // Identify and show presentation.
        val present = PresentationHandler(this, display1)
        present.show()
        // When this service starts.
        // Update Progress bar and log.
        Globals.loading.progress = 3
        Globals.loadingtext.text = "ListenerService Started!"
        // Bind to ScreenService.
        startscreenservice()
        // Create a variable to store requests for ScreenService.
        var request: Message
        // Create a BroadcastReceiver.
        val screenStateReciever = object : BroadcastReceiver()
        {
            var lid = "open"
            override fun onReceive(context: Context, intent: Intent)
            {
                // When a Broadcast is Received.

                if (intent.action == Intent.ACTION_POWER_CONNECTED || intent.action == Intent.ACTION_POWER_DISCONNECTED)
                {
                    wakeup()
                }
                if (intent.action == "com.android.s22present.NOTIFICATION_RECEIVED")
                {
                    wakeuplong()
                }
                if (intent.action == "com.android.s22present.LIDOPEN")
                {
                    lid = "open"
                }
                if (intent.action == "com.android.s22present.LIDCLOSED")
                {
                    lid = "closed"
                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            wakeup()
                        },250)
                }
                if(intent.action == Intent.ACTION_SCREEN_ON)
                {
                    turnoff()
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
                        Log.v("S22PresListServ", "The Phone is ringing.")
                        wakeuplong()
                    }
                }
            }
            fun wakeup()
            {
                if (lid == "closed")
                {
                    Log.v("S22PresListServ", "Action requiring screen on detected")
                    request = Message.obtain(null, 2, 0, 0); rootservice?.send(request)
                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            if (lid == "closed")
                            {
                                Log.v("S22PresListServ", "Still closed. Going to sleep.")
                                request = Message.obtain(null, 3, 0, 0); rootservice?.send(request)
                            }
                        }, 2000)
                }
            }
            fun wakeuplong()
            {
                if(lid=="closed")
                {
                    Log.v("S22PresListServ", "Action requiring screen on detected")
                    request = Message.obtain(null, 2, 0, 0); rootservice?.send(request)
                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            if(lid=="closed")
                            {
                                Log.v("S22PresListServ", "Still closed. Going to sleep.")
                                request = Message.obtain(null, 3, 0, 0); rootservice?.send(request)
                            }
                        },8000)
                }
            }
            fun turnoff()
            {
                if(lid=="open")
                {
                    request = Message.obtain(null, 1, 0, 0)
                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            Log.v("S22PresListServ", "Action requiring screen off detected")
                            rootservice?.send(request)
                        }, 850)
                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            if(display1.state== Display.STATE_ON)
                            rootservice?.send(request)
                        }, 1400)
                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            if(display1.state== Display.STATE_ON)
                                rootservice?.send(request)
                        }, 1800)
                }
            }
        }
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        val proximitySensorEventListener: SensorEventListener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
            override fun onSensorChanged(event: SensorEvent)
            {
                if (event.sensor.type == Sensor.TYPE_PROXIMITY) {
                    if (event.values[0] == 0f)
                    {
                        Handler(Looper.getMainLooper()).postDelayed(
                            {
                                if (display0!!.state == Display.STATE_OFF)
                                {
                                    Log.v("S22PresSensList", "Shut")
                                    Intent().also { broadcast -> broadcast.setAction("com.android.s22present.LIDCLOSED")
                                        sendBroadcast(broadcast) }
                                }
                            }, 1500)
                    }
                    else
                    {
                        Handler(Looper.getMainLooper()).postDelayed(
                            {
                                if (display0!!.state == Display.STATE_ON)
                                {
                                    Log.v("S22PresSensList", "Open")
                                    Intent().also { broadcast -> broadcast.setAction("com.android.s22present.LIDOPEN")
                                        sendBroadcast(broadcast)
                                    }
                                }
                            }, 400)
                    }

                }
            }
        }
        sensorManager.registerListener(proximitySensorEventListener, proximitySensor, SensorManager.SENSOR_DELAY_FASTEST)
        // Create a filter for the Broadcast Receiver
        val screenStateFilter = IntentFilter()
        // Add various actions to it.
        screenStateFilter.addAction(Intent.ACTION_SCREEN_ON)
        screenStateFilter.addAction(Intent.ACTION_POWER_CONNECTED)
        screenStateFilter.addAction(Intent.ACTION_POWER_DISCONNECTED)
        screenStateFilter.addAction("com.android.s22present.NOTIFICATION_RECEIVED")
        screenStateFilter.addAction("com.android.s22present.NOTIFICATION_REMOVED")
        screenStateFilter.addAction("com.android.s22present.LIDCLOSED")
        screenStateFilter.addAction("com.android.s22present.LIDOPEN")
        screenStateFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
        // Start the Receiver with the filter we just created.
        registerReceiver(screenStateReciever, screenStateFilter)
        Log.i("S22PresListServInit", "Listening...")
        return START_NOT_STICKY
    }
    // Unused.
    override fun onBind(intent: Intent?): IBinder? {
      return null
    }
}
// Notification listener. Whilst technically a service is it ran so long as it's declared in the manifest.
class NotificationService : NotificationListenerService() {
    var currentnotifs : String? = null
    var musicactive=false
    var musicnotiftitle:String = ""
    var musicnotiftext:String = ""
    // When listener created.
    override fun onCreate() {
        // Log.
        Log.i("S22PresNotifServInit", "Listening...")
        super.onCreate()
    }

    // When Notification created.
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // Store information in variables
        // currentnotifs.plus(sbn.id.toString())
        val packageName = sbn.packageName
        val extras = sbn.notification.extras
        val title = extras.getString("android.title")
        val text = extras.getString("android.text")
        if(title != null.toString() || text != null.toString()) {
            if (Globals.titlefield.text == "") {
                ObjectAnimator.ofFloat(Globals.datefield, "translationY", -20f)
                    .apply { duration = 500; start() }
                ObjectAnimator.ofFloat(Globals.timefield, "translationY", -20f)
                    .apply { duration = 500; start() }
                ObjectAnimator.ofFloat(Globals.titlefield, "translationY", 0f)
                    .apply { duration = 500; start() }
                ObjectAnimator.ofFloat(Globals.contentfield, "translationY", 0f)
                    .apply { duration = 500; start() }
            }
            if (title != Globals.titlefield.text) {
                Log.v("S22PresNotifServ", "Ping!")
                if (packageName == "it.vfsfitvnm.vimusic" || packageName == "com.google.android.apps.youtube.music" || packageName == "com.spotify.music" || packageName == "org.fossify.musicplayer") {
                    Log.v("S22PresNotifServ", "Music")
                    musicactive = true
                    musicnotiftitle = title
                    musicnotiftext = text
                    when(Globals.visual)
                    {
                        1->{Globals.visualbar.isInvisible = false}
                        2->{Globals.visualsquare.isInvisible = false}
                    }
                }
                Globals.titlefield.text = title
                Intent().also { broadcast ->
                    broadcast.setAction("com.android.s22present.NOTIFICATION_RECEIVED")
                    sendBroadcast(broadcast)
                }

            }
            if (text != Globals.contentfield.text) {
                Globals.contentfield.text = text
            }
        }
    }
    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        Log.v("S22PresNotifServ", "Something got removed.")
            val title = sbn?.notification?.extras?.getString("android.title")
            val text = sbn?.notification?.extras?.getString("android.text")
            if(Globals.titlefield.text == title && Globals.contentfield.text == text)
            {
                Log.v("S22PresNotifServ", "Clearing display.")
                if(musicactive && title != musicnotiftitle)
                {
                    Log.v("S22PresNotifServ", "Switching to music")
                    Globals.titlefield.text = musicnotiftitle
                    Globals.contentfield.text = musicnotiftext
                }
                else
                {
                    Globals.titlefield.text = ""
                    Globals.contentfield.text = ""
                    ObjectAnimator.ofFloat(Globals.datefield, "translationY", 0f).apply { duration = 500; start() }
                    ObjectAnimator.ofFloat(Globals.timefield, "translationY", 0f).apply { duration = 500; start() }
                    ObjectAnimator.ofFloat(Globals.titlefield, "translationY", 20f).apply { duration = 500; start() }
                    ObjectAnimator.ofFloat(Globals.contentfield, "translationY", 20f).apply { duration = 500; start() }
                }
            }
        if(musicactive && title==musicnotiftitle)
        {
            Log.v("S22PresNotifServ", "Clearing music")
            when(Globals.visual)
            {
                1->{Globals.visualbar.isInvisible = true}
                2->{Globals.visualsquare.isInvisible = true}
            }
            musicactive=false
            musicnotiftitle=""
            musicnotiftext=""
        }
        super.onNotificationRemoved(sbn)
        }
    }








