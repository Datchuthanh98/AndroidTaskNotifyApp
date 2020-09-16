package com.example.tasknotify.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.example.tasknotify.R;
import com.example.tasknotify.adapter.AdapterFragment;
import com.example.tasknotify.auth.LoginActivity;
import com.example.tasknotify.repository.SQLiteHelper;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private SQLiteHelper sqLiteHelper;
    private boolean isManager;
    private SharedPreferences sharedPref;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);
        sharedPref = getApplicationContext().getSharedPreferences("sessionUser", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPref.edit();
        sqLiteHelper = new SQLiteHelper(getApplicationContext());

        String userID = sharedPref.getString("idUser", "");
        //Check permission
        firestore.collection("User").document(userID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    isManager=false;
                }else {
                    editor.putString("idUser","Manager");
                    editor.apply();
                    isManager=true;
                }
                TabLayout tabLayout = findViewById(R.id.tablayout);
                ViewPager viewPager = findViewById(R.id.viewpager);
                //Set Animation
                viewPager.setPageTransformer(true, new HorizontalFlipTransformation());
                FragmentManager manager = getSupportFragmentManager();
                AdapterFragment adapter = new AdapterFragment(manager, AdapterFragment.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, isManager);
                viewPager.setAdapter(adapter);
                tabLayout.setupWithViewPager(viewPager);
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mymenu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.mLogout:
                FirebaseAuth.getInstance().signOut();
                sqLiteHelper.resetTableTodo();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                break;
            case R.id.mPhone:
                Toast.makeText(getApplicationContext(),
                        "Phone",Toast.LENGTH_LONG).show();
                break;
            case R.id.mEmail:
                Toast.makeText(getApplicationContext(),
                        "Email",Toast.LENGTH_LONG).show();
                break;
            case R.id.mExit:
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}