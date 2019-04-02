package com.example.computer.mywhatsapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.DocumentsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {
    private EditText muser,mstatus;
    private Button mupdate;
    private FirebaseUser mCurrentUser;
    private FirebaseAuth mauth;
    FirebaseStorage storage;
    private CircleImageView circleImageView;
    private DatabaseReference mRoot;
    private String CurrentUserId;
    private static final int GalleryPick = 1;
    private StorageReference UserProfileImageRef;
    private ProgressDialog loadingbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        mauth = FirebaseAuth.getInstance();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        CurrentUserId = mauth.getCurrentUser().getUid();
        mRoot = FirebaseDatabase.getInstance().getReference();
        Initializefields();
        mupdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatesettings();
            }
        });
        Retrieveinfo();
        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GalleryPick);
                //uploadImage();
            }
        });
    }
    private void Retrieveinfo(){
        mRoot.child("Users").child(CurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists())&&(dataSnapshot.hasChild("name")&&(dataSnapshot.hasChild("image")))){
                    String retrieveusername = dataSnapshot.child("name").getValue().toString();
                    String retrievestatus = dataSnapshot.child("status").getValue().toString();
                    String retrieveprofile = dataSnapshot.child("image").getValue().toString();
                    muser.setText(retrieveusername);
                    mstatus.setText(retrievestatus);
                    Picasso.get().load(retrieveprofile).into(circleImageView);
                }else if ((dataSnapshot.exists())&&(dataSnapshot.hasChild("name"))){
                    String retrieveusername = dataSnapshot.child("name").getValue().toString();
                    String retrievestatus = dataSnapshot.child("status").getValue().toString();
                    muser.setText(retrieveusername);
                    mstatus.setText(retrievestatus);
                }
                else{
                    Toast.makeText(SettingsActivity.this,"Please enter your status...",Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null) {
            Uri downloadurls = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
            }
            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    loadingbar.setTitle("Set Profile Image");
                    loadingbar.setMessage("Please wait your profile image is updating...");
                    loadingbar.setCanceledOnTouchOutside(false);
                    loadingbar.show();
                    final Uri resulturi = result.getUri();
                    final StorageReference filepath = UserProfileImageRef.child(CurrentUserId + ".jpg");
                    if (resulturi != null) {
                        filepath.putFile(resulturi)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        filepath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Uri> task) {
                                                String profileImageUrl = task.getResult().toString();
                                                mRoot.child("Users").child(CurrentUserId).child("image").setValue(profileImageUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            loadingbar.dismiss();
                                                            Toast.makeText(SettingsActivity.this, "Successfully added in databse.", Toast.LENGTH_LONG).show();
                                                        } else {
                                                            Toast.makeText(SettingsActivity.this, "Error in uploading image.", Toast.LENGTH_LONG).show();
                                                            loadingbar.dismiss();
                                                        }
                                                    }
                                                });
                                                Log.i("URL", profileImageUrl);
                                            }
                                        });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        loadingbar.dismiss();
                                        Toast.makeText(SettingsActivity.this, "aaa " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }
            }
        }

    private void updatesettings() {
        String setusername = muser.getText().toString();
        String setstatus = mstatus.getText().toString();
        if (TextUtils.isEmpty(setusername)){
            Toast.makeText(SettingsActivity.this,"Please write your username...",Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(setstatus)){
            Toast.makeText(SettingsActivity.this,"Please enter your status...",Toast.LENGTH_SHORT).show();
        }
        else {
            Map<String,Object>profilemap =new HashMap<>();
            profilemap.put("uid",CurrentUserId);
            profilemap.put("name",setusername);
            profilemap.put("status",setstatus);
             mRoot.child("Users").child(CurrentUserId).updateChildren(profilemap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        SendUserToMainActivity();
                        Toast.makeText(SettingsActivity.this, "Profile Updated Successfully..", Toast.LENGTH_SHORT).show();
                    } else {
                        String message = task.getException().toString();
                        Toast.makeText(SettingsActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
    private void Initializefields() {
        muser= (EditText)findViewById(R.id.editText);
        mstatus= (EditText)findViewById(R.id.editText2);
        mupdate= (Button) findViewById(R.id.updbtn);
        circleImageView = (CircleImageView)findViewById(R.id.profile_image);
        loadingbar=new ProgressDialog(this);
    }
    private void SendUserToMainActivity() {
        Intent loginInt = new Intent(SettingsActivity.this,MainActivity.class);
        loginInt.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginInt);
        finish();
    }
}
