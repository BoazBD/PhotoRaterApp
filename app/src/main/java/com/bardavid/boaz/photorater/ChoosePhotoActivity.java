package com.bardavid.boaz.photorater;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.exifinterface.media.ExifInterface;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ChoosePhotoActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private Boolean isUserFirstPhoto;
    private Button chooseBtn1, chooseBtn2, uploadBtn, rotateBtn1, rotateBtn2,infoBtn;
    private ImageView blankImage1, blankImage2;
    private ProgressBar progressBar;
    private BottomNavigationView bottomNavigationView;
    private Uri ImageUri1, ImageUri2;
    private StorageReference storageRef;
    private DatabaseReference databasePersonRef, databaseImageRef;
    private StorageTask uploadTask;
    private PersonModel person;
    private String name, id, gender, genderPreference, agePreference, url1, url2;
    private int age, photoCount, chosenPhoto;
    private boolean photo1Chosen, photo2Chosen, photo1Uploaded;
    private float rotationPhoto1, rotationPhoto2;
    private Dialog focusInfoDialog;
    private EditText focusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_photo);
        setSupportActionBar(findViewById(R.id.toolbar));
        Intent intent = getIntent();
        isUserFirstPhoto = intent.getStringExtra("from") != null && intent.getStringExtra("from").equals("main_activity");
        chooseBtn1 = findViewById(R.id.chooseBtn1);
        chooseBtn2 = findViewById(R.id.chooseBtn2);
        uploadBtn = findViewById(R.id.uploadBtn);
        focusInfoDialog = new Dialog(this);
        blankImage1 = findViewById(R.id.blankImage1);
        blankImage2 = findViewById(R.id.blankImage2);
        infoBtn=findViewById(R.id.infoBtn);
        rotateBtn1 = findViewById(R.id.rotatePhoto1);
        rotateBtn2 = findViewById(R.id.rotatePhoto2);
        rotateBtn1.setEnabled(false);
        rotateBtn2.setEnabled(false);
        progressBar = findViewById(R.id.progressBar);
        focusText=findViewById(R.id.focusEditTxt);
        bottomNavigationView = findViewById(R.id.bottom_menu);
        chosenPhoto = 0;
        rotationPhoto2 = 0;
        rotationPhoto2 = 0;
        photo1Chosen = false;
        photo2Chosen = false;
        photo1Uploaded = false;
        loadPreferences();
        if (!isUserFirstPhoto) {
            showNavigationBar();
        } else {
            bottomNavigationView.setVisibility(View.INVISIBLE);
        }
        storageRef = FirebaseStorage.getInstance().getReference("uploads");
        databasePersonRef = FirebaseDatabase.getInstance().getReference().child(Prefs.PUBLIC);
        chooseBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chosenPhoto = 1;
                openFileChooser();
            }
        });
        chooseBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chosenPhoto = 2;
                openFileChooser();
            }
        });
        blankImage1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chosenPhoto = 1;
                openFileChooser();
            }
        });
        blankImage2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chosenPhoto = 2;
                openFileChooser();
            }
        });
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uploadTask != null && uploadTask.isInProgress()) {
                    Toast.makeText(ChoosePhotoActivity.this, "upload in progress", Toast.LENGTH_SHORT).show();
                } else {
                    uploadBtn.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    uploadFile();
                }
            }
        });
        rotateBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotationPhoto1 = (rotationPhoto1 - 90) % 360;
                Picasso.get().load(ImageUri1).rotate(rotationPhoto1).into(blankImage1);
            }
        });
        rotateBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotationPhoto2 = (rotationPhoto2 - 90) % 360;
                Picasso.get().load(ImageUri2).rotate(rotationPhoto2).into(blankImage2);
            }
        });
        infoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                focusInfoDialog.setContentView(R.layout.focus_info_dialog);
                focusInfoDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                ImageView closeImageView = focusInfoDialog.findViewById(R.id.closeImageView);
                Button gotItBtn = focusInfoDialog.findViewById(R.id.gotItBtn);
                closeImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        focusInfoDialog.dismiss();
                    }
                });
                gotItBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        focusInfoDialog.dismiss();
                    }
                });
                focusInfoDialog.show();
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //Loads the chosen picture on screen
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            if (chosenPhoto == 1) {
                ImageUri1 = data.getData();
                Picasso.get().load(ImageUri1).into(blankImage1);
                photo1Chosen = true;
                rotateBtn1.setEnabled(true);
            } else if (chosenPhoto == 2) {
                ImageUri2 = data.getData();
                Picasso.get().load(ImageUri2).into(blankImage2);
                photo2Chosen = true;
                rotateBtn2.setEnabled(true);
            }
            if (photo1Chosen && photo2Chosen)
                uploadBtn.setVisibility(View.VISIBLE);
        }
    }

    public String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFile() {
        //Uploads the chosen image to FireBase Storage
        if (ImageUri1 != null && ImageUri2 != null) {
            //Creates custom string for image's storage name
            lockButtons();
            String randDigits = String.valueOf((int) (Math.random() * 1000));
            final StorageReference fileReference1 = storageRef.child(id).child(randDigits +
                    "." + getFileExtension(ImageUri1));
            uploadTask = fileReference1.putFile(ImageUri1);
            Task<Uri> urlTask1 = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful())
                        throw task.getException();
                    return fileReference1.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        //Uploaded successfully, now uploading 2nd photo to storage
                        Uri downloadUri = task.getResult();
                        url1 = downloadUri.toString();
                        photo1Uploaded = true;
                        String randDigits = String.valueOf((int) (Math.random() * 1000));
                        final StorageReference fileReference2 = storageRef.child(id).child(randDigits +
                                "." + getFileExtension(ImageUri2));
                        uploadTask = fileReference2.putFile(ImageUri2);
                        Task<Uri> urlTask2 = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task task) throws Exception {
                                if (!task.isSuccessful())
                                    throw task.getException();
                                return fileReference2.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    Uri downloadUri = task.getResult();
                                    url2 = downloadUri.toString();
                                    uploadToDb();
                                } else {
                                    Toast.makeText(ChoosePhotoActivity.this, "Failed uploading", Toast.LENGTH_LONG).show();
                                    progressBar.setVisibility(View.INVISIBLE);
                                    uploadBtn.setVisibility(View.VISIBLE);
                                    unlockButtons();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(ChoosePhotoActivity.this, "Failed uploading", Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.INVISIBLE);
                        uploadBtn.setVisibility(View.VISIBLE);
                        unlockButtons();
                    }
                }
            });
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_LONG).show();
        }
    }

    public void uploadToDb() {
        databasePersonRef.child(Prefs.PEOPLE).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(id)) {
                    person = snapshot.child(id).getValue(PersonModel.class);
                    person.addPairID(url1.substring(url1.length() - 10));
                } else {
                    person = new PersonModel(name, age, gender, genderPreference, agePreference);
                    person.setId(id);
                    List<String> pairIDs = new ArrayList<>();
                    pairIDs.add(url1.substring(url1.length() - 10));
                    person.setPairCodes(pairIDs);
                    isUserFirstPhoto = true;
                }
                //Updates the person and also uploads the new image to database
                databasePersonRef.child(Prefs.PEOPLE).child(id).setValue(person);
                databaseImageRef = FirebaseDatabase.getInstance().getReference().child(Prefs.PUBLIC);
                PairRates newPair = new PairRates(url1, url2, rotationPhoto1, rotationPhoto2,focusText.getText().toString());
                newPair.setOwnerID(id);
                databaseImageRef.child(Prefs.PAIRS_NEEDS_RATING).child(newPair.getPairID()).setValue(newPair);
                progressBar.setVisibility(View.GONE);
                blankImage1.setImageURI(null);
                blankImage2.setImageURI(null);
                updatePreference();
                moveToRate();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChoosePhotoActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void lockButtons() {
        chooseBtn1.setEnabled(false);
        chooseBtn2.setEnabled(false);
        uploadBtn.setEnabled(false);
        rotateBtn1.setEnabled(false);
        rotateBtn2.setEnabled(false);
        focusText.setEnabled(false);
    }

    public void unlockButtons() {
        chooseBtn1.setEnabled(true);
        chooseBtn2.setEnabled(true);
        uploadBtn.setEnabled(true);
        rotateBtn1.setEnabled(true);
        rotateBtn2.setEnabled(true);
        focusText.setEnabled(true);
    }

    public void moveToRate() {
        Intent intent = new Intent(ChoosePhotoActivity.this, RateActivity.class);
        intent.putExtra("isUserFirstPhoto", isUserFirstPhoto);
        startActivity(intent);
    }

    public void showNavigationBar() {
        //Shows the navigation bar on bottom of screen
        bottomNavigationView.setSelectedItemId(R.id.upload);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.upload:
                        return true;
                    case R.id.graph:
                        Intent intent = new Intent(ChoosePhotoActivity.this, MyRatingsActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.home:
                        intent = new Intent(ChoosePhotoActivity.this, RateActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.settings:
                        intent = new Intent(ChoosePhotoActivity.this, SettingsActivity.class);
                        startActivity(intent);
                        return true;
                }
                return false;
            }
        });
    }

    public void updatePreference() {
        SharedPreferences sharedPreferences = getSharedPreferences(Prefs.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Prefs.PHOTO_COUNT, photoCount + 1);
        editor.apply();
    }

    public void loadPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(Prefs.PREFS_NAME, MODE_PRIVATE);
        name = sharedPreferences.getString(Prefs.NAME, Prefs.ERROR_VALUE);
        id = sharedPreferences.getString(Prefs.ID, Prefs.ERROR_VALUE);
        gender = sharedPreferences.getString(Prefs.GENDER, Prefs.ERROR_VALUE);
        genderPreference = sharedPreferences.getString(Prefs.GENDER_PREFERENCE, Prefs.ALL_GENDERS);
        agePreference = sharedPreferences.getString(Prefs.AGE_PREFERENCE, Prefs.ALL_GENDERS);
        age = sharedPreferences.getInt(Prefs.AGE, 0);
        photoCount = sharedPreferences.getInt(Prefs.PHOTO_COUNT, 0);
    }

}