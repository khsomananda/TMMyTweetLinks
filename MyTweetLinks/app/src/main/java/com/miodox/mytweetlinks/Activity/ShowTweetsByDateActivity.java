package com.miodox.mytweetlinks.Activity;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.miodox.mytweetlinks.Adapters.TodYesAdapter;
import com.miodox.mytweetlinks.Models.MyTweet;
import com.miodox.mytweetlinks.Pref;
import com.miodox.mytweetlinks.R;

public class ShowTweetsByDateActivity extends AppCompatActivity {


    private RecyclerView mRecyclerView;
    private TodYesAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private DatabaseReference mDatabase;

    private TextView mSwipeDownMessage;
    private int prevCount=0;
    private long offset;
    private int mLastVisibleItemPosition;
    private ProgressBar qaProgressBar;
    private ProgressBar mProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_tweets_by_date);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent=getIntent();
        String clickDate=intent.getStringExtra("date");

        setTitle(clickDate);

        mProgress=(ProgressBar)findViewById(R.id.loading_by_date_progressBar);
        qaProgressBar=(ProgressBar)findViewById(R.id.load_more_date_progressBar);
        //hide progress bar when activity start
        qaProgressBar.setVisibility(View.GONE);

        mSwipeDownMessage=(TextView)findViewById(R.id.swipe_down_message);

        mRecyclerView=(RecyclerView)findViewById(R.id.tweets_by_date_recycler_view);
        mLayoutManager=new LinearLayoutManager(getApplicationContext());

        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter=new TodYesAdapter(ShowTweetsByDateActivity.this,clickDate);
        mRecyclerView.setAdapter(mAdapter);

        //used as key in database to stored links of the particular date
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String useruid=user.getUid();




        mDatabase = FirebaseDatabase.getInstance().getReference(useruid).child("AllTweets").child(clickDate);


        offset=(-1)* Pref.getSinceId(ShowTweetsByDateActivity.this);
        retrievingData();


        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                //swipe refresh should be idle

                    mLastVisibleItemPosition = mLayoutManager.findLastCompletelyVisibleItemPosition();
                    if (mLastVisibleItemPosition == mAdapter.getItemCount() - 1) {
                        if (prevCount != mLastVisibleItemPosition) {
                           // Log.d("soma", "hurray");
                            qaProgressBar.setVisibility(View.VISIBLE);
                            retrievingData();
                        }

                        prevCount = mLastVisibleItemPosition;
                    }
                }

        });


    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

               finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    void retrievingData()
    {
        Query tQuery= mDatabase.orderByChild("tweetId").startAt(offset+100).limitToFirst(10);

        tQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //if call via scroll listener
                mProgress.setVisibility(View.INVISIBLE);

                    qaProgressBar.setVisibility(View.GONE);


                for (DataSnapshot qaSnapshot: dataSnapshot.getChildren())
                {

                    MyTweet mData= qaSnapshot.getValue(MyTweet.class);
                    mAdapter.addItem(mAdapter.getItemCount(),mData,qaSnapshot.getKey());

                    offset=mData.getTweetId();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                mProgress.setVisibility(View.INVISIBLE);
                    qaProgressBar.setVisibility(View.GONE);

            }
        });
    }


}
