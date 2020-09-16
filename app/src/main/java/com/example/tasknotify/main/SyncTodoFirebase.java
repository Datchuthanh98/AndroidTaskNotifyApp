package com.example.tasknotify.main;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.example.tasknotify.Interface.TodoConnectFirebase;
import com.example.tasknotify.model.Todo;
import com.example.tasknotify.repository.RepoFireStoreData;
import com.example.tasknotify.repository.SQLiteHelper;

import java.util.Calendar;
import java.util.List;

import static android.content.Context.ALARM_SERVICE;

public class SyncTodoFirebase {
    static SyncTodoFirebase instance;
    private RepoFireStoreData repoFireStoreData = RepoFireStoreData.getInstance();
    private SQLiteHelper sqLiteHelper;
    private SharedPreferences sharedPref;

    public static SyncTodoFirebase getInstance() {
        if (instance == null) {
            instance = new SyncTodoFirebase();
        }
        return instance;
    }

    public void synchronizeFireStore(final Context context) {

        if (sqLiteHelper == null) sqLiteHelper = new SQLiteHelper(context);
        //Khi cập nhập lại list todo từ firebase thì hủy hết các đăng kí alarm cũ đi và xóa data trong sqlie đi
        cancelAllAlarms(context);
        sqLiteHelper.resetTableTodo();

        sharedPref = context.getSharedPreferences("sessionUser", Context.MODE_PRIVATE);
        String userID = sharedPref.getString("idUser", "");

        //dùng cái userID lưu trong sharePreference kia để cập nhập listtodo đúng vs user đó
        // nếu ko reset bướ bên trên thì sqlite chưa cả dữ liệu của user đăng nhập cũ => rò rỉ dữ liệu :v

        //add data from firebase to sqlite
        repoFireStoreData.loadListTodo(userID, new TodoConnectFirebase() {
            @Override
            public void onPlayerLoadedFromFireBase(List<Todo> todos) {
                for (int i = 0; i < todos.size(); i++) {
                    // do reset bên trên lên cái todo đầu tiên update request code là =1( xem chi tiết trong sqliteHelper)
                    // for lần lượt thì set đc cho từng cái có request code là 1 , 2 3,4.....
                    todos.get(i).setRequestCode(sqLiteHelper.getMaxRequestCode() + 1);
                    // đăng kí cái todo đó vào alarm để đến time= dateTask nó gửi thông báo cho mình xem
                    addAlarm(context, todos.get(i));
                    sqLiteHelper.insertTodo(todos.get(i));
                }
                // set lại timestamp vào shared preference
                ObserverManager.getInstance().data.setValue((int) 1);
            }
        });
    }

    private void addAlarm(Context context, Todo todo) {
        Toast.makeText(context,"Cập nhập danh sách thành công",Toast.LENGTH_SHORT).show();

        //Nếu thời gian của todo lớn hơn thời gian hiện tại thì đăng kí thông báo
        if (Calendar.getInstance().getTimeInMillis() < todo.getDateTask()) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            Intent intent = new Intent(context, FireBaseSyncReceiver.class);
            //truyên 1 số data vào intent để bên thông báo nhận đc intent này lấy ra set lên thông báo thôi
            intent.putExtra("todo_name", todo.getName());
            intent.putExtra("todo_rc", todo.getRequestCode());
            intent.putExtra("todo_priority", (int)todo.getPriority());
            //trong đăng kí alarm thì phải có intent và mã của đăng kí đấy chính là cái requestCode của todo đó
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, todo.getRequestCode(), intent, 0);
            //thời gian alarm nó báo thức chính là cái todo.getDataTask() đặt ở dòng dưới
            alarmManager.set(AlarmManager.RTC_WAKEUP, todo.getDateTask(), pendingIntent);
        }
    }

    private void cancelAllAlarms(Context context) {
        //cái hủy này đơn giản là cái todo nào có requestCode mà tồn tai trong alarm thì nó hủy đi thôi
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        List<Todo> todos = sqLiteHelper.getAllTodos();
        for (Todo todo : todos) {
            Intent intent = new Intent(context, FireBaseSyncReceiver.class);
            //FLAG_NO_CREATE ko tim thay thi ko tao ra
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, todo.getRequestCode(), intent, PendingIntent.FLAG_NO_CREATE);
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
            }
        }
    }


}
