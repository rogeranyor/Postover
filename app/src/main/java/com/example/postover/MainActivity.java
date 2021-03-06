package com.example.postover;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.postover.Model.Client;
import com.example.postover.Model.ToDoNote;
import com.example.postover.SlideFragments.AdapterSlide;
import com.example.postover.ui.ActivityLogin;
import com.example.postover.ui.DialogCloseListener;
import com.example.postover.ui.SettingsActivity;
import com.example.postover.ui.TODO.TodoFragment;
import com.example.postover.ui.data.usernameDialog;
import com.example.postover.ui.home.HomeFragment;
import com.example.postover.ui.CALENDAR.CalendarFragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;


public class MainActivity extends AppCompatActivity implements DialogCloseListener {

    public static final String NOTIFICATION_CHANNEL_ID = "notifyLemubit";
    private final static String default_notification_channel_id = "default";

    private AppBarConfiguration mAppBarConfiguration;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private TextView textViewUsername;

    private EditText loginMail, loginPassword;
    private String mailLogin, passwordLogin;

    private static TodoFragment todoFragment;
    private HomeFragment homeFragment;
    private CalendarFragment calendarFragment;

    //cositas del firebase
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    public FirebaseUser user;

    private int GOOGLE_SIGN_IN = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayShowTitleEnabled(false);
        mAuth = FirebaseAuth.getInstance();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        user = mAuth.getCurrentUser();

        createNotificationChannel();
       // createTODOReseterAlarm();
        try {
            if (getIntent().getExtras().getString("Login") != null) {
                Intent intent = new Intent(MainActivity.this, ActivityLogin.class);
                MainActivity.this.finish();
                MainActivity.this.startActivity(intent);
            }
           else if (getIntent().getExtras().getString("KeepLoged") != null) {
                createFragments();
            } else if (getIntent().getExtras().getString("ChangeUs") != null) {
                createFragments();
            } else {
                createFragments();
            }
        } catch (NullPointerException e) {
            //createFragments();
        }
    }

    public void signOut(View v) {
        mAuth.signOut();
        Intent mainIntent = new Intent(MainActivity.this, ActivityLogin.class);
        MainActivity.this.startActivity(mainIntent);
        MainActivity.this.finish();
    }

    public void createFragments() {
        List<Fragment> fragmentList = new ArrayList<>();
        todoFragment = new TodoFragment();
        fragmentList.add(todoFragment);
        homeFragment = new HomeFragment();
        fragmentList.add(homeFragment);
        calendarFragment = new CalendarFragment();
        fragmentList.add(calendarFragment);
        AdapterSlide adapter = new AdapterSlide(getSupportFragmentManager(), getLifecycle(), fragmentList);
        ViewPager2 viewPager2 = findViewById(R.id.view_pager2);
        viewPager2.setAdapter(adapter);
        viewPager2.setCurrentItem(1);


        updateNav();
    }

    public void updateNav() {
        mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    Log.d("firebase", String.valueOf(task.getResult().getValue()));
                    Client client = task.getResult().getValue(Client.class);
                    TextView nameNavHead = (TextView) findViewById(R.id.name_navhead);
                    if (getIntent().getExtras().getString("ChangeUs") != null) {
                        Bundle bundle = getIntent().getExtras();
                        String message = bundle.getString("ChangeUs");
                        nameNavHead.setText(message);
                    } else {
                        nameNavHead.setText(client.getName());
                    }
                    TextView emailNavHead = (TextView) findViewById(R.id.emailNavhead);
                    emailNavHead.setText(client.getMail());
                    ImageView imageNavhead = (ImageView) findViewById(R.id.image_navHead);

                    try {
                        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(MainActivity.this);
                        Uri photo = account.getPhotoUrl();

                        //imageNavhead.setImageURI(photo);

                        //Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), photo);
                        // imageNavhead.setImageBitmap(bitmap);
                    } catch (Exception e) {

                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);

                if (account != null) {
                    AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                //Datos realetime database
                            } else {
                                Toast.makeText(MainActivity.this, "Error! Google authentification exploted", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            } catch (ApiException e) {
                Toast.makeText(this, "Error! Google authentification exploted", Toast.LENGTH_SHORT).show();
            }
        }
        if (resultCode == 1) {
            handleDialogClose(dialog, "HomeNote");

        }
    }

    public void openDrawer(View v) {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.openDrawer(GravityCompat.START);
    }

    public void loginUser() {
        mAuth.signInWithEmailAndPassword(mailLogin, passwordLogin).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    user = mAuth.getCurrentUser();
                    Toast.makeText(MainActivity.this, "Success! login completed", Toast.LENGTH_SHORT).show();
                    createFragments();
                    dialog.dismiss();
                } else {
                    Toast.makeText(MainActivity.this, "Error! These credentials do not match our records", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public void handleDialogClose(DialogInterface dialog, String note) {
        switch (note) {
            case "Todo":
                todoFragment.getList();
                break;
            case "HomeNote":
                homeFragment.getList();
                break;
            case "CalendarNote":
                calendarFragment.getList();
                break;

        }
    }

    public void settingsJumper(View v) {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        MainActivity.this.finish();
        MainActivity.this.startActivity(intent);
    }

    @Override
    protected void onNightModeChanged(int mode) {
        super.onNightModeChanged(mode);
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "LemubitReminderChannel";
            String description = "Channel for Lemubit Reminder";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            notificationChannel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    public static void creaNotificacion(long when, String title, String content, Context context) {

        Intent notificationIntent = new Intent(context, NotificationPublisher.class);
        notificationIntent.putExtra("id", (int) when);
        notificationIntent.putExtra("title", title);
        notificationIntent.putExtra("content", content);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) System.currentTimeMillis(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

        long hourInMillis = 60 * 60 * 1000;

        alarmManager.set(AlarmManager.RTC_WAKEUP, when - hourInMillis, pendingIntent);
    }

    /*public void createTODOReseterAlarm() {
        //System request code
        int DATA_FETCHER_RC = 123;
        //Create an alarm manager
        AlarmManager mAlarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        //Create the time of day you would like it to go off. Use a calendar
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);

        //Create an intent that points to the receiver. The system will notify the app about the current time, and send a broadcast to the app
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, DATA_FETCHER_RC,intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //initialize the alarm by using inexactrepeating. This allows the system to scheduler your alarm at the most efficient time around your
        //set time, it is usually a few seconds off your requested time.
        // you can also use setExact however this is not recommended. Use this only if it must be done then.
        //Also set the interval using the AlarmManager constants
        mAlarmManager.setInexactRepeating(AlarmManager.RTC,calendar.getTimeInMillis(),AlarmManager.INTERVAL_DAY, pendingIntent);

    }

    public static void reseterTODO() {
        todoFragment.Reseter();
    }
    //This is the broadcast receiver you create where you place your logic once the alarm is run. Once the system realizes your alarm should be run, it will communicate to your app via the BroadcastReceiver. You must implement onReceive.
    public static class AlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Your code once the alarm is set off goes here
            MainActivity.reseterTODO();
            // You can use an intent filter to filter the specified intent
        }
    }*/
}
