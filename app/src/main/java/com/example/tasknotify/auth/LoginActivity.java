package com.example.tasknotify.auth;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tasknotify.R;
import com.example.tasknotify.main.MainActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class LoginActivity extends AppCompatActivity {
    private Button btnEmail;
    private  Button btnRegister;
    EditText txtEmail,txtPassword;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPref;
    private Dialog loginLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        //auto login , neu co token cua user sẵn rồi thì vào luôn màn hình chính
        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        }

       //khởi tạo sharePre....
        sharedPref = getApplicationContext().getSharedPreferences("sessionUser", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPref.edit();

        mAuth = FirebaseAuth.getInstance();
        //Email
        txtEmail=findViewById(R.id.txtEmail);
        txtPassword=findViewById(R.id.txtPassword);
        btnEmail=findViewById(R.id.loginEmail);



        btnEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(txtEmail.getText().toString())){
                    Toast.makeText(LoginActivity.this, "Pleas Enter Email Address", Toast.LENGTH_SHORT).show();
                }
                else if(!Patterns.EMAIL_ADDRESS.matcher(txtEmail.getText().toString()).matches()){
                    Toast.makeText(LoginActivity.this, "Pleas Enter valid Email Address", Toast.LENGTH_SHORT).show();
                }
                else  if(TextUtils.isEmpty(txtPassword.getText().toString())){
                    Toast.makeText(LoginActivity.this, "Pleas Enter Password", Toast.LENGTH_SHORT).show();
                }
                else if(txtPassword.getText().toString().length()<6){
                    Toast.makeText(LoginActivity.this, "Pleas Enter 6 or more than digit password", Toast.LENGTH_SHORT).show();
                }
                else {
                      //tạo trạng thái loading . con mèo xoay xoay
                    loginLoading = new Dialog(LoginActivity.this);
                    loginLoading.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    loginLoading.setContentView(R.layout.custom_loading_layout);
                    loginLoading.setCancelable(false);
                    loginLoading.show();

                   // firebaseAuthWithEmail();
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(txtEmail.getText().toString(),txtPassword.getText().toString())
                            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    FirebaseUser user = authResult.getUser();
                                    Toast.makeText(LoginActivity.this, "Login Successful....", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    //add idUser login vào shareprefercce để dùng trong app
                                    editor.putString("idUser",user.getUid());
                                    editor.apply();
                                    //hủy cái con mèo đi
                                    loginLoading.cancel();
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(LoginActivity.this, "Failed :"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    loginLoading.cancel();
                                }
                            });
                }
            }
        });

        btnRegister=findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
