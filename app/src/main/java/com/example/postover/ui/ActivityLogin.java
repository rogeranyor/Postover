package com.example.postover.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.postover.MainActivity;
import com.example.postover.Model.Client;
import com.example.postover.R;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ActivityLogin extends AppCompatActivity {
    private EditText loginMail, loginPassword;
    private String mailLogin, passwordLogin;
    private final int GOOGLE_SIGN_IN = 100;
    private final CallbackManager callbackManager = CallbackManager.Factory.create();

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    public FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        OAuthProvider.Builder provider = OAuthProvider.newBuilder("twitter.com");

        mDatabase = FirebaseDatabase.getInstance().getReference();
        user = mAuth.getCurrentUser();

        Button login = (Button) findViewById(R.id.btn_login);
        TextView register = findViewById(R.id.tv_register);
        ImageView googleLogin = findViewById(R.id.google_login);
        ImageView twiterLogin = findViewById(R.id.twitteR_login);
        ImageView facebookLogin = findViewById(R.id.facebook_login);
        loginMail = (EditText) findViewById(R.id.pt_username);
        loginPassword = (EditText) findViewById(R.id.pt_password);


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mailLogin = loginMail.getText().toString();
                passwordLogin = loginPassword.getText().toString();
                if (mailLogin.length() > 0 && passwordLogin.length() > 0) {
                    loginUser();
                } else {
                    Toast.makeText(ActivityLogin.this, "Cannot be empty!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(ActivityLogin.this, ActivityRegister.class);
                ActivityLogin.this.finish();
                ActivityLogin.this.startActivity(mainIntent);

            }
        });

        googleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

                GoogleSignInClient googleClient = GoogleSignIn.getClient(ActivityLogin.this, gso);
                googleClient.signOut();
                startActivityForResult(googleClient.getSignInIntent(), GOOGLE_SIGN_IN);
            }
        });

        twiterLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth
                        .startActivityForSignInWithProvider(ActivityLogin.this, provider.build())
                        .addOnSuccessListener(
                                new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        AuthCredential credential = authResult.getCredential();
                                        assert credential != null;
                                        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if(task.isSuccessful()){
                                                    String id = mAuth.getUid();
                                                    checkUser(id);
                                                }
                                            }
                                        });
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(ActivityLogin.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
            }
        });

        facebookLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logInWithReadPermissions(ActivityLogin.this, Collections.singletonList("email"));
                LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        AccessToken token = loginResult.getAccessToken();
                        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
                        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    String id = mAuth.getUid();
                                    checkUser(id);
                                }else{
                                    Toast.makeText(ActivityLogin.this, "Error! Facebook authentification exploted", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onError(FacebookException error) {
                        Toast.makeText(ActivityLogin.this, "Error!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);

                if(account != null) {
                    AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                    mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                String id = mAuth.getUid();
                                checkUser(id);
                                }else{
                                    Toast.makeText(ActivityLogin.this, "Error! Google authentification exploted", Toast.LENGTH_SHORT).show();
                                }
                        }
                    });
                   }

            } catch (ApiException e) {
                Toast.makeText(ActivityLogin.this, e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        }
    }
    public void checkUser(String id){
        mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(!task.isSuccessful()){
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else{
                    Client client = task.getResult().getValue(Client.class);
                    if(client != null){
                        Toast.makeText(ActivityLogin.this, "Success! login completed", Toast.LENGTH_SHORT).show();
                        Intent activityIntent = new Intent(ActivityLogin.this, MainActivity.class);
                        activityIntent.putExtra("KeepLoged", "KeepLoged");
                        ActivityLogin.this.finish();
                        ActivityLogin.this.startActivity(activityIntent);

                    }else{
                        Client client_ = new Client(mAuth.getCurrentUser().getDisplayName(), mAuth.getCurrentUser().getEmail());
                        mDatabase.child("users").child(id).setValue(client_).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task2) {
                                if (task2.isSuccessful()) {
                                    Toast.makeText(ActivityLogin.this, "Success!", Toast.LENGTH_SHORT).show();
                                    Intent activityIntent = new Intent(ActivityLogin.this, MainActivity.class);
                                    activityIntent.putExtra("KeepLoged", "KeepLoged");
                                    ActivityLogin.this.finish();
                                    ActivityLogin.this.startActivity(activityIntent);
                                } else {
                                    Toast.makeText(ActivityLogin.this, "Error! ", Toast.LENGTH_SHORT).show();
                                    Intent activityIntent = new Intent(ActivityLogin.this, ActivityLogin.class);
                                    ActivityLogin.this.finish();
                                    ActivityLogin.this.startActivity(activityIntent);

                                }
                            }
                        });
                    }
                }
            }
        });
    }

    public void loginUser() {
        mAuth.signInWithEmailAndPassword(mailLogin, passwordLogin).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    user = mAuth.getCurrentUser();
                    Toast.makeText(ActivityLogin.this, "Success! login completed", Toast.LENGTH_SHORT).show();
                    Intent activityIntent = new Intent(ActivityLogin.this, MainActivity.class);
                    activityIntent.putExtra("KeepLoged","KeepLoged");
                    ActivityLogin.this.finish();
                    ActivityLogin.this.startActivity(activityIntent);
                } else {
                    Toast.makeText(ActivityLogin.this, "Error! These credentials do not match our records", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}