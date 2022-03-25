package nl.inholland.myvitality.ui.push_notifications

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.data.TokenApiClient
import nl.inholland.myvitality.data.entities.requestbody.PushToken
import nl.inholland.myvitality.ui.MainActivity
import nl.inholland.myvitality.ui.notification.NotificationActivity
import nl.inholland.myvitality.util.SharedPreferenceHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import kotlin.random.Random

class PushService : FirebaseMessagingService() {
    @Inject
    lateinit var sharedPrefs: SharedPreferenceHelper

    @Inject
    lateinit var apiClient: TokenApiClient

    private val CHANNEL_ID = "vitality_channel"

    override fun onCreate() {
        super.onCreate()

        (application as VitalityApplication).appComponent.inject(this)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val intent = Intent(this, NotificationActivity::class.java)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = Random.nextInt()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        Log.d("TAG", message.data.toString())

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(message.data["title"])
            .setContentText(message.data["text"])
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channelName = "vitality_notifications"
        val channel = NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Vitality notifications channel"
        }
        notificationManager.createNotificationChannel(channel)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        sharedPrefs.pushToken = token
        if(sharedPrefs.isLoggedIn()) {
            apiClient.createPushToken("Bearer ${sharedPrefs.accessToken}", PushToken(token)).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    Log.i("LoginActivity", "New pushtoken sent to API")
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("LoginActivity", "onFailure: ", t)
                }
            })
        }

        Log.d("token", "New Token: ${sharedPrefs.pushToken}")
    }
}