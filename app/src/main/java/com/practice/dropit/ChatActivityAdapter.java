package com.practice.dropit;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;


import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivityAdapter extends RecyclerView.Adapter<ChatActivityAdapter.MyViewHolder> {

    List<String> userList;
    Context context;
    String currentUserName;

    //we will bind to the image and textview values from the firebase database.
    FirebaseDatabase database;
    DatabaseReference reference;

    public ChatActivityAdapter(List<String> userList, String currentUserName, Context context) {
        this.userList = userList;
        this.context = context;
        this.currentUserName = currentUserName;

        database = FirebaseDatabase.getInstance();
        reference = database.getReference();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_user_design, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        reference.child("Users").child(userList.get(position)).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                       String otherUserName =  snapshot.child("username").getValue().toString();
                        String imageURL = snapshot.child("image").getValue().toString();

                        holder.username.setText(otherUserName);
                        if (imageURL.equals("null")) {
                            holder.user_pfp.setImageResource(R.drawable.profile_photo);
                        }else{
                            Picasso.get().load(imageURL).into(holder.user_pfp);
                        }
                        holder.user_bg.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent i = new Intent(context, MessageActivity.class);
                                i.putExtra("currentusername", currentUserName);
                                i.putExtra("otherusername", otherUserName);
                                context.startActivity(i);

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        ConstraintLayout user_bg;
        TextView username;
        CircleImageView user_pfp;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            user_bg = itemView.findViewById(R.id.constraintLytChatUser_bg);
            username = itemView.findViewById(R.id.textViewUserChatActivity);
            user_pfp = itemView.findViewById(R.id.imageViewChatActivity);
        }
    }

}
