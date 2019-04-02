package com.example.computer.mywhatsapp;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.computer.mywhatsapp.Model.Messages;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>
{
  private List<Messages>userMessageList;
  private FirebaseAuth mAuth;
  private DatabaseReference UserRef;
  public MessageAdapter(List<Messages>userMessageList)
  {
      this.userMessageList = userMessageList;
  }
    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView senderMessageText,receiverMessageText;
        public CircleImageView receiverprofileImage;
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMessageText = (TextView)itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = (TextView)itemView.findViewById(R.id.receiver_message_text);
            receiverprofileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);

        }
    }
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_message_layout,viewGroup,false);
        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, int i) {
         String messageSenderid =mAuth.getCurrentUser().getUid();
         Messages messages = userMessageList.get(i);
         String fromuserId = messages.getFrom();
         String frommessagetype = messages.getType();
         UserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromuserId);
         UserRef.addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 if (dataSnapshot.hasChild("image"))
                 {
                     String receiverid = dataSnapshot.child("image").getValue().toString();
                     Picasso.get().load(receiverid).placeholder(R.drawable.ic_person_black_24dp).into(messageViewHolder.receiverprofileImage);
                 }
             }

             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {

             }
         });

         if (frommessagetype.equals("text"))
         {
             messageViewHolder.receiverMessageText.setVisibility(View.INVISIBLE);
             messageViewHolder.receiverprofileImage.setVisibility(View.INVISIBLE);
             messageViewHolder.senderMessageText.setVisibility(View.INVISIBLE);

             if (fromuserId.equals(messageSenderid))
              {
                  messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);
                  messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_message_layout);
                  messageViewHolder.senderMessageText.setTextColor(Color.BLACK);
                  messageViewHolder.senderMessageText.setText(messages.getMessage());
              }
              else
              {
                  messageViewHolder.receiverprofileImage.setVisibility(View.VISIBLE);
                  messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);

                  messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_message_layout);
                  messageViewHolder.receiverMessageText.setTextColor(Color.BLACK);
                  messageViewHolder.receiverMessageText.setText(messages.getMessage());
              }
         }
    }

    @Override
    public int getItemCount() {
        return userMessageList.size();
    }



}
