package com.example.chatchatme.fragment;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.Fragment;

import androidx.annotation.NonNull;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.chatchatme.MainActivity;
import com.example.chatchatme.R;
import com.example.chatchatme.chat.MessageActivity;
import com.example.chatchatme.model.ChatModel;
import com.example.chatchatme.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

public class ChatFragment extends Fragment {

    private static final boolean DEVELOPER_MODE = true;
    MainActivity activity;
    RecyclerView recyclerView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity)getActivity();

    }
    @Override
    public void onDetach() {
        super.onDetach();
        activity=null;
    }

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd hh:mm");

    public void onCreate(Bundle savedInstanceState) {
        if (DEVELOPER_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()   // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_chat,container,false);

        recyclerView = view.findViewById(R.id.chatfragment_recyclerview);
        recyclerView.setAdapter(new ChatRecyclerViewAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        recyclerView.setItemAnimator(null);
        FirebaseDatabase.getInstance().getReference().child("chatrooms").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recyclerView.setAdapter(new ChatRecyclerViewAdapter());
                recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return view;

    }



    class ChatRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        private String uid;


        private ArrayList<String> destinationUsers = new ArrayList<>();
        private List<ChatModel> chatModels = new ArrayList<>();
        private List<ChatModel> timesort = new ArrayList<ChatModel>();

        public ChatRecyclerViewAdapter() {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/"+uid).equalTo(true)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    chatModels.isEmpty();
                    timesort.isEmpty();
                    chatModels.clear();
                    timesort.clear();
                    for (DataSnapshot item : snapshot.getChildren()){
                        timesort.add(item.getValue(ChatModel.class));
                    }
                    Collections.sort(timesort, new Comparator<ChatModel>() {
                                @Override
                                public int compare(ChatModel o1, ChatModel o2) {
                                    if (o1.lastTime - o2.lastTime > 0) {
                                        return 1;
                                    } else if (o1.lastTime - o2.lastTime < 0) {
                                        return -1;
                                    } else
                                        return 0;
                                }
                            });Collections.reverse(timesort);
                            chatModels = timesort;
                            notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat,parent,false);

            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int i) {
            //   holder.setIsRecyclable(false);
            final CustomViewHolder customViewHolder = (CustomViewHolder) holder;
            String destinationUid = null;
            // check every user in chatroom
            for (String user : chatModels.get(i).users.keySet()) {
                if (!user.equals(uid)) {
                    destinationUid = user;
                    destinationUsers.add(destinationUid);
                }
            }
            FirebaseDatabase.getInstance().getReference().child("users").child(destinationUid)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            UserModel userModel = snapshot.getValue(UserModel.class);
                            if (isAdded()) {
                                Glide.with(customViewHolder.itemView.getContext())
                                        .load(userModel.profileImageUrl)
                                        .apply(new RequestOptions().circleCrop())
                                        .into(customViewHolder.imageView);

                                customViewHolder.textView_title.setText(userModel.userName);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
            Map<String, ChatModel.Comment> commentMap = new TreeMap<>(Collections.reverseOrder());
            commentMap.putAll(chatModels.get(i).comments);

            if (commentMap.size() > 0) {
                String lastMessageKey = (String) commentMap.keySet().toArray()[0];
                customViewHolder.textView_last_message.setText(chatModels.get(i).comments.get(lastMessageKey).message);

            customViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), MessageActivity.class);
                    intent.putExtra("destinationUid", destinationUsers.get(i));

                    ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(v.getContext(), R.anim.fromright, R.anim.toleft);
                    activity.startActivityForResult(intent, 102, activityOptions.toBundle());
                }
            });

            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            long unixTime = (long) chatModels.get(i).comments.get(lastMessageKey).timestamp;
            Date date = new Date(unixTime);
            customViewHolder.textView_timestamp.setText(simpleDateFormat.format(date));
        }}

        @Override
        public int getItemCount() {
            return chatModels.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            public ImageView imageView;
            public TextView textView_title;
            public TextView textView_last_message;
            public TextView textView_timestamp;

            public CustomViewHolder(View itemView) {

                super(itemView);
                imageView = (ImageView) itemView.findViewById(R.id.chatitem_imageview);
                textView_title = (TextView) itemView.findViewById(R.id.chatitem_textview_title);
                textView_last_message = (TextView) itemView.findViewById(R.id.chatitem_textview_lastMessage);
                textView_timestamp = (TextView) itemView.findViewById(R.id.chatitem_textview_timestamp);
            }
        }
    }
    public void setChatroom(){
        FirebaseDatabase.getInstance().getReference().child("chatrooms").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                recyclerView.setAdapter(new ChatRecyclerViewAdapter());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

}
