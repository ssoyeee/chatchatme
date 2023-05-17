package com.example.chatchatme;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatchatme.model.UserModel;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.util.TextUtils;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

public class SignupActivity extends AppCompatActivity {
    private static final int PICK_FROM_ALBUM = 10;
    String TAG = "SignupActivity";
    EditText email;
    String stEmail;
    EditText password;
    String stPassword;
    EditText name;
    String stName;
    Button signup;
    ImageView profile;
    Uri imageUri;
    ProgressBar progressBar;
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);


        profile = (ImageView)findViewById(R.id.signupActivity_imageview_profile);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, PICK_FROM_ALBUM);

            }
        });

        email = (EditText) findViewById(R.id.signupActivity_edittext_email);
        name = (EditText) findViewById(R.id.signupActivity_edittext_name);
        password = (EditText) findViewById(R.id.signupActivity_edittext_password);
        signup = (Button) findViewById(R.id.signupActivity_button_signup);

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                stEmail = email.getText().toString();
                stName = name.getText().toString();
                stPassword = password.getText().toString();

                if (stEmail == null) {
                    Toast.makeText(SignupActivity.this, "Please enter your email!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(stName)) {
                    Toast.makeText(SignupActivity.this, "Please enter your name!", Toast.LENGTH_SHORT).show();
                    return;
                }if (TextUtils.isEmpty(stPassword)) {
                    Toast.makeText(SignupActivity.this, "Please enter your password!", Toast.LENGTH_SHORT).show();
                    return;
                }if (imageUri == null){
                    return;
                }
//            progressBar.setVisibility(View.VISIBLE);
                Toast.makeText(SignupActivity.this, "Your <" + stEmail+ "> has been registered. Please allow us a moment to upload your profile!", Toast.LENGTH_LONG).show();
                registerUser(stEmail, stName, stPassword);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FROM_ALBUM && resultCode == RESULT_OK) {
            profile.setImageURI(data.getData());
            imageUri = data.getData(); //original image path
        }
    }

    public void registerUser(String email, String name, String password) {

        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(stEmail, stPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        final String uid = task.getResult().getUser().getUid();
                        FirebaseStorage.getInstance().getReference().child("userImages").child(uid).putFile(imageUri)
                                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(Task<UploadTask.TaskSnapshot> taskSnapshot) {
                                        if (taskSnapshot.isSuccessful()) {
                                            Toast.makeText(SignupActivity.this, "Your picture saved successfully", Toast.LENGTH_LONG).show();
                                            String DownloadUrl = taskSnapshot.getResult().getStorage().getDownloadUrl().toString();
//                                            String imageUrl = task.getResult().toString();
                                            Task<Uri> uriTask = FirebaseStorage.getInstance().getReference().child("userImages").child(uid).getDownloadUrl();
                                            while(!uriTask.isSuccessful());
                                                Uri downloadUrl = uriTask.getResult();
                                                String imageUrl = downloadUrl.toString();
                                                UserModel userModel = new UserModel();
                                            userModel.userName = stName;
                                            userModel.profileImageUrl = imageUrl;
                                            userModel.uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                                            FirebaseDatabase.getInstance().getReference().child("users").child(uid).setValue(userModel)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                                                            startActivity(intent);
                                                            intent.putExtra("email", stEmail);
                                                            intent.putExtra("name", stName);
                                                            intent.putExtra("uid",uid);
                                                            SignupActivity.this.finish();
                                                        }
                                                    });

                                        }

//                                        Intent intent = new Intent(SignupActivity.this, MainActivity.class);
//                                        startActivity(intent);


                                    }
                                });
//                        UserModel userModel = new UserModel();
//
//                        if(task.isSuccessful())
//                        {
//                            // Sign in success, update UI with the signed-in user's information
//                            Log.d(TAG, "createUserWithEmail:success");
//                            FirebaseUser user = mAuth.getCurrentUser();
//                            String stUserEmail = user.getEmail();
//                            String stUserName = user.getDisplayName();
//                            Log.d(TAG, "stUserEmail: " + stUserEmail);
//
//                            Intent in = new Intent(SignupActivity.this, ChatActivity.class);
//                            startActivity(in);
//                            Toast.makeText(SignupActivity.this, "Authentication Success.",
//                                    Toast.LENGTH_SHORT).show();
//                            updateUI(user);
//                        } else
//
//                        {
//                            // If sign in fails, display a message to the user.
//                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
//                            Toast.makeText(SignupActivity.this, "Authentication failed.",
//                                    Toast.LENGTH_SHORT).show();
//                            updateUI(null);
//                        }
                    }
                });
    }

    public void updateUI(FirebaseUser currentUser) {

//        if(currentUser != null){
//            Toast.makeText(this,"Signed in Successfully",Toast.LENGTH_LONG).show();
//            startActivity(new Intent(this,MainActivity.class));
//            reload();
//
//        }else {
//            Toast.makeText(this,"Login Attempt Failed",Toast.LENGTH_LONG).show();
//        }

}
public void getCurrentUser(FirebaseUser user){
        // Name, email address, and profile photo Url
        String name = user.getDisplayName();
        String email = user.getEmail();
        Uri photoUrl = user.getPhotoUrl();

        // Check if user's email is verified
        boolean emailVerified = user.isEmailVerified();

        // The user's ID, unique to the Firebase project. Do NOT use this value to
        // authenticate with your backend server, if you have one. Use
        // FirebaseUser.getIdToken() instead.
        String uid = user.getUid();


}


}