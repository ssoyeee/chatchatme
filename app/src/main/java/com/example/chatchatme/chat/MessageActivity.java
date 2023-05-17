package com.example.chatchatme.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.chatchatme.LoginActivity;
import com.example.chatchatme.MainActivity;
import com.example.chatchatme.R;
import com.example.chatchatme.model.ChatModel;
import com.example.chatchatme.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.util.TextUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class MessageActivity extends AppCompatActivity {
    private String destinationUid;
    private ImageButton imgbutton;
    private ImageButton exitbutton;
    private EditText editText;
    private TextView textView;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private UserModel destinationUserModel;

    private String uid;
    private String myuid;
    private String chatRoomUid;
    int peopleCount = 0;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");

    private List<ChatModel.Comment> comments;

    FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        database = FirebaseDatabase.getInstance();

        myuid = FirebaseAuth.getInstance().getCurrentUser().getUid(); //my side
        Log.d("Soyeon", String.format("myuid=%s", FirebaseAuth.getInstance().getCurrentUser().getUid()));


        destinationUid = getIntent().getStringExtra("destinationUid"); // the other side

        imgbutton = (ImageButton) findViewById(R.id.messageActivity_button);
        editText = (EditText) findViewById(R.id.messageActivity_editText);
        recyclerView = (RecyclerView) findViewById(R.id.messageActivity_recyclerview);
        textView = (TextView) findViewById(R.id.messageActivity_destname);

        FirebaseDatabase.getInstance().getReference().child("users").child(destinationUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserModel userModel = dataSnapshot.getValue(UserModel.class);
                textView.setText(userModel.userName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        exitbutton = (ImageButton) findViewById(R.id.messageActivity_button_exit);
        exitbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
//        if (editText.getText().toString() == null) {imgbutton.setEnabled(false);}
//        else {imgbutton.setEnabled(true);}

        imgbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText().length() == 0) {
                    return;
                }
                ChatModel chatModel = new ChatModel();
                chatModel.users.put(myuid, true);
                chatModel.users.put(destinationUid, true);



                if (chatRoomUid==null) {
                    imgbutton.setEnabled(false);
                    Toast.makeText(MessageActivity.this, "chat room has been created", Toast.LENGTH_SHORT).show();

                    database.getInstance().getReference().child("chatrooms").push().setValue(chatModel)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    checkChatRoom();
                                }
                            });
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //Write whatever to want to do after delay specified (1 sec)

                           //  database.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments").push().setValue(comment);
                            Log.d("Handler", "Running Handler");
                        }
                    }, 100);
                } else {
                    ChatModel.Comment comment = new ChatModel.Comment();
                    comment.uid = myuid;
                    comment.message = editText.getText().toString();
                    comment.timestamp = ServerValue.TIMESTAMP;

                    database.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments").push().setValue(comment)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    editText.setText("");
                                }
                            });
                    //  sendtoDB();
                }
                // FirebaseDatabase.getInstance().getReference().child("chatrooms").push().setValue(chatModel);
            }
        });
        checkChatRoom();

    }

//    public void checkChatRoom() {
//        FirebaseDatabase.getInstance().getReference().child("chatrooms")
//                .orderByChild("users/" + uid).equalTo(true)
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        if (dataSnapshot.getValue() == null) {
//                            ChatModel newRoom = new ChatModel();
//                            newRoom.users.put(uid, true);
//                            newRoom.users.put(destinationUid, true);
//                            FirebaseDatabase.getInstance().getReference().child("chatrooms").push().setValue(newRoom)
//                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                        @Override
//                                        public void onSuccess(Void unused) {
//                                            checkChatRoom();
//                                        }
//                                    });
//                            return;
//                        }
//
//                        for (DataSnapshot item : dataSnapshot.getChildren()) {
//                            ChatModel chatModel = item.getValue(ChatModel.class);
//                            if (chatModel.users.containsKey(destinationUid) && chatModel.users.size() == 2) {
//                                chatRoomUid = item.getKey();
//                                imgbutton.setEnabled(true);
//                                recyclerView.setLayoutManager(new LinearLayoutManager(MessageActivity.this));
//                                recyclerView.setAdapter(new RecyclerViewAdapter());
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
//    }

    void checkChatRoom() {

        FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/" + myuid).equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    ChatModel chatModel = item.getValue(ChatModel.class);
                    if (chatModel.users.containsKey(destinationUid)) {
//!if(TextUtils.isEmpty(chatRoomUid)
                            chatRoomUid = item.getKey();
                            imgbutton.setEnabled(true);
                            //sync
                            recyclerView.setLayoutManager(new LinearLayoutManager(MessageActivity.this));
                            recyclerView.setAdapter(new RecyclerViewAdapter());


                            // sendtoDB();

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendtoDB() {
        if (!editText.getText().toString().equals("")) {
            ChatModel.Comment comment = new ChatModel.Comment();
            comment.uid = uid;
            comment.message = editText.getText().toString();
            comment.timestamp = ServerValue.TIMESTAMP;
            database.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments").push().setValue(comment)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            editText.setText("");
                        }
                    });
        }
    }

    // =============================================================chat==========
    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        List<ChatModel.Comment> comments;
        UserModel userModel;

        public RecyclerViewAdapter() {
            comments = new ArrayList<>();
            FirebaseDatabase.getInstance().getReference().child("users").child(destinationUid)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            comments.isEmpty();
                            comments.clear();
                       //     mAdapter.notifyDataSetChanged();
                            userModel = dataSnapshot.getValue(UserModel.class);
                            getMessageList();
//                    for(DataSnapshot item: dataSnapshot.getChildren()){
//                        comments.add(item.getValue(ChatModel.Comment.class));
                        }
                        //     notifyDataSetChanged();
                        //destinationUserModel = dataSnapshot.getValue(UserModel.class);
                        // getMessageList();

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_message, viewGroup, false);
            return new MessageViewHolder(view);
        }


boolean isEmpty(){
            return comments.isEmpty();
}

        private void getMessageList() {
            FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            comments.isEmpty();
                            comments.clear();//here items is an ArrayList populating the RecyclerView
//                                mAdapter.notifyDataSetChanged();

                            Map<String, Object> readUsersMap = new HashMap<>();

                            for (DataSnapshot item : snapshot.getChildren()) {
                                comments.add(item.getValue(ChatModel.Comment.class));
                            }
                            notifyDataSetChanged();
                            recyclerView.scrollToPosition(comments.size() - 1);
                        }
//                                String key = item.getKey();
//                                ChatModel.Comment commentOrigin = item.getValue(ChatModel.Comment.class);
//                                ChatModel.Comment commentModify = item.getValue(ChatModel.Comment.class);
//                                commentModify.readUsers.put(myuid, true);
//
//                                readUsersMap.put(key, commentModify);
//                                comments.add(commentOrigin);
//                            }
//                            if(!comments.get(comments.size()-1).readUsers.containsKey(myuid)){
//                                database.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments").updateChildren(readUsersMap)
//                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                            @Override
//                                            public void onComplete(@NonNull Task<Void> task) {
//                                                //refresh
//                                                notifyDataSetChanged();
//                                                recyclerView.scrollToPosition(comments.size()-1);
//                                            }
//                                        });
//                            }else{
//                                notifyDataSetChanged();
//                                recyclerView.scrollToPosition(comments.size()-1);
//                            }

//                            if(comments.size() == 0){
//                                return;
                                //notifyItemInserted(comments.size() - 1);

//                            comments.addAll((Collection<? extends ChatModel.Comment>) recyclerView);// add new data
//                            mAdapter.notifyItemRangeInserted(0, comments.size());// notify adapter of new data
                                //notifyDataSetChanged();

                            @Override
                            public void onCancelled (@NonNull DatabaseError databaseError){
                            }
                        });
                    }
//                            if (!comments.get(comments.size() - 1).readUsers.containsKey(uid)){
//                                FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments")
//                                        .updateChildren(readUsersMap).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<Void> task) {
//                                        notifyDataSetChanged();
//                                        recyclerView.scrollToPosition(comments.size() - 1);
//                                    }
//                                });
//                                //message refresh
//                            }else{
//                                notifyDataSetChanged();
//                                recyclerView.scrollToPosition(comments.size() - 1);
//                            }
//
//                        }

//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//
//                        }
//                    });


        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
            MessageViewHolder messageViewHolder = ((MessageViewHolder) viewHolder);
            // if (!(messageViewHolder.textView_message == null)) {
            Log.d("Soyeon", String.format("myuid=%s", comments.get(i).uid));

            //if (comments != null && comments.get(i).uid.equals(myuid)) {
                if (comments != null && comments.get(i).uid  != null && comments.get(i).uid.equals(myuid)) {
                // I sent
                //    if (comments.get(i).uid.equals(myuid)) {
                messageViewHolder.textView_message.setText(comments.get(i).message);
                messageViewHolder.textView_message.setBackgroundResource(R.drawable.rightbubble);
                messageViewHolder.LinearLayout_destination.setVisibility(View.INVISIBLE);
                messageViewHolder.LinearLayout_main.setGravity(Gravity.RIGHT);
//                messageViewHolder.LinearLayout_time.setGravity(Gravity.RIGHT);
                messageViewHolder.textView_message.setTextSize(25);
              //  setReadCounter(i, messageViewHolder.textView_readCounter_left);
                messageViewHolder.textView_name.setText("");
                //you sent
            } else {
                Glide.with(messageViewHolder.itemView.getContext())
                        .load(userModel.profileImageUrl)

                        .apply(new RequestOptions().circleCrop())
                        .into(messageViewHolder.imageView_profile);
                messageViewHolder.textView_name.setText(userModel.userName);

                messageViewHolder.LinearLayout_destination.setVisibility(View.VISIBLE);
                messageViewHolder.textView_message.setBackgroundResource(R.drawable.leftbubble);
                messageViewHolder.textView_message.setText(comments.get(i).message);
                messageViewHolder.textView_message.setTextSize(25);
                messageViewHolder.LinearLayout_main.setGravity(Gravity.LEFT);
//                messageViewHolder.LinearLayout_time.setGravity(Gravity.LEFT);
               // setReadCounter(i, messageViewHolder.textView_readCounter_right);
            }
            //  messageViewHolder.textView_message.setText(comments.get(i).message);
            long unixTime = (long) comments.get(i).timestamp;
            Date date = new Date(unixTime);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            String time = simpleDateFormat.format(date);
            messageViewHolder.textView_timestamp.setText(time);
        }


        @Override
        public int getItemCount() {
            return comments.size();
        }
//        public void setReadCounter(final int position, final TextView textView) {
//            //uid count == people count
//            if (peopleCount == 0) {
//                database.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("users")
//                        .addListenerForSingleValueEvent(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                Map<String, Boolean> users = (Map<String, Boolean>) snapshot.getValue();
//                                peopleCount = users.size();
//                                int count = peopleCount - comments.get(position).readUsers.size();
//                                if (count > 0) {
//                                    textView.setVisibility(View.VISIBLE);
//                                    textView.setText(String.valueOf(count));
//                                } else {
//                                    textView.setVisibility(View.INVISIBLE);
//                                }
//                            }
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError error) {
//
//                            }
//                        });
//            } else {
//                int count = peopleCount - comments.get(position).readUsers.size();
//                if (count > 0) {
//                    textView.setVisibility(View.VISIBLE);
//                    textView.setText(String.valueOf(count));
//                } else {
//                    textView.setVisibility(View.INVISIBLE);
//                }
//            }
//        }
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView textView_message; //message content
        public TextView textView_name;
        public TextView textView_timestamp;
        public ImageView imageView_profile;
        public LinearLayout LinearLayout_destination;
        public LinearLayout LinearLayout_main;
        public LinearLayout LinearLayout_time;
      //  public TextView textView_readCounter_left;
      //  public TextView textView_readCounter_right;

        public MessageViewHolder(View itemView) {
            super(itemView);
            textView_message = (TextView) itemView.findViewById(R.id.messageItem_textView_message);
            textView_name = (TextView) itemView.findViewById(R.id.messageItem_textview_name);
            imageView_profile = (ImageView) itemView.findViewById(R.id.messageItem_LinearLayout_profile);
            LinearLayout_destination = (LinearLayout) itemView.findViewById(R.id.messageItem_LinearLayout_destination);
            LinearLayout_main = (LinearLayout) itemView.findViewById(R.id.messageItem_LinearLayout_main);
            textView_timestamp = (TextView) itemView.findViewById(R.id.messageItem_textview_timestamp);

     //       textView_readCounter_left = (TextView) itemView.findViewById(R.id.messageItem_textview_readCounterLeft);
      //      textView_readCounter_right = (TextView) itemView.findViewById(R.id.messageItem_textview_readCounterRight);

        }


        public void onBackPressed() {
            databaseReference.removeEventListener(valueEventListener);
            Intent intent = new Intent(MessageActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            finish();
            startActivity(intent);
            overridePendingTransition(R.anim.fromleft, R.anim.toright);
        }
    }
}


