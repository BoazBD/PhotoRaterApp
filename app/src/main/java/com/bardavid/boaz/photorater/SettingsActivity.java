package com.bardavid.boaz.photorater;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingsActivity extends AppCompatActivity {

    private String name, id;
    private Button sign_outBtn, saveBtn;
    private TextView savedTxt, showName;
    private RadioGroup agePreferenceRadio, genderPreferenceRadio;
    private String agePreference,genderPreference;
    private BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        showName = findViewById(R.id.showName);
        sign_outBtn = findViewById(R.id.sign_out);
        genderPreferenceRadio=findViewById(R.id.genderPreferenceRadio);
        agePreferenceRadio=findViewById(R.id.agePreferenceRadio);
        saveBtn=findViewById(R.id.saveBtn);
        savedTxt=findViewById(R.id.savedTxt);

        showNavigationBar();
        loadPreferences();
        checkCorrectRadioButtons();
        showName.setText("Connected to: " + name);
        sign_outBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //clears preferences
                SharedPreferences sharedPreferences= getSharedPreferences(Prefs.PREFS_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor=sharedPreferences.edit();
                editor.clear().commit();
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                //resets activity stack
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePreferences();
                DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference().child(Prefs.PUBLIC);
                databaseReference.child(Prefs.PEOPLE).child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //updates user preferences in database
                        PersonModel person=snapshot.getValue(PersonModel.class);
                        person.setGenderPreference(genderPreference);
                        person.setAgePreference(agePreference);
                        databaseReference.child(Prefs.PEOPLE).child(id).setValue(person);
                        saveBtn.setVisibility(View.INVISIBLE);
                        savedTxt.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.settings);
    }

    public void genderPreferenceButtonHandler(View view){
        switch (genderPreferenceRadio.getCheckedRadioButtonId()){
            case R.id.everybodyBtn:
                genderPreference=Prefs.ALL_GENDERS;
                saveBtn.setVisibility(View.VISIBLE);
                savedTxt.setVisibility(View.INVISIBLE);
                break;
            case R.id.malesPreferenceBtn:
                genderPreference=Prefs.MALES_ONLY;
                saveBtn.setVisibility(View.VISIBLE);
                savedTxt.setVisibility(View.INVISIBLE);
                break;
            case R.id.femalesPreferenceBtn:
                genderPreference=Prefs.FEMALES_ONLY;
                saveBtn.setVisibility(View.VISIBLE);
                savedTxt.setVisibility(View.INVISIBLE);
                break;
        }
    }
    public void agePreferenceButtonHandler(View view) {
        switch (agePreferenceRadio.getCheckedRadioButtonId()) {
            case R.id.allAgesPreferenceBtn:
                agePreference = Prefs.ALL_AGES;
                saveBtn.setVisibility(View.VISIBLE);
                savedTxt.setVisibility(View.INVISIBLE);
                break;
            case R.id.agesUnder18:
                agePreference = Prefs.UNDER_18;
                saveBtn.setVisibility(View.VISIBLE);
                savedTxt.setVisibility(View.INVISIBLE);
                break;
            case R.id.ages18to29:
                agePreference = Prefs.BETWEEN_18_29;
                saveBtn.setVisibility(View.VISIBLE);
                savedTxt.setVisibility(View.INVISIBLE);
                break;
            case R.id.ages30up:
                agePreference = Prefs.OVER_30;
                saveBtn.setVisibility(View.VISIBLE);
                savedTxt.setVisibility(View.INVISIBLE);
                break;
        }
    }
    public void checkCorrectRadioButtons(){
        switch (genderPreference){
            case Prefs.ALL_GENDERS:
                genderPreferenceRadio.check(R.id.everybodyBtn);
                break;
            case Prefs.MALES_ONLY:
                genderPreferenceRadio.check(R.id.malesPreferenceBtn);
                break;
            case Prefs.FEMALES_ONLY:
                genderPreferenceRadio.check(R.id.femalesPreferenceBtn);
                break;
        }
        switch (agePreference){
            case Prefs.ALL_AGES:
                agePreferenceRadio.check(R.id.allAgesPreferenceBtn);
                break;
            case Prefs.UNDER_18:
                agePreferenceRadio.check(R.id.agesUnder18);
                break;
            case Prefs.BETWEEN_18_29:
                agePreferenceRadio.check(R.id.ages18to29);
                break;
            case Prefs.OVER_30:
                agePreferenceRadio.check(R.id.ages30up);
                break;
        }
    }
    public void showNavigationBar() {
        bottomNavigationView = findViewById(R.id.bottom_menu);
        bottomNavigationView.setSelectedItemId(R.id.settings);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.upload:
                        Intent intent = new Intent(SettingsActivity.this, ChoosePhotoActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.graph:
                        intent = new Intent(SettingsActivity.this, MyRatingsActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.home:
                        intent = new Intent(SettingsActivity.this, RateActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.settings:
                        return true;
                }
                return false;
            }
        });
    }
    public void savePreferences(){
        SharedPreferences sharedPreferences= getSharedPreferences(Prefs.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString(Prefs.GENDER_PREFERENCE,genderPreference);
        editor.putString(Prefs.AGE_PREFERENCE,agePreference);
        editor.apply();
    }
    public void loadPreferences(){
        SharedPreferences sharedPreferences= getSharedPreferences(Prefs.PREFS_NAME, MODE_PRIVATE);
        name=sharedPreferences.getString(Prefs.NAME,Prefs.ERROR_VALUE);
        id=sharedPreferences.getString(Prefs.ID,Prefs.ERROR_VALUE);
        genderPreference=sharedPreferences.getString(Prefs.GENDER_PREFERENCE,Prefs.ALL_GENDERS);
        agePreference=sharedPreferences.getString(Prefs.AGE_PREFERENCE,Prefs.ALL_AGES);
    }
}