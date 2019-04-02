package com.example.computer.mywhatsapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.example.computer.mywhatsapp.Model.Messages;
import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.common.internal.Objects;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
private String receiverid,receivername,receiverimage,receiverstatus,messagesenderid;
private TextView userName, userLastSeen;
private CircleImageView circleImageView;
private Toolbar ChatToolbar;
private FloatingActionButton imageButton;
private DatabaseReference RootRef;
private FirebaseAuth mAuth;
private EditText editText;
private final List<Messages> messagesList = new ArrayList<>();
private LinearLayoutManager linearLayoutManager;
private MessageAdapter messageAdapter;
private RecyclerView userMessageslist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mAuth = FirebaseAuth.getInstance();
        messagesenderid = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        receiverid = getIntent().getExtras().get("visit_user_id").toString();
        receiverstatus = getIntent().getExtras().get("visit_user_status").toString();
        receivername = getIntent().getExtras().get("visit_user_name").toString();
        receiverimage = getIntent().getExtras().get("visit_user_image").toString();
        DisplayLastSeen();
        initializecontrollers();
        userName.setText(receivername);
        Picasso.get().load(receiverimage).placeholder(R.drawable.ic_person_black_24dp).into(circleImageView);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage();
            }
        });
       // userLastSeen.setText(receiverstatus);
    }
    public void DisplayLastSeen()
    {
        RootRef.child("Users").child(messagesenderid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("userState").hasChild("state"))
                {
                    String state = dataSnapshot.child("userState").child("state").getValue().toString();
                    String date = dataSnapshot.child("userState").child("date").getValue().toString();
                    String time = dataSnapshot.child("userState").child("time").getValue().toString();
                    if (state.equals("online"))
                    {
                        userLastSeen.setText("online");
                    }
                    else if (state.equals("offline"))
                    {
                        userLastSeen.setText("Last Seen:" + date + " " + time);
                    }
                }
                else
                {
                    userLastSeen.setText("offline");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        RootRef.child("Messages").child(messagesenderid).child(receiverid).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages messages = dataSnapshot.getValue(Messages.class);
                messagesList.add(messages);
                messageAdapter.notifyDataSetChanged();
                userMessageslist.smoothScrollToPosition(userMessageslist.getAdapter().getItemCount());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SendMessage() {
    String messagetext = editText.getText().toString();
    if (TextUtils.isEmpty(messagetext))
    {
        Toast.makeText(ChatActivity.this,"Please enter your message",Toast.LENGTH_SHORT).show();
    }
    else {
        String messageSenderRef = "Messages/"+messagesenderid +"/" +receiverid;
        String messageReceiverRef = "Messages/"+receiverid +"/" +messagesenderid;
        DatabaseReference usermessagereferky = RootRef.child("Messages").child(messagesenderid).child(receiverid).push();
        String messagepushid= usermessagereferky.getKey();
        Map messagetextbody = new HashMap();
        messagetextbody.put("message",messagetext);
        messagetextbody.put("type","text");
        messagetextbody.put("from",messagesenderid);
        messagetextbody.put("message",messagetext);

        Map messaebodydetail = new HashMap();
        messaebodydetail.put(messageSenderRef + "/" +messagepushid,messagetextbody);
        messaebodydetail.put(messageReceiverRef + "/" +messagepushid,messagetextbody);
        RootRef.updateChildren(messaebodydetail).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful())
                {
                    Toast.makeText(ChatActivity.this,"Message sent successfully",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(ChatActivity.this,"Errorr  ",Toast.LENGTH_SHORT).show();
                }
                editText.setText("");
            }
        });
    }
    }

    private void initializecontrollers() {
        ChatToolbar = (Toolbar)findViewById(R.id.chat_toolbar);
        imageButton = (FloatingActionButton) findViewById(R.id.send_message_btn);
        editText = (EditText)findViewById(R.id.input_message);
        setSupportActionBar(ChatToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionbarview = layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(actionbarview);
        circleImageView = (CircleImageView)findViewById(R.id.custom_profile_image);
        userName = (TextView) findViewById(R.id.custom_profile_name);
        userLastSeen = (TextView) findViewById(R.id.custom_profile_status);
        messageAdapter = new MessageAdapter(messagesList);
        userMessageslist = (RecyclerView)findViewById(R.id.private_message_list_os_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessageslist.setLayoutManager(linearLayoutManager);
        userMessageslist.setAdapter(messageAdapter);
    }
}
