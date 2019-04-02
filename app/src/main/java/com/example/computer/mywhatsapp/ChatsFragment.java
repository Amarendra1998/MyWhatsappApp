package com.example.computer.mywhatsapp;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {
  private View privatechatsfragment;
  private RecyclerView chatslist;
  private DatabaseReference ChatsRef,UserRef;
  private FirebaseAuth mAuth;
  private String currentuserid;
    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        privatechatsfragment = inflater.inflate(R.layout.fragment_chats, container, false);
        mAuth = FirebaseAuth.getInstance();
        currentuserid = mAuth.getCurrentUser().getUid();
        ChatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentuserid);
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatslist = (RecyclerView)privatechatsfragment.findViewById(R.id.chats_light);
        chatslist.setLayoutManager(new LinearLayoutManager(getContext()));
        return privatechatsfragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contacts>options= new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(ChatsRef,Contacts.class).build();
        FirebaseRecyclerAdapter<Contacts,ChatsViewHolder>adapter = new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model) {
              final String userIds = getRef(position).getKey();
                final String[] retImage = {"default_image"};
                UserRef.child(userIds).addValueEventListener(new ValueEventListener() {
                  @Override
                  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                         if(dataSnapshot.exists()){
                      if (dataSnapshot.hasChild("image")) {
                           retImage[0] = dataSnapshot.child("image").getValue().toString();
                          Picasso.get().load(retImage[0]).into(holder.profileImage);
                      }
                             final String retName = dataSnapshot.child("name").getValue().toString();
                             final String retStatus = dataSnapshot.child("status").getValue().toString();
                             holder.userName.setText(retName);
                             holder.userStatus.setText("Last Seen:" + "\n" + "Date" + "Time");
                      if (dataSnapshot.child("userState").hasChild("state"))
                      {
                          String state = dataSnapshot.child("userState").child("state").getValue().toString();
                          String date = dataSnapshot.child("userState").child("date").getValue().toString();
                          String time = dataSnapshot.child("userState").child("time").getValue().toString();
                          if (state.equals("online"))
                          {
                              holder.userStatus.setText("online");
                          }
                          else if (state.equals("offline"))
                          {
                              holder.userStatus.setText("Last Seen:" + date + " " + time);
                          }
                      }
                      else
                      {
                          holder.userStatus.setText("offline");
                      }

                      holder.itemView.setOnClickListener(new View.OnClickListener() {
                          @Override
                          public void onClick(View v) {
                              Intent intent = new Intent(getContext(),ChatActivity.class);
                              intent.putExtra("visit_user_id",userIds);
                              intent.putExtra("visit_user_name",retName);
                              intent.putExtra("visit_user_status",retStatus);
                              intent.putExtra("visit_user_image", retImage[0]);
                              startActivity(intent);
                          }
                      });
                  }
                  }

                  @Override
                  public void onCancelled(@NonNull DatabaseError databaseError) {

                  }
              });
            }

            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                 View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout,viewGroup,false);
                return new ChatsViewHolder(view);
            }
        };
        chatslist.setAdapter(adapter);
        adapter.startListening();

    }
    public static class ChatsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName,userStatus;
        CircleImageView profileImage;
        ImageView onlineicon;
        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            onlineicon = itemView.findViewById(R.id.user_online_status);
        }
    }
}
