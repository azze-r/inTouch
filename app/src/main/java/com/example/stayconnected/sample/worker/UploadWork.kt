package com.example.stayconnected.sample.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.stayconnected.R
import com.example.stayconnected.sample.ui.MainActivity


@Suppress("SameParameterValue")
class UploadWork(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    companion object {
        const val KEY_RECEIVE = "RECEIVING DATA"
    }

    override fun doWork(): Result {
        Log.i("tryhard","doWork")

        displayNotification("Working on notifications.","Restez Connecté")

        val data1 = Data.Builder()
            .putString(KEY_RECEIVE, "WORK DATA RECEIVED")
            .build()

        return Result.success(data1)
    }

    private fun displayNotification(task: String, desc: String) {
        Log.i("tryhard","displayNotification")

        // Create an Intent for the activity you want to start
        val resultIntent = Intent(applicationContext, MainActivity::class.java)

        val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(applicationContext).run {
            // Add the intent, which inflates the back stack
            addNextIntentWithParentStack(resultIntent)
            // Get the PendingIntent containing the entire back stack
            getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        val manager: NotificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel("TestingWorkManger", "TestingWorkManger", NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(notificationChannel)
        }

        val builder = NotificationCompat.Builder(applicationContext, "TestingWorkManger")
            .setContentIntent(resultPendingIntent)
            .setContentTitle(task)
            .setContentText(desc)
            .setSmallIcon(R.mipmap.touch)

        manager.notify(1, builder.build())
    }

}