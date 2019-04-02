package com.example.computer.mywhatsapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseUser firebaseUser;
    private EditText mailedit,passedit;
    private TextView mlogin;
    private Button mregbtn;
    private FirebaseAuth mauth;
    private DatabaseReference mRoot;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mauth = FirebaseAuth.getInstance();
        initializefields();
        mRoot = FirebaseDatabase.getInstance().getReference();
        mlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToLoginActivity();
            }
        });

        mregbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createaccount();
            }
        });
    }
    private void createaccount() {
        String email = mailedit.getText().toString();
        String password = passedit.getText().toString();
        if (TextUtils.isEmpty(email)){
            Toast.makeText(this,"Please provide email...",Toast.LENGTH_LONG).show();
        }
        if (TextUtils.isEmpty(password)){
            Toast.makeText(this,"Please provide password...",Toast.LENGTH_LONG).show();
        }
        else {
            progressDialog.setTitle("Creating new account");
            progressDialog.setMessage("Please wait, process is under construction...");
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.show();
            mauth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        String devicetoken = FirebaseInstanceId.getInstance().getToken();
                        String currentId = mauth.getCurrentUser().getUid();
                        mRoot.child("Users").child(currentId).setValue("");
                        mRoot.child("Users").child(currentId).child("device_token").setValue(devicetoken);
                        SendUserToMainActivity();
                        Toast.makeText(RegisterActivity.this,"Sighed up successfully",Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }else {
                        String message = task.getException().toString();
                        Toast.makeText(RegisterActivity.this,"Error:"+message,Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                }
            });
        }
    }
    private void SendUserToMainActivity() {
        Intent loginInt = new Intent(RegisterActivity.this,MainActivity.class);
        loginInt.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginInt);
        finish();
    }

    private void initializefields() {
        mailedit = (EditText)findViewById(R.id.mailedit);
        passedit = (EditText)findViewById(R.id.passedit);
        mlogin= (TextView)findViewById(R.id.mlog);
        mregbtn = (Button)findViewById(R.id.regbtn);
        progressDialog = new ProgressDialog(this);
    }
    private void SendUserToLoginActivity() {
         Intent loginInt = new Intent(RegisterActivity.this,LoginActivity.class);
         loginInt.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginInt);
    }
}
