package com.practice.dropit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class SignUpActivity extends AppCompatActivity {

    ImageView profilePic;
    TextInputEditText email, password, username;
    Button signUp;
    Toolbar toolbar;
    boolean imageControl = false; //we will use this value to check whether the user picked a photo.

    Uri imageUri;// this will be the image path on phone

    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference reference = database.getReference();

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        profilePic = findViewById(R.id.circleImageView);
        email = findViewById(R.id.editTextSignUpEmail);
        password = findViewById(R.id.editTextSignUpPassword);
        username = findViewById(R.id.editTextSignUpUsername);
        signUp = findViewById(R.id.buttonSignUp);
        toolbar = findViewById(R.id.toolbarSignUp);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageChooser();
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userEmail = email.getText().toString();
                String userPassword = password.getText().toString();
                String userName = username.getText().toString();

                if (!userEmail.equals("") && !userPassword.equals("") && !userName.equals("")){
                    signUpMethod(userEmail, userPassword, userName);
                }else{
                    Toast.makeText(SignUpActivity.this, "Please input all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    public void signUpMethod(String userEmail, String userPassword, String userName){

        auth.createUserWithEmailAndPassword(userEmail, userPassword)
            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(SignUpActivity.this, "signing up was successful", Toast.LENGTH_SHORT).show();
                        //I stored the username in under the user's ID
                        reference.child("Users").child(auth.getUid()).child("username").setValue(userName);

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
                                            reference.child("Users").child(auth.getUid()).child("image").setValue(userFilePath)
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(SignUpActivity.this, "Unable to upload profile to database", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                    });

                                }
                            });

                        }else {
                            //if he didnt pick a picture, we will store null in database
                            reference.child("Users").child(auth.getUid()).child("image").setValue("null");
                        }

                        Intent i = new Intent(SignUpActivity.this,LoginActivity.class);
                        i.putExtra("username", userName);
                        startActivity(i);
                        finish();

                    } else {
                        Toast.makeText(SignUpActivity.this, "could not signup at the moment", Toast.LENGTH_SHORT).show();
                    }
                }
            });

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