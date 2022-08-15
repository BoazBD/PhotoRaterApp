package com.bardavid.boaz.photorater;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Person;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {
    List<PairRates> pairs;
    Context context;
    int ratingsCount;
    String id;
    public RecyclerViewAdapter(Context ct,String id, List<PairRates> pairs, int ratingsCount){
        context=ct;
        this.id=id;
        this.pairs = pairs;
        this.ratingsCount=ratingsCount;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(context);
        View view=inflater.inflate(R.layout.activity_chart_template,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Picasso.get().load(pairs.get(position).getUrl1()).rotate(pairs.get(position).getRotation1()).fit().centerCrop().into(holder.image1, new Callback() {
            @Override
            public void onSuccess() {
                Picasso.get().load(pairs.get(position).getUrl2()).rotate(pairs.get(position).getRotation2()).fit().centerCrop().into(holder.image2, new Callback() {
                    @Override
                    public void onSuccess() {
                        holder.imagesLayout.setVisibility(View.VISIBLE);
                        if (ratingsCount < pairs.size() * Prefs.NUMBER_OF_RATINGS_NEEDED_FOR_EACH_PHOTO) {
                            //User did'nt rate enough
                            holder.imagesLayout.setVisibility(View.VISIBLE);
                            holder.progressBar.setVisibility(View.INVISIBLE);
                            holder.ratingsProcessText.setText("To view ratings, rate " + (pairs.size() * Prefs.NUMBER_OF_RATINGS_NEEDED_FOR_EACH_PHOTO - ratingsCount) + " more photos");
                            holder.chart.setVisibility(View.INVISIBLE);
                            holder.ratingsProcessText.setVisibility(View.VISIBLE);
                            holder.goRateBtn.setVisibility(View.VISIBLE);
                            holder.lock.setVisibility(View.VISIBLE);
                            enableDeleteBtn(holder,position);
                        } else if (pairs.get(position).getTotalVotes() == 0) {
                            //User rated enough but the photo does'nt have ratings yet
                            holder.imagesLayout.setVisibility(View.VISIBLE);
                            holder.progressBar.setVisibility(View.INVISIBLE);
                            holder.totalVotesPhoto1Txt.setVisibility(View.VISIBLE);
                            holder.totalVotesPhoto2Txt.setVisibility(View.VISIBLE);
                            holder.ratingsProcessText.setText("Unfortunately, there are no votes yet,\nbut the photos are currently being rated by other people.\nCome back later to see results!");
                            holder.chart.setVisibility(View.INVISIBLE);
                            holder.ratingsProcessText.setVisibility(View.VISIBLE);
                            enableDeleteBtn(holder,position);
                        } else {
                            //User rated enough, reveal rating chart
                            holder.progressBar.setVisibility(View.INVISIBLE);
                            //holder.chart.setVisibility(View.VISIBLE);
                            holder.imagesLayout.setVisibility(View.VISIBLE);
                            holder.ratingsProcessText.setText("*Photo is still being rated by other people right now, come back later for more votes!");
                            holder.ratingsProcessText.setVisibility(View.VISIBLE);
                            holder.totalVotesPhoto1Txt.setText("Total votes: "+ pairs.get(position).getPhoto1Votes());
                            holder.totalVotesPhoto2Txt.setText("Total votes: "+ pairs.get(position).getPhoto2Votes());
                            plotChart(holder, pairs.get(position).getPhoto1Votes(), pairs.get(position).getPhoto2Votes());
                            enableDeleteBtn(holder,position);
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(context, "Error loading image", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(context, "Error loading image", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void enableDeleteBtn(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position){
        holder.deleteBtn.setVisibility(View.VISIBLE);
        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Do you want to permanently delete this pair of photos?");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deletePair(position);
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
    }

    public void plotChart(MyViewHolder holder,int photo1Votes,int photo2Votes) {
        ArrayList<BarEntry> barEntries=new ArrayList<>();
        barEntries.add(new BarEntry(1,photo1Votes));
        barEntries.add(new BarEntry(2,photo2Votes));
        BarChart chart= holder.chart;
        BarDataSet barDataSet=new BarDataSet(barEntries,"Votes");
        barDataSet.setColor(Color.rgb(138,92,255));
        BarData theData= new BarData(barDataSet);
        ValueFormatter vf = new ValueFormatter() { //value format here, here is the overridden method
            @Override
            public String getFormattedValue(float value) {
                return ""+(int)value;
            }
        };
        theData.setBarWidth(0.4f);
        theData.setValueTextSize(22f);
        theData.setValueFormatter(vf); // turns float numbers to int
        chart.setData(theData);
        //hiding the grey background of the chart, default false if not set
        chart.setDrawGridBackground(false);
        //remove the bar shadow, default false if not set
        chart.setDrawBarShadow(false);
        //remove border of the chart, default false if not set
        chart.setDrawBorders(false);
        //remove the description label text located at the lower right corner
        chart.getDescription().setEnabled(false);
        //change the position of x-axis to the bottom
        chart.getXAxis().setPosition(chart.getXAxis().getPosition().BOTTOM);
        //set the horizontal distance of the grid line
        chart.getAxisLeft().setGranularity(1);
        chart.getAxisLeft().setGranularityEnabled(true);

        //hiding the x-axis line, default true if not set
        chart.getXAxis().setDrawAxisLine(false);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisRight().setDrawGridLines(false);
        //hiding the grid lines, default true if not set
        chart.getXAxis().setDrawGridLines(false);

        //hiding the left y-axis line, default true if not set
        chart.getAxisLeft().setDrawAxisLine(false);
        chart.getLegend().setEnabled(false);
        chart.getXAxis().setLabelCount(2);
        //hiding the right y-axis line, default true if not set
        chart.getAxisRight().setDrawAxisLine(false);
        chart.getAxisRight().setDrawLabels(false);
        chart.getXAxis().setEnabled(false);
        chart.getAxisLeft().setAxisMinimum(0);
        chart.getAxisLeft().setEnabled(false);
        chart.setDrawValueAboveBar(true);
        //chart.setFitBars(true);

        chart.animateY(1500);
    }
    public void deletePair(int position){
        //Removing pair from database
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child(Prefs.PUBLIC);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Remove pair from the person's list of pairs
                if(snapshot.child(Prefs.PEOPLE).hasChild(id)) {
                    PersonModel person = snapshot.child(Prefs.PEOPLE).child(id).getValue(PersonModel.class);
                    if (person != null) {
                        person.removePairCode(pairs.get(position).getPairID());
                        dbRef.child(Prefs.PEOPLE).child(id).setValue(person);
                    }
                }
                //Remove pair from Pairs database branch
                if(snapshot.child(Prefs.PAIRS_RATED).hasChild(pairs.get(position).getPairID())){
                    PairRates pair = snapshot.child(Prefs.PAIRS_RATED).child(pairs.get(position).getPairID()).getValue(PairRates.class);
                    if(pair!=null){
                        dbRef.child(Prefs.DELETED_PAIRS).child(pairs.get(position).getPairID()).setValue(pair);
                        dbRef.child(Prefs.PAIRS_RATED).child(pairs.get(position).getPairID()).removeValue();
                    }
                }
                if(snapshot.child(Prefs.PAIRS_NEEDS_RATING).hasChild(pairs.get(position).getPairID())){
                    PairRates pair = snapshot.child(Prefs.PAIRS_NEEDS_RATING).child(pairs.get(position).getPairID()).getValue(PairRates.class);
                    if(pair!=null){
                        dbRef.child(Prefs.DELETED_PAIRS).child(pairs.get(position).getPairID()).setValue(pair);
                        dbRef.child(Prefs.PAIRS_NEEDS_RATING).child(pairs.get(position).getPairID()).removeValue();
                    }
                }
                //Removes pair from recycler view
                pairs.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeRemoved(position,pairs.size());
                Toast.makeText(context,"Photos deleted successfully",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    @Override
    public int getItemCount() {
        return pairs.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView image1, image2;
        private ImageView numVotes, lock;
        private TextView totalVotesPhoto1Txt,totalVotesPhoto2Txt;
        private BarChart chart;
        private Button goRateBtn,deleteBtn;
        private TextView ratingsProcessText,ratesTxt;
        private ProgressBar progressBar;
        private LinearLayout imagesLayout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            image1 = itemView.findViewById(R.id.image1);
            image2 = itemView.findViewById(R.id.image2);
            totalVotesPhoto1Txt = itemView.findViewById(R.id.totalVotesPhoto1Txt);
            totalVotesPhoto2Txt = itemView.findViewById(R.id.totalVotesPhoto2Txt);
            imagesLayout=itemView.findViewById(R.id.imagesLayout);
            chart = itemView.findViewById(R.id.chart);
            ratingsProcessText=itemView.findViewById(R.id.ratingsProcessText);
            progressBar=itemView.findViewById(R.id.progress_bar);
            lock=itemView.findViewById(R.id.lock_image);
            deleteBtn=itemView.findViewById(R.id.deleteBtn);
            goRateBtn=itemView.findViewById(R.id.goRateBtn);
            goRateBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, RateActivity.class);
                    context.startActivity(intent);
                }
            });
        }
    }
}

