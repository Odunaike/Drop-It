package com.practice.dropit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetPasswordActivity extends AppCompatActivity {

    Toolbar toolbar;
    EditText email;
    Button buttonForget;

    FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        email = findViewById(R.id.editTextForgotEmail);
        buttonForget = findViewById(R.id.buttonForget);
        toolbar = findViewById(R.id.toolbarForgetPass);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        buttonForget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userEmail = email.getText().toString();
                resetPassword(userEmail);
            }
        });

    }

    public void resetPassword(String email){
        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(ForgetPasswordActivity.this, "Check your mail for the link", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(ForgetPasswordActivity.this, "Sorry, try again later", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}