package com.example.tasknotify.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.tasknotify.Interface.OnFirebaseDelete;
import com.example.tasknotify.Interface.OnFirebaseInsert;
import com.example.tasknotify.Interface.OnFirebaseUpdate;
import com.example.tasknotify.Interface.TodoConnectFirebase;
import com.example.tasknotify.Interface.UserConnectFirebase;
import com.example.tasknotify.model.Todo;
import com.example.tasknotify.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepoFireStoreData {
    static RepoFireStoreData instance;
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    public static RepoFireStoreData getInstance() {
        if (instance == null) {
            instance = new RepoFireStoreData();
        }
        return instance;
    }

    //Firestore realtime
    public void loadListTodo(String uid, final TodoConnectFirebase todoConnectFirebase) {
        if (uid.equals("Manager")) {
            db.collection("Todo").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    List<Todo> todos = new ArrayList<>();
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            Todo p = new Todo();
                            p.setName((String) document.get("name"));
                            p.setId(document.getId());
                            p.setDateTask(document.getLong("dateTask"));
                            p.setPriority(document.getLong("priority"));
                            p.setUrlImagePreview((String) document.get("urlImagePreview"));
                            p.setUserID((String) document.get("userID"));
                            todos.add(p);
                        }
                        todoConnectFirebase.onPlayerLoadedFromFireBase(todos);
                    } else {
                        Log.d("meomeo", "failed");
                    }
                }
            });
        } else {
            db.collection("Todo").whereEqualTo("userID", uid).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    List<Todo> todos = new ArrayList<>();
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            Todo p = new Todo();
                            p.setName((String) document.get("name"));
                            p.setId(document.getId());
                            p.setDateTask(document.getLong("dateTask"));
                            p.setPriority(document.getLong("priority"));
                            p.setUrlImagePreview((String) document.get("urlImagePreview"));
                            p.setUserID((String) document.get("userID"));
                            todos.add(p);
                        }
                        todoConnectFirebase.onPlayerLoadedFromFireBase(todos);
                    } else {
                        Log.d("meomeo", "failed");
                    }
                }


            });
        }
    }

    //Firestore realtime
    public void loadUser(final UserConnectFirebase userConnectFirebase) {
        db.collection("User").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<User> todos = new ArrayList<>();
                if (!queryDocumentSnapshots.isEmpty()) {
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        User p = new User();
                        p.setName((String) document.get("name"));
                        p.setId(document.getId());
                        todos.add(p);
                    }
                    userConnectFirebase.onPlayerLoadedFromFireBase(todos);
                } else {
                    Log.d("meomeo", "failed");
                }
            }
        });

    }




    public void updateTodo(final Todo todo, final OnFirebaseUpdate onFirebaseUpdate) {
        DocumentReference newPlayer = db.collection("Todo").document(todo.getId());
        newPlayer.set(todo).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    onFirebaseUpdate.updateSqlite(todo);
                }
            }
        });
    }

    public void insertTodo(final Todo todo, final OnFirebaseInsert onFirebaseInsert) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", todo.getName());
        map.put("dateTask", todo.getDateTask());
        map.put("priority", todo.getPriority());
        map.put("urlImagePreview", todo.getUrlImagePreview());
        map.put("userID", todo.getUserID());
        db.collection("Todo").add(map).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                todo.setId(documentReference.getId());
                onFirebaseInsert.insertSqlite(todo);
            }
        });
    }

    public void insertUser(final User user) {
        db.collection("User").document(user.getId()).set(user);
    }

    public void deleteTodo(final String id, final OnFirebaseDelete onFirebaseDelete) {
        db.collection("Todo").document(id).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                onFirebaseDelete.deleteSqlite(id);
            }
        });
    }

}
