package com.zhadko.mapsapp.di

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.zhadko.mapsapp.MainActivity
import com.zhadko.mapsapp.R
import com.zhadko.mapsapp.utils.Const.NOTIFICATION_CHANNEL_ID
import com.zhadko.mapsapp.utils.Const.PENDING_INTENT_REQUEST_CODE
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
object ServiceModuleProvider {

    @Provides
    @ServiceScoped
    fun provideNotification(
        @ApplicationContext context: Context,
        pendingIntent: PendingIntent,
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_run)
            .setContentIntent(pendingIntent)
    }

    @Provides
    @ServiceScoped
    fun provideLocationPendingIntent(
        @ApplicationContext context: Context,
    ): PendingIntent {
        return PendingIntent.getActivity(
            context, PENDING_INTENT_REQUEST_CODE,
            Intent(
                context, MainActivity::class.java
            ), PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    @Provides
    @ServiceScoped
    fun provideNotificationManager(
        @ApplicationContext context: Context,
    ): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
}