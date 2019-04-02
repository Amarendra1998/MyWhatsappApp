package com.example.computer.mywhatsapp;

import android.app.Notification;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
private String recieverid,current_state,currentuserid;
private CircleImageView circleImageView;
private TextView userProfileName,userProfileStatus;
private Button SendRequest,DeclineRequest;
private DatabaseReference UserRef,ChatRequestRef,ContactsRef,Notifications;
private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        recieverid = getIntent().getExtras().get("visit_user_id").toString();
        mAuth= FirebaseAuth.getInstance();
        currentuserid = mAuth.getCurrentUser().getUid();
        UserRef= FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestRef= FirebaseDatabase.getInstance().getReference().child("Chat Request");
        ContactsRef= FirebaseDatabase.getInstance().getReference().child("Contacts");
        Notifications = FirebaseDatabase.getInstance().getReference().child("Notifications");
        Toast.makeText(ProfileActivity.this,"User Id:"+recieverid,Toast.LENGTH_SHORT).show();
        circleImageView = (CircleImageView)findViewById(R.id.visit_profile_image);
        userProfileName =  (TextView)findViewById(R.id.visit_profile_name);
        userProfileStatus = (TextView)findViewById(R.id.visit_profile_status);
        SendRequest = (Button)findViewById(R.id.send_message_request_button);
        DeclineRequest = (Button)findViewById(R.id.decline_message_request_button);
        current_state = "new";
        retrieveinfo();
    }

    private void retrieveinfo() {
        UserRef.child(recieverid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists())&&(dataSnapshot.hasChild("image")))
                {
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();
                    Picasso.get().load(userImage).placeholder(R.drawable.ic_person_black_24dp).into(circleImageView);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);
                    ManageChatRequest();
                }else
                {
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);
                    ManageChatRequest();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void ManageChatRequest() {
        ChatRequestRef.child(currentuserid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(recieverid)) {
                    String request_type = dataSnapshot.child(recieverid).child("request_type").getValue().toString();
                    if (request_type.equals("sent")) {
                        current_state = "request_sent";
                        SendRequest.setText("Cancel Chat Request");
                    }else if (request_type.equals("received"))
                    {
                        current_state = "request_received";
                        SendRequest.setText("Accept Chat Request");
                        DeclineRequest.setVisibility(View.VISIBLE);
                        DeclineRequest.setEnabled(true);
                        DeclineRequest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CanelChatRequest();
                            }
                        });
                    }
                }else
                {
                    ContactsRef.child(currentuserid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(recieverid))
                            {
                                current_state="friends";
                                SendRequest.setText("Remove the Contact");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if (!currentuserid.equals(recieverid)) {
            SendRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SendRequest.setEnabled(false);

                    if (current_state.equals("new")) {
                        SendChatRequest();
                    }
                    if (current_state.equals("request_sent")) {
                        CanelChatRequest();
                    }
                    if (current_state.equals("request_received")) {
                        AcceptChatRequest();
                    }
                    if (current_state.equals("friends")) {
                        RemoveSpecificContacts();
                    }

                }
            });
        } else {
            SendRequest.setVisibility(View.INVISIBLE);
        }
    }

    private void RemoveSpecificContacts() {
        ContactsRef.child(currentuserid).child(recieverid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    ContactsRef.child(recieverid).child(currentuserid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            SendRequest.setEnabled(true);
                            current_state = "new";
                            SendRequest.setText("Send Message");
                            DeclineRequest.setVisibility(View.INVISIBLE);
                            DeclineRequest.setEnabled(false);
                        }
                    });
                }
            }
        });
    }

    private void AcceptChatRequest() {
     ContactsRef.child(currentuserid).child(recieverid).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
         @Override
         public void onComplete(@NonNull Task<Void> task) {
             if (task.isSuccessful())
             {
                 ContactsRef.child(recieverid).child(currentuserid).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                     @Override
                     public void onComplete(@NonNull Task<Void> task) {
                         if (task.isSuccessful())
                         {
                            ChatRequestRef.child(currentuserid).child(recieverid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                    {
                                        ChatRequestRef.child(recieverid).child(currentuserid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                               SendRequest.setEnabled(true);
                                               current_state = "friends";
                                               SendRequest.setText("Remove this Contact");
                                               DeclineRequest.setVisibility(View.INVISIBLE);
                                               DeclineRequest.setEnabled(false);
                                            }
                                        });
                                    }
                                }
                            });
                         }
                     }
                 });
             }
         }
     });
    }

    private void CanelChatRequest() {
        ChatRequestRef.child(currentuserid).child(recieverid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    ChatRequestRef.child(recieverid).child(currentuserid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            SendRequest.setEnabled(true);
                            current_state = "new";
                            SendRequest.setText("Send Message");
                            DeclineRequest.setVisibility(View.INVISIBLE);
                            DeclineRequest.setEnabled(false);
                        }
                    });
                }
            }
        });
    }

    private void SendChatRequest() {
     ChatRequestRef.child(currentuserid).child(recieverid).child("request_type").setValue("sent")
             .addOnCompleteListener(new OnCompleteListener<Void>() {
                 @Override
                 public void onComplete(@NonNull Task<Void> task) {
                     if (task.isSuccessful())
                     {
                         ChatRequestRef.child(recieverid).child(currentuserid).child("request_type").setValue("received")
                                 .addOnCompleteListener(new OnCompleteListener<Void>() {
                                     @Override
                                     public void onComplete(@NonNull Task<Void> task) {
                                         if (task.isSuccessful())
                                         {
                                             HashMap<String,String>chatnotifications = new HashMap<>();
                                             chatnotifications.put("from",currentuserid);
                                             chatnotifications.put("type","request");
                                             Notifications.child(recieverid).push().setValue(chatnotifications).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                 @Override
                                                 public void onComplete(@NonNull Task<Void> task) {
                                                     if (task.isSuccessful())
                                                     {
                                                         SendRequest.setEnabled(true);
                                                         current_state = "request_sent";
                                                         SendRequest.setText("Cancel Chat Request");
                                                     }
                                                 }
                                             });

                                         }
                                     }
                                 });
                     }
                 }
             });
    }
}
