package com.practice.dropit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageActivity extends AppCompatActivity {

    TextView textViewOtherUserName;
    EditText editTextMessage;
    ImageView backButton;
    FloatingActionButton fabSend;
    RecyclerView recyclerView;

    String currentUser;
    String otherUser;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = database.getReference();

    MessageAdapter adapter;
    List<ModelClass> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        textViewOtherUserName = findViewById(R.id.textViewMessageUser);
        editTextMessage = findViewById(R.id.editTextMessage);
        backButton = findViewById(R.id.imageViewBack);
        fabSend = findViewById(R.id.floatingActionButtonSend);
        recyclerView = findViewById(R.id.recyclerViewMessage);

        //I will get the users from the intent from the ChatActivityAdapter
        otherUser = getIntent().getStringExtra("otherusername");
        currentUser = getIntent().getStringExtra("currentusername");
        textViewOtherUserName.setText(otherUser);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        list = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        fabSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userMessage = editTextMessage.getText().toString();
                if(!editTextMessage.equals("")){
                    sendMessage(userMessage);
                    editTextMessage.setText("");
                }
            }
        });

        databaseReference.child("message").child(currentUser).child(otherUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                adapter = new MessageAdapter(list, currentUser);
                getMessage();
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    public void sendMessage(String message){
        //I will need a key for a dialogue between two users, so as they can both access the the saved messages sent between themselves from the database.
        String key = databaseReference.child("messages").child(currentUser).child(otherUser).push().getKey();
        //Now I used a Map to store the message and sender
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("message", message);
        messageMap.put("from", currentUser);

        databaseReference.child("messages").child(currentUser).child(otherUser).child(key).setValue(messageMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //so I will also store the map in the other receiver directory too ,in the database
                        databaseReference.child("messages").child(otherUser).child(currentUser).child(key).setValue(messageMap);
                    }
                });
    }

    public void getMessage(){

        databaseReference.child("messages").child(currentUser).child(otherUser).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                 ModelClass modelClass =  snapshot.getValue(ModelClass.class);
                 list.add(modelClass);
                 adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(list.size()-1); // we scroll the recyclerview to show the latest message.
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}