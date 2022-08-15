package com.bardavid.boaz.photorater;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;

public class PairRates {
    private String url1;
    private String url2;
    private float rotation1;
    private float rotation2;
    private String focus;
    private String pairID;
    private int photo1Votes;
    private int photo2Votes;
    private int totalVotes;
    private String ownerID;

    public int getTotalVotes() {
        return totalVotes;
    }

    public void setTotalVotes(int totalVotes) {
        this.totalVotes = totalVotes;
    }
    public void addPhoto1Vote(){
        photo1Votes++;
        totalVotes++;
    }
    public void addPhoto2Vote(){
        photo2Votes++;
        totalVotes++;
    }
    private int reportsCount;
    public PairRates(String url1,String url2,float rotation1, float rotation2, String focus){
        this.photo1Votes =0;
        this.photo2Votes =0;
        this.reportsCount=0;
        this.totalVotes=0;
        this.rotation1=rotation1;
        this.rotation2=rotation2;
        this.url1=url1;
        this.url2=url2;
        this.focus=focus;
        this.pairID = url1.substring(url1.length() - 10);
    }
    public PairRates(){
        this.photo1Votes =0;
        this.photo2Votes =0;
        this.reportsCount=0;
        this.totalVotes=0;
    }
    public String getUrl1() {
        return url1;
    }
    public String getUrl2() {
        return url2;
    }

    public String getFocus() {
        return focus;
    }

    public void setFocus(String focus) {
        this.focus = focus;
    }

    public float getRotation1() {
        return rotation1;
    }

    public void setRotation1(float rotation1) {
        this.rotation1 = rotation1;
    }

    public float getRotation2() {
        return rotation2;
    }

    public void setRotation2(float rotation2) {
        this.rotation2 = rotation2;
    }

    public int getPhoto1Votes() {
        return photo1Votes;
    }
    public int getPhoto2Votes() {
        return photo2Votes;
    }

    public void setPhoto1Votes(int photo1Votes) {
        this.photo1Votes = photo1Votes;
    }
    public void setPhoto2Votes(int photo2Votes) {
        this.photo2Votes = photo2Votes;
    }
    public String getPairID(){
        return this.pairID;
    }
    public String getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }

    public int getReportsCount() {
        return reportsCount;
    }

    public void setReportsCount(int reportsCount) {
        this.reportsCount = reportsCount;
    }
    public void addReport() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().child(Prefs.PUBLIC);
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isRated = false;
                PairRates image = snapshot.child(Prefs.PAIRS_NEEDS_RATING).child(getPairID()).getValue(PairRates.class);
                if (image == null) {
                    isRated = true;
                    image = snapshot.child(Prefs.PAIRS_RATED).child(getPairID()).getValue(PairRates.class);
                }
                image.setReportsCount(getReportsCount() + 1);
                if (image.getReportsCount() == 3) {
                    //removes the image from it's place and moves it to reportedPAIRS
                    if (isRated)
                        databaseRef.child(Prefs.PAIRS_RATED).child(getPairID()).removeValue();
                    else
                        databaseRef.child(Prefs.PAIRS_NEEDS_RATING).child(getPairID()).removeValue();
                    databaseRef.child(Prefs.REPORTED_PAIRS).child(getPairID()).setValue(image);
                } else if (isRated)
                    databaseRef.child(Prefs.PAIRS_RATED).child(getPairID()).setValue(image);
                else
                    databaseRef.child(Prefs.PAIRS_NEEDS_RATING).child(getPairID()).setValue(image);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }


    public void addRating(int votedPhoto) {
        //Adds given rating to the image
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().child(Prefs.PUBLIC);
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isPhotoRated=false;
                PairRates pairRates = snapshot.child(Prefs.PAIRS_NEEDS_RATING).child(getPairID()).getValue(PairRates.class);
                if(pairRates ==null) {
                    pairRates = snapshot.child(Prefs.PAIRS_RATED).child(getPairID()).getValue(PairRates.class);
                    isPhotoRated = true;
                }
                if(votedPhoto==1)
                    pairRates.addPhoto1Vote();
                else
                    pairRates.addPhoto2Vote();
                try {
                    if (isPhotoRated)
                        databaseRef.child(Prefs.PAIRS_RATED).child(getPairID()).setValue(pairRates);
                    else
                        databaseRef.child(Prefs.PAIRS_NEEDS_RATING).child(getPairID()).setValue(pairRates);
                    if (pairRates.getTotalVotes() == Prefs.PHOTO_RATINGS_LIMIT_SIZE) {
                        movePhotoToRatedFolder();
                    }
                }
                catch (Exception e){
                    System.out.println("Problem occured");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    public void movePhotoToRatedFolder() {
        //Moves the image from PAIRSNeedsRating to PAIRSRated
        DatabaseReference oldDatabaseRef = FirebaseDatabase.getInstance().getReference(Prefs.PUBLIC).child(Prefs.PAIRS_NEEDS_RATING);
        DatabaseReference newDatabaseRef = FirebaseDatabase.getInstance().getReference(Prefs.PUBLIC).child(Prefs.PAIRS_RATED);
        oldDatabaseRef.child(getPairID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                PairRates image=snapshot.getValue(PairRates.class);
                try {
                    newDatabaseRef.child(image.getPairID()).setValue(image);
                    oldDatabaseRef.child(image.getPairID()).removeValue();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
