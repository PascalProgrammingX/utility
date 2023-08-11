package com.utility.handler

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.utility.R


fun createNotificationChannel(activity: Context):NotificationManager{
    val notificationManager = activity.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//    val uri = Uri.parse("android.resource://" + activity.packageName + "/" + R.raw.alert) // For custom notification sound
//    val attributes = AudioAttributes.Builder()
//        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
//        .build()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val mChannel =  NotificationChannel("NOTIFICATION_SERVICE",
            "NOTIFICATION", //Channel name
            NotificationManager.IMPORTANCE_HIGH)

        // Configure the notification channel.
        mChannel.description = "Helene Finance Notification Update."
        mChannel.enableLights(true)
        mChannel.enableVibration(true)
        mChannel.setShowBadge(true)
       // mChannel.setSound(uri, attributes) // uncomment when you've added the raw sound file.
        mChannel.lightColor = Color.GREEN
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mChannel.setAllowBubbles(true)
        }
        notificationManager.createNotificationChannel(mChannel)
    }
    return notificationManager
}


    /*
    Call this function in your onReceive function of Firebase messaging to show notification
    When app is in foreground, a notification bubble is displayed.
     */
    fun showNotification(activity: Context, title:String, message:String){
        val notificationManager = createNotificationChannel(activity)
        val notificationIntent = Intent(activity, null) //Activity yo nativate to when notification is clicked
        notificationIntent.putExtra("title", title)
        notificationIntent.putExtra("message", message)
        notificationIntent.flags = (Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val intent = PendingIntent.getActivity(activity, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT) // This is needed for Android 10+ devices

        val notificationBuilder = NotificationCompat.Builder(activity, "NOTIFICATION_SERVICE")
        notificationBuilder.setAutoCancel(true)
            .setVibrate(longArrayOf(0, 1000, 500, 2000))
            .setStyle(NotificationCompat.BigTextStyle())
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(getBitmapFromVectorDrawable(activity))
            .setContentIntent(intent)
            .setChannelId("NOTIFICATION_SERVICE")
            .setContentTitle(title)
            .setContentText(message)
            .setOnlyAlertOnce(true)
        val notification = notificationBuilder.build()
        notificationManager.notify(890, notification)
    }



fun getBitmapFromVectorDrawable(context: Context): Bitmap? {
    val drawable = ContextCompat.getDrawable(context, R.mipmap.ic_launcher)
    val bitmap = Bitmap.createBitmap(
        drawable!!.intrinsicWidth,
        drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}