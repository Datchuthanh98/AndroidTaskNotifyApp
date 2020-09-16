package com.example.tasknotify.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tasknotify.R;
import com.example.tasknotify.model.User;
import com.example.tasknotify.repository.RepoFireStoreData;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity  extends AppCompatActivity {

    private Button btnBack;
    private  Button btnRegister;
    EditText txtEmail,txtPassword,txtName;
    private RepoFireStoreData repoFireStoreData = RepoFireStoreData.getInstance();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        txtEmail=findViewById(R.id.txtEmail);
        txtPassword=findViewById(R.id.txtPassword);
        txtName=findViewById(R.id.txtNameAccount);
        btnBack=findViewById(R.id.btnBack);

        btnRegister=findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Đăng kí tài khoản thì lưu account vào firebase auth và cả database
                // đăng kí chỉ dùng cho staff , tài khoản manager không tạo đc nên tài khoản manager chỉ
                // có trong filebare auth chứ ko có trong database
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(txtEmail.getText().toString(),txtPassword.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        User user= new User();
                        user.setName(txtName.getText().toString());
                        user.setPassword(txtPassword.getText().toString());
                        user.setEmail(txtEmail.getText().toString());
                        user.setId(authResult.getUser().getUid());
                        repoFireStoreData.insertUser(user);
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });

            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
}
