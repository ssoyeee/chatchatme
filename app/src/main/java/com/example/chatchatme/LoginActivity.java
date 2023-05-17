package com.example.chatchatme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.chatchatme.chat.MessageActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    String TAG = "MainActivity";
    EditText etId;
    EditText etPassword;
    String stEmail;
    String stPassword;
    FirebaseDatabase database;
    EditText name;
    String stName;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseAuth.AuthStateListener mAuthListener;

    ImageView user_profile;
    Uri image_uri;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        signOut();

     //   getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setStatusBarColor(Color.BLACK);


        etId = (EditText) findViewById(R.id.etId);
        etPassword = (EditText) findViewById(R.id.etPassword);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        name = (EditText) findViewById(R.id.signupActivity_edittext_name);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);

                } else {
                    //User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
        signOut();
        Button btnRegister = (Button) findViewById(R.id.btnRegister);
        progressBar.setVisibility(View.GONE);
        btnRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick (View view){
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });

        Button btnLogin = (Button) findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toast.makeText(MainActivity.this,"Login",Toast.LENGTH_LONG).show();

                stEmail = etId.getText().toString();
                stPassword = etPassword.getText().toString();

                if(stEmail.isEmpty()){
                    Toast.makeText(LoginActivity.this, "Please enter your email!", Toast.LENGTH_SHORT).show();
                    return;
                }if(stPassword.isEmpty()){
                    Toast.makeText(LoginActivity.this, "Please enter your password!", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);

                userLogin();

            }
        });
    }

    public void userLogin(){

        mAuth.signInWithEmailAndPassword(stEmail, stPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        final String uid = task.getResult().getUser().getUid();
                        Intent in = new Intent(LoginActivity.this, MainActivity.class);
                        in.putExtra("email", stEmail);
                        in.putExtra("name", stName);
                        in.putExtra("uid",uid);
                        startActivity(in);

                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(),Toast.LENGTH_LONG).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        mAuth.addAuthStateListener(mAuthListener);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }
    public void onStop(){
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public void updateUI(FirebaseUser currentUser){

//        if(currentUser != null){
//            Toast.makeText(this,"Signed in Successfully",Toast.LENGTH_LONG).show();
//            startActivity(new Intent(this,MainActivity.class));
//            reload();
//
//        }else {
//            Toast.makeText(this,"Login Attempt Failed",Toast.LENGTH_LONG).show();
//        }

    }

    private void reload() {
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

//    public void registerUser(String email, String password){
//        mAuth.createUserWithEmailAndPassword(email, password)
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                            @Override
//                            public void onComplete(@NonNull Task<AuthResult> task) {
//                                progressBar.setVisibility(View.GONE);
//
//                                if (task.isSuccessful()) {
//                                    // Sign in success, update UI with the signed-in user's information
//                                    Log.d(TAG, "createUserWithEmail:success");
//                                    FirebaseUser user = mAuth.getCurrentUser();
//                                    String stUserEmail = user.getEmail();
//                                    String stUserName = user.getDisplayName();
//                                    Log.d(TAG, "stUserEmail: "+stUserEmail);
//
//                                    Intent in = new Intent(MainActivity.this, MainActivity2.class);
//                                    startActivity(in);
//                                    Toast.makeText(MainActivity.this, "Authentication Success.",
//                                            Toast.LENGTH_SHORT).show();
//                                    updateUI(user);
//                                } else {
//                                    // If sign in fails, display a message to the user.
//                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
//                                    Toast.makeText(MainActivity.this, "Authentication failed.",
//                                            Toast.LENGTH_SHORT).show();
//                                    updateUI(null);
//                                }
//                            }
//                        });
//
//    }

    private void sendEmailVerification() {
        // Send verification email
        // [START send_email_verification]
        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // Email sent
                    }
                });
        // [END send_email_verification]
    }

    public void signOut() {
        // [START auth_sign_out]
        FirebaseAuth.getInstance().signOut();
        // [END auth_sign_out]
    }

    /*----------For saving image and user name in Firebase Database-------*/
    private void userProfile() {
        user = mAuth.getCurrentUser();
        if(user != null){
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(etId.getText().toString())
                    .setPhotoUri(image_uri).build();
            user.updateProfile(profileUpdates).addOnCompleteListener(this, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    verifyEmailRequest();
                }
            });
        }
    }
    /*-------For sending verification email on user's registered email------*/
    private void verifyEmailRequest() {
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(LoginActivity.this,"Email verification sent on\n"+etId.getText().toString(),Toast.LENGTH_LONG).show();
                            mAuth.signOut();
                            startActivity(new Intent(LoginActivity.this, LoginActivity.class));
                        }
                        else {
                            Toast.makeText(LoginActivity.this,"Sign up Success But Failed to send verification email.",Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

}