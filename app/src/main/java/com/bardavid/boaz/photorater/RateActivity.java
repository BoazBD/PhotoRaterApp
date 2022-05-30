package com.bardavid.boaz.photorater;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class RateActivity extends AppCompatActivity {


    private Button votePhoto1Btn, votePhoto2Btn;
    private BottomNavigationView bottomNavigationView;
    private ImageView image1, image2, image3, image4, reportImg;
    private Boolean isShowingImage1;
    private TextView numRatings;
    private ProgressBar progressBar;
    private List<PairRates> uploads = new ArrayList<>();
    private PairRates currPair;
    private int indexOfPairToRate;
    private int indexOfPairToLoad;
    private DatabaseReference databaseRef;
    private TextView focusTxt;
    private String name;
    private String id;
    private String gender;
    private int age;
    private int ratingsCount, photoCount;
    private Dialog instructionsDialog;
    private Animation slideLeftFromCenter;
    private Animation slideLeftToCenter;
    private LinearLayout images, backgroundImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate);
        loadPreferences();
        showNavigationBar();
        isShowingImage1 = Boolean.TRUE;
        instructionsDialog = new Dialog(this);
        if (ratingsCount == 0)
            openInstructionsDialog();
        indexOfPairToRate = ratingsCount;
        indexOfPairToLoad = 0;
        votePhoto1Btn = findViewById(R.id.votePhoto1Btn);
        votePhoto2Btn = findViewById(R.id.votePhoto2Btn);
        focusTxt =findViewById(R.id.focusTxt);
        reportImg = findViewById(R.id.reportImg);
        image1 = findViewById(R.id.image1);
        image2 = findViewById(R.id.image2);
        image3 = findViewById(R.id.image3);
        image4 = findViewById(R.id.image4);
        images = findViewById(R.id.imagesLayout);
        backgroundImages = findViewById(R.id.backgroundImages);
        progressBar = findViewById(R.id.progressBar);
        numRatings = findViewById(R.id.numRatings);
        numRatings.setText(ratingsCount + "/" + (Prefs.NUMBER_OF_RATINGS_NEEDED_FOR_EACH_PHOTO * photoCount));
        disableButtons();
        slideLeftFromCenter = AnimationUtils.loadAnimation(this, R.anim.slide_left_from_center);
        slideLeftToCenter = AnimationUtils.loadAnimation(this, R.anim.slide_left_to_center);

        reportImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(RateActivity.this);
                builder.setTitle("Do you want to report this person?");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        currPair.addReport();
                        nextPair();
                        loadFirstPhoto(0); //grade 0 means don't add any grade to the photo
                        Toast.makeText(RateActivity.this, "Person reported successfully", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        databaseRef = FirebaseDatabase.getInstance().getReference().child(Prefs.PUBLIC);
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Creates a list of images that the user is eligible to rate. max size of 200
                //First adds images that need rating, if there's more room then adds also rated photos
                int photoCounter = 0;
                for (DataSnapshot postSnapshot : snapshot.child(Prefs.PAIRS_NEEDS_RATING).getChildren()) {
                    PairRates pair = postSnapshot.getValue(PairRates.class);
                    PersonModel imageOwner = snapshot.child(Prefs.PEOPLE).child(pair.getOwnerID()).getValue(PersonModel.class);
                    if (imageOwner != null && !(imageOwner.getId().equals(id)) && isLegalRater(imageOwner)) {
                        uploads.add(pair);
                        photoCounter++;
                    }
                    if (photoCounter >= 200)
                        break;
                }
                for (DataSnapshot postSnapshot : snapshot.child(Prefs.PAIRS_RATED).getChildren()) {
                    PairRates pair = postSnapshot.getValue(PairRates.class);
                    PersonModel imageOwner = snapshot.child(Prefs.PEOPLE).child(pair.getOwnerID()).getValue(PersonModel.class);
                    if (imageOwner != null && !(imageOwner.getId().equals(id)) && isLegalRater(imageOwner)) {
                        uploads.add(pair);
                        photoCounter++;
                    }
                    if (photoCounter >= 200)
                        break;
                }
                if (indexOfPairToRate >= uploads.size())
                    indexOfPairToRate = 0;
                currPair = uploads.get(indexOfPairToRate);
                for (int i = 0; i < 5; i++)
                    loadNextPhotoFromCache();
                loadFirstPhoto(0);
                //grade 0 means dont add rating to currPhoto
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RateActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    public void btn1(View v) {
        rateButtonClicked(1);
    }

    public void btn2(View v) {
        rateButtonClicked(2);
    }

    public void rateButtonClicked(int grade) {
        //Adds chosen rating to database, then loads next photo
        disableButtons();
        if (grade == 1) {
            votePhoto1Btn.setBackgroundResource(R.mipmap.heart_btn_clicked);
        } else if (grade == 2) {
            votePhoto2Btn.setBackgroundResource(R.mipmap.heart_btn_clicked);
        }
        try {
            currPair.addRating(grade);
        } catch (Exception e) {
            System.out.println("EXECTION: " + e.getMessage());
        }
        nextPair();
        loadFirstPhoto(grade);
    }

    public void loadFirstPhoto(int grade) {
        //first tries to fetch next photo from cache (it tried to load to cache 5 next photos previously)
        Picasso.get().load(currPair.getUrl1()).networkPolicy(NetworkPolicy.OFFLINE).rotate(currPair.getRotation1()).into(image3, new Callback() {
            @Override
            public void onSuccess() {
                //Toast.makeText(RateActivity.this, "SUCCESS OFFLINE", Toast.LENGTH_SHORT).show();
                loadSecondPhoto(grade,true); //true->should animate
            }

            @Override
            public void onError(Exception e) { //could not load from cache, now loads from online
                progressBar.setVisibility(View.VISIBLE);
                backgroundImages.setVisibility(View.INVISIBLE);
                images.setVisibility(View.INVISIBLE);
                Picasso.get().load(currPair.getUrl1()).rotate(currPair.getRotation1()).into(image3, new Callback() {
                    @Override
                    public void onSuccess() {
                        loadSecondPhoto(grade,false);
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(RateActivity.this, "Error loading image", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public void loadSecondPhoto(int grade,boolean loadedPic1) {
        boolean shouldAnimate = loadedPic1 && image2.getDrawable() != null;
        //first tries to fetch next photo from cache (it tried to load to cache 5 next photos previously)
        Picasso.get().load(currPair.getUrl2()).networkPolicy(NetworkPolicy.OFFLINE).rotate(currPair.getRotation2()).into(image4, new Callback() {
            @Override
            public void onSuccess() {
                //Toast.makeText(RateActivity.this, "SUCCESS OFFLINE", Toast.LENGTH_SHORT).show();
                if (shouldAnimate) {
                    animateImageSwitch(grade);
                } else {
                    images.setVisibility(View.INVISIBLE);
                    backgroundImages.setVisibility(View.VISIBLE);
                    handleSuccessfulImageLoad(grade);
                }
            }

            @Override
            public void onError(Exception e) { //could not load from cache, now loads from online
                progressBar.setVisibility(View.VISIBLE);
                Picasso.get().load(currPair.getUrl2()).into(image4, new Callback() {
                    @Override
                    public void onSuccess() {
                        images.setVisibility(View.INVISIBLE);
                        backgroundImages.setVisibility(View.VISIBLE);
                        handleSuccessfulImageLoad(grade);
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(RateActivity.this, "Error loading image", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        loadNextPhotoFromCache();
    }

    public void animateImageSwitch(int grade) {

        images.startAnimation(slideLeftFromCenter);
        backgroundImages.setVisibility(View.VISIBLE);
        backgroundImages.startAnimation(slideLeftToCenter);
        images.setVisibility(View.INVISIBLE);
        slideLeftToCenter.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                handleSuccessfulImageLoad(grade);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void handleSuccessfulImageLoad(int grade) {
        if(currPair.getFocus()!=null) {
            focusTxt.setText("Focus on: " + currPair.getFocus());
            focusTxt.setVisibility(View.VISIBLE);
        }
        else
            focusTxt.setVisibility(View.INVISIBLE);
        LinearLayout temp0 = images;
        ImageView temp1=image1;
        ImageView temp2=image2;
        images = backgroundImages;
        image1=image3;
        image2=image4;
        backgroundImages = temp0;
        image3=temp1;
        image4=temp2;
        progressBar.setVisibility(View.GONE);
        if (grade != 0) {
            votePhoto1Btn.setBackgroundResource(R.mipmap.heart_btn); //resets clicked btn's color
            votePhoto2Btn.setBackgroundResource(R.mipmap.heart_btn);
            ratingsCount++;
            updatePreferences();
            if (ratingsCount == Prefs.NUMBER_OF_RATINGS_NEEDED_FOR_EACH_PHOTO * photoCount) {
                Intent intent = new Intent(RateActivity.this, MyRatingsActivity.class);
                startActivity(intent);
            }
        }
        if (ratingsCount > Prefs.NUMBER_OF_RATINGS_NEEDED_FOR_EACH_PHOTO * photoCount)
            numRatings.setVisibility(View.INVISIBLE);
        numRatings.setText(ratingsCount + "/" + (Prefs.NUMBER_OF_RATINGS_NEEDED_FOR_EACH_PHOTO * photoCount));
        enableButtons();
    }

    public void nextPair() {
        //Places next photo in line in currPhoto
        indexOfPairToRate++;
        if (indexOfPairToRate == uploads.size()) {
            Toast.makeText(RateActivity.this, "Out of people!", Toast.LENGTH_LONG).show();
            switchToMyRatingsActivity();
            return;
        }
        currPair = uploads.get(indexOfPairToRate);
    }

    public Boolean isLegalRater(PersonModel owner) {
        //Returns true if the user fits the preferences of photo owner
        String ownerGenderPreference = owner.getGenderPreference();
        String ownerAgePreference = owner.getAgePreference();
        switch (ownerGenderPreference) {
            case Prefs.MALES_ONLY:
                if (gender != Prefs.MALE)
                    return false;
                break;
            case Prefs.FEMALES_ONLY:
                if (gender != Prefs.FEMALE)
                    return false;
                break;
        }
        switch (ownerAgePreference) {
            case Prefs.UNDER_18:
                if (age > 18)
                    return false;
                break;
            case Prefs.BETWEEN_18_29:
                if (age < 18 || age > 29)
                    return false;
                break;
            case Prefs.OVER_30:
                if (age < 30)
                    return false;
                break;
        }
        return true;
    }

    public void switchToMyRatingsActivity() {
        Intent intent = new Intent(RateActivity.this, MyRatingsActivity.class);
        startActivity(intent);
        finish();
    }

    public void showNavigationBar() {
        bottomNavigationView = findViewById(R.id.bottom_menu);
        bottomNavigationView.setSelectedItemId(R.id.home);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.upload:
                        Intent intent = new Intent(RateActivity.this, ChoosePhotoActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.graph:
                        intent = new Intent(RateActivity.this, MyRatingsActivity.class);
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.home:
                        return true;
                    case R.id.settings:
                        intent = new Intent(RateActivity.this, SettingsActivity.class);
                        startActivity(intent);
                        return true;
                }
                return false;
            }
        });
    }

    public void loadPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(Prefs.PREFS_NAME, MODE_PRIVATE);
        name = sharedPreferences.getString(Prefs.NAME, Prefs.ERROR_VALUE);
        id = sharedPreferences.getString(Prefs.ID, Prefs.ERROR_VALUE);
        gender = sharedPreferences.getString(Prefs.GENDER, Prefs.ERROR_VALUE);
        age = sharedPreferences.getInt(Prefs.AGE, 0);
        ratingsCount = sharedPreferences.getInt(Prefs.RATINGS_COUNT, 0);
        photoCount = sharedPreferences.getInt(Prefs.PHOTO_COUNT, 0);
    }

    public void updatePreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(Prefs.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Prefs.RATINGS_COUNT, ratingsCount);
        editor.apply();
    }

    public void openInstructionsDialog() {
        instructionsDialog.setContentView(R.layout.instructions_layout_dialog);
        instructionsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ImageView closeImageView = instructionsDialog.findViewById(R.id.closeImageView);
        Button gotItBtn = instructionsDialog.findViewById(R.id.gotItBtn);
        closeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                instructionsDialog.dismiss();
            }
        });
        gotItBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                instructionsDialog.dismiss();
            }
        });
        instructionsDialog.show();
    }

    public void disableButtons() {
        votePhoto1Btn.setEnabled(false);
        votePhoto2Btn.setEnabled(false);
        reportImg.setEnabled(false);
    }

    public void enableButtons() {
        votePhoto1Btn.setEnabled(true);
        votePhoto2Btn.setEnabled(true);
        reportImg.setEnabled(true);
    }

    public void loadNextPhotoFromCache() {
        if (indexOfPairToLoad <= 200 && indexOfPairToLoad < uploads.size() - 1) {
            Picasso.get().load(uploads.get(indexOfPairToLoad).getUrl1()).fetch(new Callback() {
                @Override
                public void onSuccess() {
                    //Toast.makeText(RateActivity.this,"finished loading photo"+indexOfPicToLoad,Toast.LENGTH_SHORT).show();
                    Picasso.get().load(uploads.get(indexOfPairToLoad).getUrl2()).fetch(new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });
                }

                @Override
                public void onError(Exception e) {

                }
            });
            indexOfPairToLoad++;
        }
    }
}
