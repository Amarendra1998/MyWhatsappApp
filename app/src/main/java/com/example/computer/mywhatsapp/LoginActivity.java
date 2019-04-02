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

public class LoginActivity extends AppCompatActivity {
    private FirebaseUser firebaseUser;
    private EditText mailedit,passedit;
    private TextView mforget,mcreatenew;
    private Button logbtn,phonebtn;
    private FirebaseAuth mauth;
    String currentuserid;
    private DatabaseReference UserRef;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initializefields();
        mauth = FirebaseAuth.getInstance();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        firebaseUser = mauth.getCurrentUser();
        mcreatenew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToRegisterActivity();
            }
        });
        logbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllowUsertoLogin();
            }
        });

        phonebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,PhoneLoginActivity.class);
                startActivity(intent);
            }
        });
    }
    private void AllowUsertoLogin() {
        String email = mailedit.getText().toString();
        String password = passedit.getText().toString();
        if (TextUtils.isEmpty(email)){
            Toast.makeText(this,"Please provide email...",Toast.LENGTH_LONG).show();
        }
        if (TextUtils.isEmpty(password)){
            Toast.makeText(this,"Please provide password...",Toast.LENGTH_LONG).show();
        }
        else {
            progressDialog.setTitle("Logging in...");
            progressDialog.setMessage("Please wait, process is under construction...");
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.show();
            mauth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        String currentuserid = mauth.getCurrentUser().getUid();
                        String devicetoken = FirebaseInstanceId.getInstance().getToken();
                        UserRef.child(currentuserid).child("device_token").setValue(devicetoken)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful())
                                        {
                                            SendUserToMainActivity();
                                            progressDialog.dismiss();
                                            Toast.makeText(LoginActivity.this,"Login successfully",Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });

                    }else {
                        String message = task.getException().toString();
                        Toast.makeText(LoginActivity.this,"Error:"+message,Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                }
            });
        }
    }
    private void initializefields() {
        mailedit = (EditText)findViewById(R.id.mailedit);
        passedit = (EditText)findViewById(R.id.passedit);
        mforget = (TextView)findViewById(R.id.mforget);
        mcreatenew= (TextView)findViewById(R.id.mcreate);
        logbtn = (Button)findViewById(R.id.lobtn);
        phonebtn = (Button)findViewById(R.id.logbtn);
        progressDialog = new ProgressDialog(this);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        if (firebaseUser!=null){
            SendUserToMainActivity();
        }
    }

    private void SendUserToMainActivity() {
        Intent loginInt = new Intent(LoginActivity.this,MainActivity.class);
        loginInt.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginInt);
        finish();
    }

    private void SendUserToRegisterActivity() {
        Intent loginInt = new Intent(LoginActivity.this,RegisterActivity.class);
        loginInt.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginInt);
    }
}
