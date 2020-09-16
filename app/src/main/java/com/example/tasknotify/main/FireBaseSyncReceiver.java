package com.example.tasknotify.main;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.tasknotify.R;

public class FireBaseSyncReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //Mỗi khi cái alarm thức giấc thì cái Broadcast revicer này đc gọi và chạy vào đấy

        //cái intentCallback này nghĩa là mình ấn cái thông báo thì nó vào lại app ở vị trí trang MainActiviti thôi
        Intent intentCallback = new Intent(context, MainActivity.class);
        //lấy dữ liệu mình cần dung mà mk set lúc add alarm từ intent ra
        String todoName = intent.getStringExtra("todo_name");
        int todoRequestCode = intent.getIntExtra("todo_rc", 0);
        int priority = intent.getIntExtra("todo_priority", 0);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, -todoRequestCode, intentCallback, 0);

        //gửi thông báo lên thôi
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, MyApplication.alarm_channel_id)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Công việc")
                .setContentText("Nhận công việc: " + todoName + " "+todoRequestCode)
                .setContentIntent(pendingIntent)
                //NotificationCompat.PRIORITY_DEFAULT
                .setPriority(priority)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(todoRequestCode, notificationBuilder.build());


    }


}
