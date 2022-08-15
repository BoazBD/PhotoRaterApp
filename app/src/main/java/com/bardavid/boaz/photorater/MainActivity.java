package com.bardavid.boaz.photorater;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private TextView nameTxtView,ageTxtView;
    private RadioGroup genderRadio,genderPreferenceRadio,agePreferenceRadio;
    private TextView illegalNameTxt,illegalAgeTxt,illegalGenderTxt;
    private Button startButton;
    private String gender;
    private String agePreference;
    private String genderPreference;
    private Dialog welcomeDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //sets up notification alerts for the future
        //checks if user already connected to account
        if(getSharedPreferences(Prefs.PREFS_NAME, MODE_PRIVATE).getString("name", null) != null){
            Intent intent = new Intent(MainActivity.this, RateActivity.class);
            intent.putExtra("from", "main_activity");
            startActivity(intent);
            return;
        }
        createNotification();
        showWelcomeDialog();
        setContentView(R.layout.activity_main);
        nameTxtView = findViewById(R.id.name);
        ageTxtView = findViewById(R.id.age);
        genderRadio = findViewById(R.id.genderRadio);
        genderPreferenceRadio = findViewById(R.id.genderPreferenceRadio);
        agePreferenceRadio = findViewById(R.id.agePreferenceRadio);
        illegalNameTxt=findViewById(R.id.illegalNameTxt);
        illegalAgeTxt=findViewById(R.id.illegalAgeTxt);
        illegalGenderTxt=findViewById(R.id.illegalGenderTxt);
        startButton = findViewById(R.id.startButton);
    }
    public void onStartBtnClicked(View view) {
        //If all input fields are proper, goes to ChoosePhoto activity
        illegalNameTxt.setVisibility(View.INVISIBLE);
        illegalAgeTxt.setVisibility(View.INVISIBLE);
        illegalGenderTxt.setVisibility(View.INVISIBLE);
        boolean allValid = true;
        if (!isValidName()) {
            illegalNameTxt.setVisibility(View.VISIBLE);
            allValid = false;
        }
        if (!isValidAge()) {
            illegalAgeTxt.setVisibility(View.VISIBLE);
            allValid = false;
        }
        if (genderRadio.getCheckedRadioButtonId() == -1) {
            illegalGenderTxt.setVisibility(View.VISIBLE);
            allValid = false;
        }
        if (allValid) {
            savePreferences();
            Intent intent = new Intent(MainActivity.this, ChoosePhotoActivity.class);
            intent.putExtra("from", "main_activity");
            startActivity(intent);
            finish();
        }
    }
    public boolean isValidName(){
        //Checks if name input is proper
        if(nameTxtView.getText().toString().trim().length()==0)
            return false;
        String invalids = "012345567890-=/*.,\"\\;:<>[]{}()~!@#$%^&*";
        for(int i=0;i<nameTxtView.getText().length();i++){
            if(invalids.indexOf(nameTxtView.getText().charAt(i))>-1)
                return false;
        }
        return true;
    }
    public boolean isValidAge(){
        //Checks if age input is proper
        if(ageTxtView.getText().length()<1)
            return false;
        String valids="0123456789";
        for(int i=0;i<ageTxtView.getText().length();i++){
            if(valids.indexOf(ageTxtView.getText().charAt(i))==-1)
                return false;
        }
        if(Integer.parseInt(ageTxtView.getText().toString())<14)
            return false;
        return true;
    }
    public void genderRadioButtonHandler(View view) {
        switch (genderRadio.getCheckedRadioButtonId()) {
            case R.id.male:
                gender = Prefs.MALE;
                break;
            case R.id.female:
                gender = Prefs.FEMALE;
                break;
            case R.id.other:
                gender = Prefs.OTHER;
                break;
        }
    }
    public void genderPreferenceButtonHandler(View view){
        switch (genderPreferenceRadio.getCheckedRadioButtonId()){
            case R.id.everybodyBtn:
                genderPreference=Prefs.ALL_GENDERS;
                break;
            case R.id.malesPreferenceBtn:
                genderPreference=Prefs.MALES_ONLY;
                break;
            case R.id.femalesPreferenceBtn:
                genderPreference=Prefs.FEMALES_ONLY;
                break;
        }
    }
    public void agePreferenceButtonHandler(View view){
        switch (agePreferenceRadio.getCheckedRadioButtonId()){
            case R.id.allAgesPreferenceBtn:
                agePreference = Prefs.ALL_AGES;
                break;
            case R.id.agesUnder18:
                agePreference=Prefs.UNDER_18;
                break;
            case R.id.ages18to29:
                agePreference=Prefs.BETWEEN_18_29;
                break;
            case R.id.ages30up:
                agePreference=Prefs.OVER_30;
                break;
        }
    }
    public void savePreferences(){
        //Saves the user information on the device
        SharedPreferences sharedPreferences=getSharedPreferences(Prefs.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString(Prefs.ID,Long.toString(System.currentTimeMillis()));
        editor.putString(Prefs.NAME,nameTxtView.getText().toString());
        editor.putString(Prefs.GENDER,gender);
        editor.putInt(Prefs.AGE,Integer.parseInt(ageTxtView.getText().toString()));
        editor.putInt(Prefs.RATINGS_COUNT,0);
        editor.putString(Prefs.GENDER_PREFERENCE,genderPreference);
        editor.putString(Prefs.AGE_PREFERENCE,agePreference);
        editor.putInt(Prefs.PHOTO_COUNT,0);
        editor.apply();
    }
    public void showWelcomeDialog(){
        welcomeDialog=new Dialog(this);
        welcomeDialog.setContentView(R.layout.welcome_dialog);
        welcomeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ImageView closeImageView=welcomeDialog.findViewById(R.id.closeImageView);
        Button letsStartBtn=welcomeDialog.findViewById(R.id.letsStartBtn);
        closeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                welcomeDialog.dismiss();
            }
        });
        letsStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                welcomeDialog.dismiss();
            }
        });
        welcomeDialog.show();
    }
    public void setUpNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name= "ReminderChannel";
            String description = "Channel for Photo Rater";
            int importance= NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel=new NotificationChannel("notifyPhotoRater",name,importance);
            channel.setDescription(description);

            NotificationManager notificationManager=getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    public void createNotification(){
        setUpNotificationChannel();

        Intent intent= new Intent(MainActivity.this, notificationService.class);
        PendingIntent pendingIntent=PendingIntent.getBroadcast(MainActivity.this,0,intent,PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager=(AlarmManager) getSystemService(ALARM_SERVICE);
        long curTime=System.currentTimeMillis();
        long milisecondsWaitingTime=1000*60*Prefs.MINUTES_WAIT_UNTIL_NOTIFICATION;
        alarmManager.set(AlarmManager.RTC_WAKEUP,curTime+milisecondsWaitingTime,pendingIntent);

    }
}
