package com.example.tasknotify.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tasknotify.Interface.OnFirebaseDelete;
import com.example.tasknotify.R;
import com.example.tasknotify.main.EditTodoActivity;
import com.example.tasknotify.main.ObserverManager;
import com.example.tasknotify.model.Todo;
import com.example.tasknotify.repository.RepoFireStoreData;
import com.example.tasknotify.repository.SQLiteHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;



public class RecycleViewAdapterTodo extends RecyclerView.Adapter<RecycleViewAdapterTodo.MyViewHolder> {
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    public  List<Todo> todos = new ArrayList<>();
    private RepoFireStoreData repoFireStoreData = RepoFireStoreData.getInstance();
    private SQLiteHelper sqLiteHelper ;
    public RecycleViewAdapterTodo(List<Todo> todos) {
        this.todos = todos;
    }
    private int pYear,pMonth,pDay,pHour,pMinute;


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.todo_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        sqLiteHelper = new SQLiteHelper(holder.itemView.getContext());


        //Set ảnh lên item
        String url = todos.get(position).getUrlImagePreview();

        //tách đường dẫn có chưa /todo là forder chưa ảnh ra để lấy tên ảnh
        //ví du todo/phamphuong.jpg = > cái cần ấy là phamphuong.jpg
        String[] files = url.split("/");
        String fileName = files[2];
        File folder = new File(holder.imageView.getContext().getCacheDir(), files[1]);
        if (!folder.exists()) folder.mkdir();
        final File file = new File(folder, fileName);
        //kiểm tra trong cache của app có ảnh chưa , nếu có rồi thì gọi ra và set luôn lên layout
        if (file.exists()) {
            Picasso.get().load(file).into(holder.imageView);
        } else {
            //nếu chưa có thì gọi storage trong firebase để lấy ảnh lưu vào cache để lần sau load nó vào nhánh bên trên thì tối ưu
            //sau đó lại set ảnh lên layout luôn
            storage.getReference().child(todos.get(position).getUrlImagePreview()).getFile(file)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Picasso.get().load(file).into(holder.imageView);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }

       //set thời gian
        //từ cái atribute dateTask trong 1 cái todo thì mình tách ra đc ngày , tháng năm giờ để hiện thị thôi
        //còn làm việc vs alarm thì dùng cái  dataTask=122323123213 ví dụ thế
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(todos.get(position).getDateTask());
        pYear=calendar.get(Calendar.YEAR);
        pMonth=calendar.get(Calendar.MONTH);
        pDay=calendar.get(Calendar.DAY_OF_MONTH);
        pHour=calendar.get(Calendar.HOUR_OF_DAY);
        pMinute=calendar.get(Calendar.MINUTE);


        //set prority , chắc hiểu
        switch ((int) todos.get(position).getPriority()){
            case 0:
                holder.itemPriority.setText("Mức: rất quan trọng");
                break;
            case 1:
                holder.itemPriority.setText("Mức: auan trọng");
                break;
            case 2:
                holder.itemPriority.setText("Mức: bình thường");
                break;
        }

        holder.itemName.setText(todos.get(position).getName());
        holder.itemTime.setText("Thời gian : "+pHour+":"+pMinute+"    "+pDay+"/"+(pMonth+1)+"/"+pYear);
        holder.btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              Intent intent = new Intent(holder.itemView.getContext(), EditTodoActivity.class);
                intent.putExtra("todo", (Serializable) todos.get(position));
               holder.itemView.getContext().startActivity(intent);
            }
        });


        holder.btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                repoFireStoreData.deleteTodo(todos.get(position).getId(), new OnFirebaseDelete() {
                    @Override
                    public void deleteSqlite(String id) {
                        long count  = sqLiteHelper.deleteTodo(id);
                        if (count >= 0) {
                            Toast.makeText(holder.itemView.getContext(), "Deleted " + count, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(holder.itemView.getContext(), "Deleted failed", Toast.LENGTH_SHORT).show();
                        }
                        ObserverManager.getInstance().data.setValue((int) 1);
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {

        return todos.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        public ImageView imageView;
        public TextView itemName;
        public TextView itemTime;
        public TextView itemPriority;
        public CardView cardView;
        public ImageButton btnEdit;
        public ImageButton btnRemove;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView=itemView.findViewById(R.id.cardView);
            imageView = itemView.findViewById(R.id.imageView);
            itemName = itemView.findViewById(R.id.itemName);
            itemTime = itemView.findViewById(R.id.txtTime);
            itemPriority = itemView.findViewById(R.id.txtPriority);
            btnEdit=itemView.findViewById(R.id.btnEdit);
            btnRemove=itemView.findViewById(R.id.btnRemove);

        }
    }
}

