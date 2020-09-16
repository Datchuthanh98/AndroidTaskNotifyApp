package com.example.tasknotify.main;

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
import android.view.View;
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
import androidx.appcompat.app.AppCompatActivity;

import com.example.tasknotify.Interface.OnFirebaseUpdate;
import com.example.tasknotify.R;
import com.example.tasknotify.model.Todo;
import com.example.tasknotify.repository.RepoFireStoreData;
import com.example.tasknotify.repository.SQLiteHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;

public class EditTodoActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "EditTaskActivity";
    private EditText txtName, txtDate, txtHour;
    private String urlFirebase;
    private ImageView imagePreview;
    private Spinner listPriority;
    private Button edit;
    private SQLiteHelper sqLiteHelper;
    public static int RESULT_LOAD_IMG = 1012;
    private Dialog uploadingDialog;
    private FirebaseAuth mAuth;
    private ImageButton btnDate, btnHour;
    Uri imageUri;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private RepoFireStoreData repoFireStoreData = RepoFireStoreData.getInstance();
    private int pYear, pMonth, pDay, pHour, pMinute;
    long timeTimeLong;
    Todo todo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_task_fragment);

        Intent intent = getIntent();
        todo = (Todo) intent.getSerializableExtra("todo");

        sqLiteHelper = new SQLiteHelper(getApplicationContext());
        txtName = findViewById(R.id.txtName);
        txtDate = findViewById(R.id.txtDate);
        txtHour = findViewById(R.id.txtHour);
        btnDate = findViewById(R.id.btnPickDate);
        btnHour = findViewById(R.id.btnPickHour);
        listPriority = findViewById(R.id.listPriority);
        imagePreview = findViewById(R.id.imgPreview);

        uploadingDialog = new Dialog(EditTodoActivity.this);
        uploadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        uploadingDialog.setContentView(R.layout.custom_loading_layout);
        uploadingDialog.setCancelable(false);

        imagePreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);

            }
        });

        edit = findViewById(R.id.btnEdit);
        addItemSpinner();

        edit.setOnClickListener(this);
        btnDate.setOnClickListener(this);
        btnHour.setOnClickListener(this);
        timeTimeLong = todo.getDateTask();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeTimeLong);
        pYear = calendar.get(Calendar.YEAR);
        pMonth = calendar.get(Calendar.MONTH);
        pDay = calendar.get(Calendar.DAY_OF_MONTH);
        pHour = calendar.get(Calendar.HOUR_OF_DAY);
        pMinute = calendar.get(Calendar.MINUTE);
        txtDate.setText(pDay + "/" + (pMonth+1) + "/" + pYear);
        txtHour.setText(pHour + ":" + pMinute);
        txtName.setText(todo.getName());

        sqLiteHelper = new SQLiteHelper(getApplicationContext());

        //Set image to Url
        String url = todo.getUrlImagePreview();
        String[] files = url.split("/");
        String fileName = files[2];
        File folder = new File(getApplicationContext().getCacheDir(), files[1]);
        if (!folder.exists()) folder.mkdir();
        final File file = new File(folder, fileName);
        if (file.exists()) {
            Picasso.get().load(file).into(imagePreview);
            imageUri = (Uri.fromFile(file));
        } else {
            storage.getReference().child(todo.getUrlImagePreview()).getFile(file)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Picasso.get().load(file).into(imagePreview);
                            imageUri = (Uri.fromFile(file));
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        if (v == edit) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(pYear, pMonth, pDay, pHour, pMinute, 0);
            timeTimeLong = calendar.getTimeInMillis();

            calendar.setTimeInMillis(timeTimeLong);
            pYear = calendar.get(Calendar.YEAR);
            pMonth = calendar.get(Calendar.MONTH);
            pDay = calendar.get(Calendar.DAY_OF_MONTH);
            pHour = calendar.get(Calendar.HOUR_OF_DAY);
            pMinute = calendar.get(Calendar.MINUTE);

            Toast.makeText(getApplicationContext(), "Time:" + pYear + "/" + pMonth + "/" + pDay + "/" + pMinute + "/" + pHour, Toast.LENGTH_SHORT).show();

            Todo todo = setTodo();
            repoFireStoreData.updateTodo(todo, new OnFirebaseUpdate() {
                @Override
                public void updateSqlite(Todo todo) {
                    uploadImageToFireBase();
                    long addedId = sqLiteHelper.updateTodo(todo);
                    if (addedId >= 0) {
                        Toast.makeText(getApplicationContext(), "Inserted " + addedId, Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(getApplicationContext(), "Inserted failed", Toast.LENGTH_SHORT).show();
                    }
                    //Thông báo cho các cái đã đăng kí lắng nghe là "data" vừa bị tác động đó để cho các cái đã đăng kí
                    // làm j đó nhưu ở đây là update lại dữ liệu mới nhất
                    ObserverManager.getInstance().data.setValue((int) 1);
                }
            });
            finish();
        }

        if (v == btnDate) {
            Calendar c = Calendar.getInstance();
            Dialog datePickerDialog = new DatePickerDialog(EditTodoActivity.this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                    String dateString = day + "/" + month + "/" + year;
                    pDay = day;
                    pMonth = month;
                    pYear = year;
                    txtDate.setText(dateString);
                }
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        }

        if (v == btnHour) {
            Calendar c = Calendar.getInstance();
            Dialog timePickerDialog = new TimePickerDialog(EditTodoActivity.this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    String timeString = hourOfDay + ":" + minute;
                    txtHour.setText(timeString);
                    pHour = hourOfDay;
                    pMinute = minute;
                }
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
            timePickerDialog.show();
        }
    }

    public void uploadImageToFireBase() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        if (imageUri != null && urlFirebase != null) {
            UploadTask task = storage.getReference().child(urlFirebase).putFile(imageUri);
            // hien ra dialog
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
                    Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }



    public void addItemSpinner() {
        String[] list = new String[]{"Rất quan trọng", "quan trọng", "bình thường"};
        ArrayAdapter<String> data = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, list);
        data.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        listPriority.setAdapter(data);
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            try {
                imageUri = data.getData();
                final InputStream imageStream = getApplicationContext().getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                imagePreview.setImageBitmap(selectedImage);

                Cursor returnCursor =
                        getApplicationContext().getContentResolver().query(imageUri, null, null, null, null);
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                returnCursor.moveToFirst();
                urlFirebase = "/todo/" + returnCursor.getString(nameIndex);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG).show();
            }

        } else {
            Toast.makeText(getApplicationContext(), "You haven't picked Image", Toast.LENGTH_LONG).show();
        }
    }

    public Todo setTodo() {
        //với edit thì mình ko tạo new Todo vì todo mình nhận có sẵn dữ liệu r , mình update 1 số atribute thôi
        todo.setName(txtName.getText().toString());
        if (urlFirebase != null && imageUri != null)
            todo.setUrlImagePreview(urlFirebase);
        todo.setPriority(listPriority.getSelectedItemPosition());
        todo.setDateTask(timeTimeLong);
        todo.setRequestCode(sqLiteHelper.getMaxRequestCode() + 1);
        todo.setUserID(FirebaseAuth.getInstance().getUid());
        return todo;
    }

}

