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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tasknotify.Interface.OnFirebaseInsert;
import com.example.tasknotify.Interface.UserConnectFirebase;
import com.example.tasknotify.R;
import com.example.tasknotify.main.ObserverManager;
import com.example.tasknotify.model.Todo;
import com.example.tasknotify.model.User;
import com.example.tasknotify.repository.RepoFireStoreData;
import com.example.tasknotify.repository.SQLiteHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class FragmentManageAdd extends Fragment implements View.OnClickListener {
    private TextView txtName, txtDate, txtHour;
    private String urlFirebase = "";
    private ImageView imagePreview;
    private Spinner listPriority, listUser;
    private Button add;
    private SQLiteHelper sqLiteHelper;
    public static int RESULT_LOAD_IMG = 1012;
    private Dialog uploadingDialog;
    private ImageButton btnDate, btnHour;
    Uri imageUri;
    private RepoFireStoreData repoFireStoreData = RepoFireStoreData.getInstance();
    private int pYear, pMonth, pDay, pHour, pMinute;
    long timeTimeLong;
    private List<User> listDataUser= new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.add_task_fragment_manager, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sqLiteHelper = new SQLiteHelper(getContext());
        txtName = view.findViewById(R.id.txtName);
        txtDate = view.findViewById(R.id.txtDate);
        txtHour = view.findViewById(R.id.txtHour);
        btnDate = view.findViewById(R.id.btnPickDate);
        btnHour = view.findViewById(R.id.btnPickHour);
        listPriority = view.findViewById(R.id.listPriority);
        listUser = view.findViewById(R.id.listUser);
        imagePreview = view.findViewById(R.id.imgPreview);

        uploadingDialog = new Dialog(getContext());
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

        add = view.findViewById(R.id.btnAdd);

        add.setOnClickListener(this);
        btnDate.setOnClickListener(this);
        btnHour.setOnClickListener(this);

        addSpinner();


    }

    @Override
    public void onClick(View v) {

        if (v == add) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(pYear, pMonth, pDay, pHour, pMinute, 0);
            timeTimeLong = calendar.getTimeInMillis();
            Todo todo = setTodo();
            repoFireStoreData.insertTodo(todo, new OnFirebaseInsert() {
                @Override
                public void insertSqlite(Todo todo) {
                    uploadImageToFireBase();
                    long addedId = sqLiteHelper.insertTodo(todo);
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
            Dialog timePickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
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


    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            try {
                imageUri = data.getData();
                final InputStream imageStream = getContext().getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                imagePreview.setImageBitmap(selectedImage);

                Cursor returnCursor =
                        getContext().getContentResolver().query(imageUri, null, null, null, null);
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                returnCursor.moveToFirst();
                urlFirebase = "/todo/" + returnCursor.getString(nameIndex);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_LONG).show();
            }

        } else {
            Toast.makeText(getContext(), "You haven't picked Image", Toast.LENGTH_LONG).show();
        }
    }



    public void addSpinner(){

        String[] list = new String[]{"Rất quan trọng", "quan trọng", "bình thường"};
        ArrayAdapter<String> data = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, list);
        data.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        listPriority.setAdapter(data);

        //Set spinner
        repoFireStoreData.loadUser(new UserConnectFirebase() {
            @Override
            public void onPlayerLoadedFromFireBase(List<User> users) {
                listDataUser = users;
                String[] listname = new String[listDataUser.size()];
                for (int i = 0; i < listname.length; i++) {
                    listname[i] = listDataUser.get(i).getName();
                }
                ArrayAdapter<String> data2 = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, listname);
                data2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                listUser.setAdapter(data2);
            }
        });
    }
    public void uploadImageToFireBase() {
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
        Todo todo = new Todo();
        todo.setName(txtName.getText().toString());
        todo.setUrlImagePreview(urlFirebase);
        todo.setPriority(listPriority.getSelectedItemPosition());
        todo.setDateTask(timeTimeLong);
        todo.setUserID(listDataUser.get(listUser.getSelectedItemPosition()).getId());
        return todo;
    }

}
