package com.bardavid.boaz.photorater;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class notificationService extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"notifyPhotoRater")
                .setSmallIcon(R.drawable.hearts_logo)
                .setContentTitle("It's been a while!")
                .setContentText("Come check how many people rated your photos")
                .setContentIntent(PendingIntent.getActivity(context,0, new Intent(context, MainActivity.class),PendingIntent.FLAG_IMMUTABLE))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager=NotificationManagerCompat.from(context);
        notificationManager.notify(200,builder.build());

    }
}
