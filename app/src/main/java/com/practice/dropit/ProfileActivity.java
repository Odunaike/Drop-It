package com.practice.dropit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    TextInputEditText editTextUsername;
    CircleImageView profilePic;
    Toolbar toolbar;

    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = database.getReference();
    FirebaseUser user = auth.getCurrentUser();

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference();

    Uri imageUri; //this is the path of the image the user picked
    boolean imageControl = false; //we will use this value to check whether the user picked a photo.

    String userProfilePicURI;  //this is the value of the image child of a user parent  in the database.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        toolbar = findViewById(R.id.toolbarProfile);
        editTextUsername = findViewById(R.id.editTextUpdateUsername);
        profilePic = findViewById(R.id.imageViewUpdateProfile);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getUserInfo();

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageChooser();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_save:
                updateProfile();
        }
        return super.onOptionsItemSelected(item);
    }

    //this method  gets the users info ND DISPLAYS IT WHEN HE/SHE ENTERS THIS ACTIVITY.
    public void getUserInfo(){
        databaseReference.child("Users").child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userProfilePicURI = snapshot.child("image").getValue().toString();
                String userName = snapshot.child("username").getValue().toString();
                editTextUsername.setText(userName);  // We print the current username
                //Now we check if the user uploaded a photo or not
                if(userProfilePicURI.equals("null")){
                    profilePic.setImageResource(R.drawable.profile_photo);  //I use the default photo when he had not uploaded before
                }else{
                    Picasso.get().load(userProfilePicURI).into(profilePic);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public  void  updateProfile(){
        String newUserName = editTextUsername.getText().toString();
        databaseReference.child("Users").child(user.getUid()).child("username").setValue(newUserName);

        //so we check if the user picked a photo so we can store it
        if(imageControl){

            //I will give a unique name to the location where the userimage will be saved in the cloud storage
            String imagePath = "images/"+auth.getUid()+".jpg";
            storageReference.child(imagePath).putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //on successful upload, we will get a reference to the image
                    StorageReference userImageRef = storageReference.child(imagePath);
                    //I want to get the cloud storage path in strings
                    userImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {//the uri object represent the url
                            String userFilePath = uri.toString();
                            //so we store in the database the name of the cloud storage path. So it shows null when the user doesnt pick a picture and shows the cloudstorage path url when he picks a picture
                            databaseReference.child("Users").child(auth.getUid()).child("image").setValue(userFilePath)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(ProfileActivity.this, "your profile has been updated", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(ProfileActivity.this, "Unable update profile", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    });

                }
            });

        }else {
            //if he didnt pick a picture, we will keep the old value there in database, maybe the user doesn;t want to update his picture
            databaseReference.child("Users").child(auth.getUid()).child("image").setValue(userProfilePicURI);
        }

        finish();
    }

    public void imageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1 && data != null ){
            imageUri = data.getData();
            Picasso.get().load(imageUri).into(profilePic);
            imageControl = true;
        }else{
            imageControl = false;
        }
    }

}