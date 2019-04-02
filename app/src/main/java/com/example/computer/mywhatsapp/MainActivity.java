package com.example.computer.mywhatsapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
private Toolbar mtoolbar;
private ViewPager myviewpager;
private TabLayout mytablayout;
private TabAccessorAdapter tabAccessorAdapter;
private FirebaseUser firebaseUser;
private String currentuserid;
    private FirebaseAuth mauth;
    private DatabaseReference Rootref;
    private static final int GalleryPick = 1;
    private StorageReference UserProfileImageRef;
    private ProgressDialog loadingbar;
    String groupname;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        Rootref = FirebaseDatabase.getInstance().getReference();
        mauth = FirebaseAuth.getInstance();
        mtoolbar = (Toolbar)findViewById(R.id.appbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("MyWhatsapp");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        myviewpager = (ViewPager)findViewById(R.id.viewpager);
        tabAccessorAdapter = new TabAccessorAdapter(getSupportFragmentManager());
        myviewpager.setAdapter(tabAccessorAdapter);

        mytablayout = (TabLayout)findViewById(R.id.mytab);
        mytablayout.setupWithViewPager(myviewpager);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onStart() {
        super.onStart();
        if (firebaseUser==null){
            SendUserToLoginActivity();
        }else{
            updateUserStatus("online");
            verifyuserexistence();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseUser!=null)
        {
            updateUserStatus("offline");

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (firebaseUser!=null)
        {
            updateUserStatus("offline");

        }
    }

    private void verifyuserexistence() {
        String currentuserid = mauth.getCurrentUser().getUid();
        Rootref.child("Users").child(currentuserid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.child("name").exists())){
                    Toast.makeText(MainActivity.this,"welcome",Toast.LENGTH_SHORT).show();
                }else {
                    SendUserToSettings();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.mymenu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId()==R.id.log_out){
            mauth.signOut();
            SendUserToLoginActivity();
        }
        if (item.getItemId()==R.id.settings){
            SendUserToSettings();
        }
        if (item.getItemId()==R.id.friends){
           SendUserToFindFriendsActivity();
        }
        if (item.getItemId()==R.id.creategroup){
          RequestForNewGroup();
        }
        if (item.getItemId()==R.id.bot){
            Intent intent = new Intent(MainActivity.this,SimSimiBotActivity.class);
            startActivity(intent);
        }
        return  true;
    }

    private void SendUserToFindFriendsActivity() {
        Intent intent = new Intent(MainActivity.this,FindFriendsActivity.class);
        startActivity(intent);
    }

    private void RequestForNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
        builder.setTitle("Enter Group Name");
        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("e.g my group");
        builder.setView(groupNameField);
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                 groupname = groupNameField.getText().toString();
                if (TextUtils.isEmpty(groupname)){
                    Toast.makeText(MainActivity.this,"Please write group name",Toast.LENGTH_SHORT).show();
                }else {
                     CreateNewGroup(groupname);

                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void CreateNewGroup(final String groupname) {
        Rootref.child("Group").child(groupname).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(MainActivity.this,groupname+"group is created successfully",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void SendUserToSettings() {
        Intent loginInt = new Intent(MainActivity.this,SettingsActivity.class);
        startActivity(loginInt);
    }
    private void SendUserToLoginActivity() {
        Intent intent = new Intent(MainActivity.this,LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateUserStatus(String state){
        String saveCurrentTime, saveCurrentDate;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentdate = new SimpleDateFormat("MMM,dd,yyyy");
        saveCurrentDate = currentdate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());
        HashMap<String,Object> onelineState = new HashMap<>();
        onelineState.put("time",saveCurrentTime);
        onelineState.put("date",saveCurrentDate);
        onelineState.put("state",state);
        currentuserid = mauth.getCurrentUser().getUid();
        Rootref.child("Users").child(currentuserid).child("userState").updateChildren(onelineState);
    }
}
