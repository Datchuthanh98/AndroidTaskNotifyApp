package com.example.tasknotify.fragment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import com.example.tasknotify.Interface.OnFirebaseInsert;
import com.example.tasknotify.R;
import com.example.tasknotify.main.ObserverManager;
import com.example.tasknotify.model.Todo;
import com.example.tasknotify.repository.RepoFireStoreData;
import com.example.tasknotify.repository.SQLiteHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;


import static com.example.tasknotify.R.*;

public class FragmentUserAdd extends Fragment implements View.OnClickListener {
    private EditText txtName, txtDate, txtHour;
    private String urlFirebase = "";
    private ImageView imagePreview;
    private Spinner listPriority;
    private Button add;
    private SQLiteHelper sqLiteHelper;
    public static int RESULT_LOAD_IMG = 1012;
    private Dialog uploadingDialog;
    private FirebaseAuth mAuth;
    private ImageButton btnDate,btnHour;
    Uri imageUri;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private RepoFireStoreData repoFireStoreData = RepoFireStoreData.getInstance();
    private int pYear,pMonth,pDay,pHour,pMinute;
    long timeTimeLong;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(layout.add_task_fragment_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sqLiteHelper = new SQLiteHelper(getContext());
        txtName = view.findViewById(R.id.txtName);
        txtDate=view.findViewById(R.id.txtDate);
        txtHour=view.findViewById(R.id.txtHour);
        btnDate=view.findViewById(R.id.btnPickDate);
        btnHour=view.findViewById(id.btnPickHour);
        listPriority = view.findViewById(R.id.listPriority);
        imagePreview = view.findViewById(id.imgPreview);

        uploadingDialog = new Dialog(getContext());
        uploadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        uploadingDialog.setContentView(layout.custom_loading_layout);
        uploadingDialog.setCancelable(false);

        imagePreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);

            }
        });

        add = view.findViewById(R.id.btnAdd);
        addItemSpinner();

        add.setOnClickListener(this);
        btnDate.setOnClickListener(this);
        btnHour.setOnClickListener(this);

        ObserverManager.getInstance().data.observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                addItemSpinner();
            }
        });
    }

    @Override
    public void onClick(View v) {

        if (v == add) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(pYear,pMonth,pDay,pHour,pMinute,0);  // gộp các trường bị tách ra thàn h1 biến dateTask=123232323
             timeTimeLong=calendar.getTimeInMillis();
            Todo todo = setTodo();
            repoFireStoreData.insertTodo(todo, new OnFirebaseInsert() {
                @Override
                public void insertSqlite(Todo todo) {   // lưu data lên firebase
                    uploadImageToFireBase();  // lưu data thành công thì lưu ảnh lên firebase
                    long addedId = sqLiteHelper.insertTodo(todo);  // thành công nữa thì lưu dữ liệu xuống sqlite để tối ưu
                    if (addedId >= 0) {
                        Toast.makeText(getContext(), "Inserted " + addedId, Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(getContext(), "Inserted failed", Toast.LENGTH_SHORT).show();
                    }

                    //Thông báo cho các cái đã đăng kí lắng nghe là "data" vừa bị tác động đó để cho các cái đã đăng kí
                    // làm j đó nhưu ở đây là update lại dữ liệu mới nhất
                    ObserverManager.getInstance().data.setValue((int) 1);
                }
            });



    }

        if (v == btnDate) {
            Calendar c = Calendar.getInstance();
            Dialog datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                    String dateString =  day+"/"+month+"/"+year;
                    pDay=day;
                    pMonth=month;
                    pYear=year;
                    txtDate.setText(dateString);
                }
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        }

        if (v == btnHour) {
            Calendar c = Calendar.getInstance();
            Dialog timePickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    String timeString = hourOfDay + ":" + minute;
                    txtHour.setText(timeString);
                    pHour=hourOfDay;
                    pMinute=minute;
                }
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
            timePickerDialog.show();

        }

    }

    public void addItemSpinner() {
        String[] list = new String[]{"Rất quan trọng", "quan trọng", "bình thường"};
        ArrayAdapter<String> data = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, list);
        data.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        listPriority.setAdapter(data);
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            try {
                //lưu giá trị ảnh
                imageUri = data.getData();
                final InputStream imageStream = getContext().getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                //set ảnh lên màn hình
                imagePreview.setImageBitmap(selectedImage);
                Cursor returnCursor =
                        getContext().getContentResolver().query(imageUri, null, null, null, null);
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                returnCursor.moveToFirst();
                //lưu tên ảnh kèm folder để sau  add lên firebase
                urlFirebase ="/todo/" + returnCursor.getString(nameIndex);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_LONG).show();
            }

        } else {
            Toast.makeText(getContext(), "You haven't picked Image", Toast.LENGTH_LONG).show();
        }
    }



    public void uploadImageToFireBase() {
        // add ảnh lên firebase cần 2 thứ là đường dẫn ảnh ví dụ /todo/phamphuong.jgp và nội dung ảnh imageUri

        FirebaseStorage storage = FirebaseStorage.getInstance();
        UploadTask task = storage.getReference().child(urlFirebase).putFile(imageUri);
        uploadingDialog.show();

        task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                uploadingDialog.cancel();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // huy dialog hien loi
                uploadingDialog.cancel();
                Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public Todo setTodo() {
        // với add thì tạo todo như này , t tách ra cho clear ,
          Todo todo = new Todo();
          todo.setName(txtName.getText().toString());
          todo.setUrlImagePreview(urlFirebase);
          todo.setPriority(listPriority.getSelectedItemPosition());
          todo.setDateTask (timeTimeLong);
          todo.setUserID(FirebaseAuth.getInstance().getUid());
         return todo;
    }

}
