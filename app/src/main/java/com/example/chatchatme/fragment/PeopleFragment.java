package com.example.chatchatme.fragment;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.chatchatme.MainActivity;
import com.example.chatchatme.R;
import com.example.chatchatme.chat.MessageActivity;
import com.example.chatchatme.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PeopleFragment extends Fragment {
    MainActivity activity;
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


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_people,container,false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.peoplefragment_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        recyclerView.setAdapter(new PeopleFragmentRecyclerViewAdapter());
        final String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        return view;

    }

    class PeopleFragmentRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        List<UserModel> userModels;

        public PeopleFragmentRecyclerViewAdapter(){
            userModels = new ArrayList<>();
            final String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseDatabase.getInstance().getReference().child("users").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    userModels.clear();
                    for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
                        UserModel userModel = snapshot.getValue(UserModel.class);
                        if (userModel.uid.equals(myUid)) {
                            continue;
                        } else {
                            userModels.add(userModel);
                        }
                    }
                    notifyDataSetChanged();//refresh
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend,parent,false);

            return new CustomViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int i) {
            Glide.with
                    (holder.itemView.getContext())
                    .load(userModels.get(i).profileImageUrl)
                    .apply(new RequestOptions().circleCrop())
                    .into(((CustomViewHolder)holder).imageView);
            ((CustomViewHolder)holder).textView.setText(userModels.get(i).userName);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent in = new Intent(v.getContext(), MessageActivity.class);
                    in.putExtra("destinationUid", userModels.get(i).uid);
                    ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(v.getContext(), R.anim.fromright, R.anim.toleft);
                    startActivity(in, activityOptions.toBundle());
                }

            });

        }
        public int getItemCount(){
            return userModels.size();
    }

    private class CustomViewHolder extends RecyclerView.ViewHolder{
        public ImageView imageView;
        public TextView textView;

        public CustomViewHolder(View v){
            super(v);
            imageView = (ImageView) v.findViewById(R.id.frienditem_imageview);
            textView = (TextView) v.findViewById(R.id.frienditem_textview);
        }
    }
//        public void setImageView_profile(){
//            FirebaseDatabase.getInstance().getReference().child("users").addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                    for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                        UserModel userModel = snapshot.getValue(UserModel.class);
//                        if(isAdded()){
//                            if (userModel.uid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
//                                Glide.with  //이미지와 사진 추가
//                                        (getContext())
//                                        .load(userModel.profileImageUrl)
//                                        .apply(new RequestOptions().circleCrop())
//                                        .into(imageView_profile);
//                            }}
//                    }
//                }
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                }
//            });
//
//        }

    }
}
