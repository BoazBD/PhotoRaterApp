package com.bardavid.boaz.photorater;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyRatingsActivity extends AppCompatActivity {
    private DatabaseReference databaseRef;
    private List<PairRates> pairs =new ArrayList<>();
    private RecyclerView recyclerView;
    private BottomNavigationView bottomNavigationView;
    private TextView noPhotosText;
    private Button noPhotosBtn;
    private String name;
    private String id;
    private String gender;
    private int age;
    private int ratingsCount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_ratings);
        showNavigationBar();
        loadPreferences();
        //id="1631977435177";
        recyclerView=findViewById(R.id.recyclerView);
        noPhotosText=findViewById(R.id.noPhotosText);
        noPhotosBtn=findViewById(R.id.noPhotosBtn);
        noPhotosBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MyRatingsActivity.this , ChoosePhotoActivity.class);
                startActivity(intent);
            }
        });
        databaseRef = FirebaseDatabase.getInstance().getReference(Prefs.PUBLIC).child(Prefs.PEOPLE).child(id).child("pairCodes");
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String>pairCodes=new ArrayList<>();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    String pairCode=postSnapshot.getValue(String.class);
                    pairCodes.add(pairCode);
                }
                convertCodesToPairs(pairCodes);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MyRatingsActivity.this, "Error",Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void convertCodesToPairs(List<String> codes){
        DatabaseReference dtReference = FirebaseDatabase.getInstance().getReference().child(Prefs.PUBLIC);
        dtReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(String code:codes) {
                    if (snapshot.child(Prefs.PAIRS_RATED).hasChild(code)) {
                        pairs.add(snapshot.child(Prefs.PAIRS_RATED).child(code).getValue(PairRates.class));
                    } else if(snapshot.child(Prefs.PAIRS_NEEDS_RATING).hasChild(code)){
                        pairs.add(snapshot.child(Prefs.PAIRS_NEEDS_RATING).child(code).getValue(PairRates.class));
                    }
                }
                setUpRecyclerView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void setUpRecyclerView() {
        if (pairs.size() == 0) {
            noPhotosText.setVisibility(View.VISIBLE);
            noPhotosBtn.setVisibility(View.VISIBLE);
        } else {
            RecyclerViewAdapter myAdapter = new RecyclerViewAdapter(this, pairs, ratingsCount);
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
            dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.recyclerview_divider));
            recyclerView.addItemDecoration(dividerItemDecoration);
            recyclerView.setAdapter(myAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
    }
        public void showNavigationBar() {
            bottomNavigationView = findViewById(R.id.bottom_menu);
            bottomNavigationView.setSelectedItemId(R.id.graph);
            bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.upload:
                            Intent intent = new Intent(MyRatingsActivity.this, ChoosePhotoActivity.class);
                            startActivity(intent);
                            return true;
                        case R.id.graph:
                            return true;
                        case R.id.home:
                            intent = new Intent(MyRatingsActivity.this, RateActivity.class);
                            startActivity(intent);
                            return true;
                        case R.id.settings:
                            intent = new Intent(MyRatingsActivity.this, SettingsActivity.class);
                            startActivity(intent);
                            return true;
                    }
                    return false;
                }
            });
        }
    public void loadPreferences(){
        SharedPreferences sharedPreferences= getSharedPreferences(Prefs.PREFS_NAME, MODE_PRIVATE);
        name=sharedPreferences.getString(Prefs.NAME,Prefs.ERROR_VALUE);
        id=sharedPreferences.getString(Prefs.ID,Prefs.ERROR_VALUE);
        gender= sharedPreferences.getString(Prefs.GENDER,Prefs.ERROR_VALUE);
        age=sharedPreferences.getInt(Prefs.AGE,0);
        ratingsCount=sharedPreferences.getInt(Prefs.RATINGS_COUNT,0);
    }

}