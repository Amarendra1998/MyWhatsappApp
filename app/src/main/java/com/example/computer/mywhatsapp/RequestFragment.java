package com.example.computer.mywhatsapp;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {

private View RequestFragmentView;
private RecyclerView recyclerView;
    private DatabaseReference ChatRequestRef,UserRef,ContactsRef;
    private FirebaseAuth mAuth;
    private String currentuserid;
    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RequestFragmentView=  inflater.inflate(R.layout.fragment_request, container, false);
        recyclerView = (RecyclerView)RequestFragmentView.findViewById(R.id.chat_requests_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAuth = FirebaseAuth.getInstance();
        currentuserid = mAuth.getCurrentUser().getUid();
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        ChatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Request");
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        return RequestFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatRequestRef.child(currentuserid),Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,RequestViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, RequestViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final RequestViewHolder holder, int position, @NonNull Contacts model) {

                       holder.itemView.findViewById(R.id.request_accept).setVisibility(View.VISIBLE);
                       holder.itemView.findViewById(R.id.request_cancel).setVisibility(View.VISIBLE);
                        final String userID = getRef(position).getKey();
                        DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();

                        getTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists())
                                {
                                    String type = dataSnapshot.getValue().toString();
                                    if (type.equals("received"))
                                    {
                                       UserRef.child(userID).addValueEventListener(new ValueEventListener() {
                                           @Override
                                           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                               if (dataSnapshot.hasChild("image"))
                                               {

                                                   final String requestUserImage = dataSnapshot.child("image").getValue().toString();
                                                   Picasso.get().load(requestUserImage).placeholder(R.drawable.ic_person_black_24dp).into(holder.profileImage);
                                               }
                                               final String requestUserName = dataSnapshot.child("name").getValue().toString();
                                               final String requestUserStatus = dataSnapshot.child("status").getValue().toString();
                                                holder.userName.setText(requestUserName);
                                                holder.userStatus.setText("wants to connect with you");

                                                holder.accept.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        ContactsRef.child(currentuserid).child(userID).child("Contacts").setValue("Saved")
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful())
                                                                        {
                                                                            ContactsRef.child(userID).child(currentuserid).child("Contacts").setValue("Saved")
                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if (task.isSuccessful())
                                                                                            {
                                                                                                ChatRequestRef.child(currentuserid).child(userID).removeValue()
                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                            @Override
                                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                                if (task.isSuccessful())
                                                                                                                {
                                                                                                                    ChatRequestRef.child(userID).child(currentuserid).removeValue()
                                                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                                @Override
                                                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                    if (task.isSuccessful())
                                                                                                                                    {
                                                                                                                                        Toast.makeText(getContext(),"New Contact Saved",Toast.LENGTH_SHORT).show();
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
                                                                });
                                                    }
                                                });
                                                holder.cancel.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        ChatRequestRef.child(currentuserid).child(userID).removeValue()
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful())
                                                                        {
                                                                            ChatRequestRef.child(userID).child(currentuserid).removeValue()
                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if (task.isSuccessful())
                                                                                            {
                                                                                                Toast.makeText(getContext(),"Contact Deleted",Toast.LENGTH_SHORT).show();
                                                                                            }
                                                                                        }
                                                                                    });
                                                                        }
                                                                    }
                                                                });
                                                    }
                                                });
                                               holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                   @Override
                                                   public void onClick(View v) {
                                                       CharSequence options[]= new CharSequence[]
                                                               {
                                                                       "Accept",
                                                                       "Cancel"
                                                               };
                                                       AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                       builder.setTitle(requestUserName+"Chat Request");
                                                       builder.setItems(options, new DialogInterface.OnClickListener() {
                                                           @Override
                                                           public void onClick(DialogInterface dialog, int i) {
                                                               if (i==0)
                                                               {
                                                                 ContactsRef.child(currentuserid).child(userID).child("Contacts").setValue("Saved")
                                                                         .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                             @Override
                                                                             public void onComplete(@NonNull Task<Void> task) {
                                                                                 if (task.isSuccessful())
                                                                                 {
                                                                                     ContactsRef.child(userID).child(currentuserid).child("Contacts").setValue("Saved")
                                                                                             .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                 @Override
                                                                                                 public void onComplete(@NonNull Task<Void> task) {
                                                                                                     if (task.isSuccessful())
                                                                                                     {
                                                                                                        ChatRequestRef.child(currentuserid).child(userID).removeValue()
                                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                    @Override
                                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                                        if (task.isSuccessful())
                                                                                                                        {
                                                                                                                            ChatRequestRef.child(userID).child(currentuserid).removeValue()
                                                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                                        @Override
                                                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                            if (task.isSuccessful())
                                                                                                                                            {
                                                                                                                                                Toast.makeText(getContext(),"New Contact Saved",Toast.LENGTH_SHORT).show();
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
                                                                         });
                                                               }
                                                               if (i==1)
                                                               {
                                                                   ChatRequestRef.child(currentuserid).child(userID).removeValue()
                                                                           .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                               @Override
                                                                               public void onComplete(@NonNull Task<Void> task) {
                                                                                   if (task.isSuccessful())
                                                                                   {
                                                                                       ChatRequestRef.child(userID).child(currentuserid).removeValue()
                                                                                               .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                   @Override
                                                                                                   public void onComplete(@NonNull Task<Void> task) {
                                                                                                       if (task.isSuccessful())
                                                                                                       {
                                                                                                           Toast.makeText(getContext(),"Contact Deleted",Toast.LENGTH_SHORT).show();
                                                                                                       }
                                                                                                   }
                                                                                               });
                                                                                   }
                                                                               }
                                                                           });
                                                               }
                                                           }
                                                       });
                                                       builder.show();
                                                   }
                                               });
                                           }

                                           @Override
                                           public void onCancelled(@NonNull DatabaseError databaseError) {

                                           }
                                       });
                                    }
                                    else if (type.equals("sent"))
                                    {
                                        Button request_sent_btn = holder.itemView.findViewById(R.id.request_accept);
                                        request_sent_btn.setText("Request Sent");
                                        holder.itemView.findViewById(R.id.request_cancel).setVisibility(View.INVISIBLE);

                                        UserRef.child(userID).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.hasChild("image"))
                                                {

                                                    final String requestUserImage = dataSnapshot.child("image").getValue().toString();
                                                    Picasso.get().load(requestUserImage).placeholder(R.drawable.ic_person_black_24dp).into(holder.profileImage);
                                                }
                                                final String requestUserName = dataSnapshot.child("name").getValue().toString();
                                                final String requestUserStatus = dataSnapshot.child("status").getValue().toString();
                                                holder.userName.setText(requestUserName);
                                                holder.userStatus.setText("you have sent a request to "+ requestUserName);
                                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        CharSequence options[]= new CharSequence[]
                                                                {
                                                                        "Cancel The Chat Request"
                                                                };
                                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                        builder.setTitle("Already sent Request");
                                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int i) {
                                                                if (i==0)
                                                                {
                                                                    ChatRequestRef.child(currentuserid).child(userID).removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful())
                                                                                    {
                                                                                        ChatRequestRef.child(userID).child(currentuserid).removeValue()
                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                        if (task.isSuccessful())
                                                                                                        {
                                                                                                            Toast.makeText(getContext(),"you have cancelled the chat request",Toast.LENGTH_SHORT).show();
                                                                                                        }
                                                                                                    }
                                                                                                });
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                                        builder.show();
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                    @NonNull
                    @Override
                    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout,viewGroup,false);
                        RequestViewHolder viewHolder = new RequestViewHolder(view);
                        return viewHolder;
                    }
                };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }
    public static class RequestViewHolder  extends RecyclerView.ViewHolder
    {
        TextView userName,userStatus;
        CircleImageView profileImage;
        Button accept,cancel;
        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            accept = itemView.findViewById(R.id.request_accept);
            cancel = itemView.findViewById(R.id.request_cancel);
        }
    }
}
