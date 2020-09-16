package com.example.tasknotify.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tasknotify.R;
import com.example.tasknotify.adapter.RecycleViewAdapterTodo;
import com.example.tasknotify.main.ObserverManager;
import com.example.tasknotify.main.SyncTodoFirebase;
import com.example.tasknotify.model.Todo;
import com.example.tasknotify.repository.SQLiteHelper;

import java.util.List;

public class FragmentListTodo extends Fragment {
    private RecyclerView recyclerView;
    private EditText txtName;
    private SQLiteHelper sqLiteHelper;
   private Button btnSync;
   private SyncTodoFirebase syncTodoFirebase =  SyncTodoFirebase.getInstance();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_task_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sqLiteHelper = new SQLiteHelper(getContext());
        txtName = view.findViewById(R.id.txtName);
        btnSync =view.findViewById(R.id.btnSync);
        recyclerView = view.findViewById(R.id.recycleViewListCardTodo);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        //Khởi tạo màn hình ban đầu của fragment
        List<Todo> tasks = sqLiteHelper.getAllTodos();
        RecycleViewAdapterTodo adapter = new RecycleViewAdapterTodo(tasks);
        recyclerView.setAdapter(adapter);

        //Lắng nghe việc crud todo
        ObserverManager.getInstance().data.observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
               //Thực hiện làm j đó khi  lắng nghe đc "data" bị tác động/thay đổi
                // cập nhập lại listTodo
                List<Todo> todos = sqLiteHelper.getAllTodos();
                RecycleViewAdapterTodo adapter = new RecycleViewAdapterTodo(todos);
                recyclerView.setAdapter(adapter);
            }
        });

        //lắng nghe tìm kiếm todo item thông qua tên
        ObserverManager.getInstance().nameSearh.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
               //cập nhập lại lisTodo có chưa kí tự cần tìm
               if(s.equals("")){
                   //nếu trống thì lấy về toàn bộ list
                   List<Todo> todos = sqLiteHelper.getAllTodos();
                   RecycleViewAdapterTodo adapter = new RecycleViewAdapterTodo(todos);
                   recyclerView.setAdapter(adapter);
               }else{
                   //nhánh đúng thì filter qua tên của todo đó
                   List<Todo> todos = sqLiteHelper.getListTodobyName(s);
                   RecycleViewAdapterTodo adapter = new RecycleViewAdapterTodo(todos);
                   recyclerView.setAdapter(adapter);
               }
            }
        });

        txtName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
               ObserverManager.getInstance().nameSearh.setValue(txtName.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        btnSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                syncTodoFirebase.synchronizeFireStore(getContext());
            }
        });

    }


}
